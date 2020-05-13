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

    public static class Builder {
        private String apiKey;
        private Iterable<byte[]> streamer;
        private int samplingRate;
        private String dataType;
        private String host = "sense.cochlear.ai";
        private int maxEventsHistorySize = 0;
        private static final int MIN_RECOMMENDED_SAMPLING_RATE = 22050;

        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder withStreamer(Iterable<byte[]> streamer) {
            this.streamer = streamer;
            return this;
        }

        public Builder withSamplingRate(int samplingRate) {
            if (samplingRate < MIN_RECOMMENDED_SAMPLING_RATE) {
                System.out.println("a sampling rate of at least 22050 is recommended");
            }
            this.samplingRate = samplingRate;
            return this;
        }

        public Builder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withMaxEventsHistorySize(int n) {
            this.maxEventsHistorySize = n;
            return this;
        }

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

    public void inference(SenseResultListener listener) throws IOException, InterruptedException {
        StreamObserver<RequestStream> connection = newConnection(listener);


        ByteArrayOutputStream buffer = new ByteArrayOutputStream(halfSecondBytesNumber);

        for (byte[] bytes : streamer) {
            if (bytes == null) {
                System.out.println("should be stopped");
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
                    senseResultListener.onResult(result[0]);
                }
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
