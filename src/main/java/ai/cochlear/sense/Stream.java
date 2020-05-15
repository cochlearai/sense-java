package ai.cochlear.sense;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import ai.cochlear.sense.grpc.RequestStream;
import ai.cochlear.sense.grpc.SenseGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;

import ai.cochlear.sense.grpc.Response;
import ai.cochlear.sense.grpc.SenseGrpc.SenseStub;

/**
 * stream represents a class that can inference audio coming from an audio stream.
 * An audio stream is any source of data which duration is not known at runtime.
 * Because duration is not known, server will inference audio as it comes.
 * One second of audio will be required before the first result to be returned.
 * After that, one result will be given every 0.5 second of audio.
 * A stream can be for instance, the audio data comming from a microphone, audio data comming from a web radio etc...
 * Streams can be stopped at any moment while inferencing.
 * For now, the only format that is supported for streaming is a raw data stream (PCM float32 stream).
 * Raw data being sent has to be a mono channel audio stream.
 * Its sampling rate has to be given to describe the raw audio data.
 * For best performance, we recommend using a sampling rate of 22050Hz and data represented as float32.
 * Multiple results will be returned by Listener.
 * If you are using another stream encoding format that is not supported, let us know at support@cochlear.ai so that we can priorize it in our internal roadmap.
 *
 * @author  Sejong Jeong
 * @version 1.0
 */
public class Stream {
    private static final long deadline = 10;
    private final String apiKey;
    private final Iterable<byte[]> streamer;
    private int halfSecondBytesNumber;
    private final int samplingRate;
    private final String dataType;
    private final String host;
    private final int maxEventsHistorySize;
    private boolean inferenced;
    private SSLContext grpcSSLContext;
    private CountDownLatch finishLatch;

    /**
     * To create a stream instance, you need to use a Builder instance.
     * Builder is following the builder pattern and calling its build method will create a stream instance.
     */
    public static class Builder {
        private String apiKey;
        private Iterable<byte[]> streamer;
        private int samplingRate;
        private String dataType;
        private String host = "sense.cochlear.ai";
        private int maxEventsHistorySize = 0;
        private static final int MIN_RECOMMENDED_SAMPLING_RATE = 22050;

        /**
         * api key of cochlear.ai projects available at dashboard.cochlear.ai
         * @param apiKey - Copy Your Cochlear API Key Received by homepage
         */
        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         *  data of the pcm stream
         * @param streamer
         */
        public Builder withStreamer(Iterable<byte[]> streamer) {
            this.streamer = streamer;
            return this;
        }

        /**
         * sampling rate of the pcm stream
         * @param samplingRate
         */
        public Builder withSamplingRate(int samplingRate) {
            if (samplingRate < MIN_RECOMMENDED_SAMPLING_RATE) {
                System.out.println("a sampling rate of at least 22050 is recommended");
            }
            this.samplingRate = samplingRate;
            return this;
        }

        /**
         * type of the pcm float32 stream
         * @param dataType
         */
        public Builder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        /**
         * host address that performs grpc communication.
         * If this method is not used, default host is used.
         * @param host - defalut host is "sense.cochlear.ai"
         */
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * max number of events from previous inference to keep in memory
         * @param n
         */
        public Builder withMaxEventsHistorySize(int n) {
            this.maxEventsHistorySize = n;
            return this;
        }

        /**
         * creates a stream instance
         * @return Stream Instance
         * @throws Exception
         */
        public Stream build() throws Exception {
            return new Stream(this.apiKey, this.streamer, this.samplingRate, this.dataType, this.host, this.maxEventsHistorySize);
        }
    }

    private Stream(String apiKey, Iterable<byte[]> streamer, int samplingRate, String dataType, String host, int maxEventsHistorySize) throws Exception {
        grpcSSLContext = new InternalCertificate().get();

        this.apiKey = apiKey;
        this.streamer = streamer;
        this.samplingRate = samplingRate;
        this.dataType = dataType;
        this.host = host;
        this.maxEventsHistorySize = maxEventsHistorySize;
        this.inferenced = false;
        finishLatch = new CountDownLatch(0);

        this.halfSecondBytesNumber = samplingRate * dataTypeSize(dataType) / 2;
    }

    private int dataTypeSize(String datatype) throws Exception {
        switch (datatype) {
            case "float32":
            case "int32":
                return 4;
            case "float64":
            case "int64":
                return 8;
            default:
                throw new Exception(datatype + "  is not a valid data type");
        }
    }

    /**
     * When calling inference, a GRPC connection will be established with the backend, audio data of the stream will be sent every 0.5s.
     * Once result is returned by the server, the add Result function is called.
     * Note that network is not reached until inference method is called.
     * Note that inference can be called only once per stream instance.
     * @param listener
     * @throws IOException
     * @throws InterruptedException
     */
    public void inference(SenseResultListener listener) throws IOException, InterruptedException {
        StreamObserver<RequestStream> connection = newConnection(listener);


        ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

        for (byte[] bytes : streamer) {
            if (bytes == null) {
                connection.onCompleted();
                break;
            }
            buffer.write(bytes);
            if(buffer.size() >= halfSecondBytesNumber) {
                byte[] toBeSent = buffer.toByteArray();
                int startIndex = 0;
                while(startIndex < toBeSent.length) {
                    int endIndex = Math.min(toBeSent.length, startIndex + Constants.MAX_DATA_SIZE);

                    ByteString data = ByteString.copyFrom(toBeSent, startIndex, endIndex);

                    RequestStream request = RequestStream.newBuilder()
                        .setData(data)
                        .setApikey(apiKey)
                        .setSr(samplingRate)
                        .setDtype(dataType)
                        .setApiVersion(Constants.API_VERSION)
                        .setUserAgent(Constants.USER_AGENT)
                        .build();

                    startIndex = endIndex;
                    connection.onNext(request);
                }

                buffer.reset();
            }
        }

        finishLatch.await();
    }

    private StreamObserver<RequestStream> newConnection(SenseResultListener senseResultListener) throws RuntimeException {
        if (this.inferenced) {
            throw new RuntimeException("Stream was already inferenced");
        }
        this.inferenced = true;

        final Result[] result = {null};

        ManagedChannel channel = OkHttpChannelBuilder.forAddress(host, Constants.PORT).sslSocketFactory(grpcSSLContext.getSocketFactory()).build();
        SenseStub stub = SenseGrpc.newStub(channel);

        finishLatch = new CountDownLatch(1);

        class BistreamObserver implements StreamObserver<Response>{
            @Override
            public void onNext(Response out) {
                if(result[0] == null) {
                    result[0] = new Result(out.getOutputs());
                } else {
                    result[0].appendNewResult(out.getOutputs(),maxEventsHistorySize);
                }
                senseResultListener.onResult(result[0]);
            }

            @Override
            public void onError(Throwable t) {
                senseResultListener.onError(t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                senseResultListener.onComplete();
                finishLatch.countDown();
            }
        }

        StreamObserver<RequestStream> requestObserver = stub.withDeadlineAfter(deadline, TimeUnit.MINUTES)
                .senseStream(new BistreamObserver());

        return requestObserver;
    }
}
