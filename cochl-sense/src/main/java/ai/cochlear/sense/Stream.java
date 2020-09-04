package ai.cochlear.sense;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import ai.cochlear.sense.proto.CochlGrpc;
import ai.cochlear.sense.proto.SenseClient;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

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
    private final boolean smartFiltering;
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
        public String apiKey;
        public Iterable<byte[]> streamer;
        public int samplingRate;
        public String dataType;
        public String host = Constants.HOST;
        public int maxEventsHistorySize = 0;
        public boolean smartFiltering = false;
        private static final int MIN_RECOMMENDED_SAMPLING_RATE = 22050;

        /**
         * api key of cochlear.ai projects available at dashboard.cochlear.ai
         * @param apiKey - Copy Your Cochlear API Key Received by homepage
         * @return itself
         */
        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         *  data of the pcm stream
         * @param streamer an asynchronous stream of data
         * @return itself
         */
        public Builder withStreamer(Iterable<byte[]> streamer) {
            this.streamer = streamer;
            return this;
        }

        /**
         * sampling rate of the pcm stream
         * @param samplingRate rate of the audio stream in Hertz. 22050 is recommanded.
         * @return itself
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
         * @param dataType of samples of the stream. Can be int32, int64, float32, float64
         * @return itself
         */
        public Builder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        /**
         * host address that performs grpc communication.
         * If this method is not used, default host is used.
         * @param host - defalut host is "sense.cochlear.ai"
         * @return itself
         */
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * max number of events from previous inference to keep in memory
         * @param n the maximum number of previously passed events to show on each new result being sent
         * @return itself
         */
        public Builder withMaxEventsHistorySize(int n) {
            this.maxEventsHistorySize = n;
            return this;
        }

        /**
         * Activate smart filtering. Smart filtering allows to skipped some audio segments
         * allowing to decrease bill.
         * @param smartFiltering activate or not  the smart filtering (default false)
         * @return self
         */
        public Builder withSmartFiltering(boolean smartFiltering) {
            this.smartFiltering = smartFiltering;
            return this;
        }

        /**
         * creates a stream instance
         * @return Stream Instance
         * @throws Exception when errors in parameters
         * @return a stream instance
         */
        public Stream build() throws Exception {
            return new Stream(this.apiKey, this.streamer, this.samplingRate, this.dataType, this.host, this.maxEventsHistorySize, this.smartFiltering);
        }
    }

    private Stream(String apiKey, Iterable<byte[]> streamer, int samplingRate, String dataType, String host, int maxEventsHistorySize, boolean smartFiltering) throws Exception {
        grpcSSLContext = new InternalCertificate().get();

        this.apiKey = apiKey;
        this.streamer = streamer;
        this.samplingRate = samplingRate;
        this.dataType = dataType;
        this.host = host;
        this.maxEventsHistorySize = maxEventsHistorySize;
        this.inferenced = false;
        this.smartFiltering = smartFiltering;
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
     * @param listener callbacks that allow to send asynchronously some data and receive asynchronously some results
     * @throws IOException when GRPC connection error
     * @throws InterruptedException when thread interrupted
     */
    public void inference(SenseResultListener listener) throws IOException, InterruptedException {
        StreamObserver<SenseClient.Audio> connection = newConnection(listener);


        ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

        int offset = 0;

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

                    SenseClient.Audio request = SenseClient.Audio.newBuilder()
                        .setData(data)
                        .setSegmentStartTime(0)
                        .setSegmentOffset(offset)
                        .build();

                    offset += data.size();

                    startIndex = endIndex;
                    connection.onNext(request);
                }

                buffer.reset();
            }
        }

        finishLatch.await();
    }

    private StreamObserver<SenseClient.Audio> newConnection(final SenseResultListener senseResultListener) throws RuntimeException {
        if (this.inferenced) {
            throw new RuntimeException("Stream was already inferenced");
        }
        this.inferenced = true;

        final Result[] result = {null};

        ManagedChannel channel = OkHttpChannelBuilder
                .forTarget(host)
                .sslSocketFactory(grpcSSLContext.getSocketFactory())
                .build();
        CochlGrpc.CochlStub stub = CochlGrpc.newStub(channel);

        io.grpc.Metadata metadata = new Metadata()
                .withApiKey(this.apiKey)
                .withStreamFormat(this.dataType, this.samplingRate)
                .withSmartFiltering(this.smartFiltering)
                .toGRPCMetadata();

        stub = MetadataUtils.attachHeaders(stub, metadata);

        finishLatch = new CountDownLatch(1);

        class BistreamObserver implements StreamObserver<SenseClient.CochlSense>{
            @Override
            public void onNext(SenseClient.CochlSense raw) {
                if(result[0] == null) {
                    result[0] = new Result(raw);
                } else {
                    result[0].appendNewResult(raw,maxEventsHistorySize);
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

        StreamObserver<SenseClient.Audio> requestObserver = stub.withDeadlineAfter(deadline, TimeUnit.MINUTES)
                .sensestream(new BistreamObserver());

        return requestObserver;
    }
}
