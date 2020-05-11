package ai.cochlear.sense;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import ai.cochlear.sense.grpc.RequestStream;
import ai.cochlear.sense.grpc.SenseGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;

import ai.cochlear.sense.grpc.Response;
import ai.cochlear.sense.grpc.SenseGrpc.SenseStub;


public class Stream {
    private static final long deadline = 10;
    private final String apiKey;
    private final InputStream streamer;
    private final int samplingRate;
    private final String dataType;
    private final String host;
    private final int maxEventsHistorySize;
    private SenseResultListener senseResultListener = null;
    private ManagedChannel channel = null;
    private SenseStub stub = null;
    private boolean inferenced;
    private Throwable failed = null;
    private StreamObserver<RequestStream> requestObserver;
    public Result result;

    public Stream(String apiKey, InputStream streamer, int samplingRate, String dataType, String host, int maxEventsHistorySize) {
        this.apiKey = apiKey;
        this.streamer = streamer;
        this.samplingRate = samplingRate;
        this.dataType = dataType;
        this.host = host;
        this.maxEventsHistorySize = maxEventsHistorySize;
        this.inferenced = false;
    }

    private SSLContext getSSLContext()
    {
        SSLContext sslContext = null;
        String temp = Constants.SERVER_CA_CERTIFICATE;
        temp = temp.replace("\n","\r\n");
        InputStream caInputStream = new ByteArrayInputStream(temp.getBytes());

        try
        {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(caInputStream);

            System.out.println("cert=" + ((X509Certificate) cert).getSubjectDN());

            String alias = cert.getSubjectX500Principal().getName();

            // Load Client CA
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry(alias, cert);

            // Create KeyManager using Client CA
            String kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
            kmf.init(keyStore, null);

            // Create TrustManager using Client CA
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create SSLContext using KeyManager and TrustManager
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        }
        catch(Exception exp)
        {
            sslContext = null;
        }

        return sslContext;
    }

    private int fs;
    private boolean big_endian;

    private short[] b2s(byte[] barr) {
        short[] shorts = new short[barr.length / 2];
        if(this.big_endian)
            ByteBuffer.wrap(barr).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        else
            ByteBuffer.wrap(barr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    private float[] s2f (short[] sarr) {
        float[] floats = new float[sarr.length];
        for(int i=0; i<sarr.length; i++) {
            float f;
            int r = sarr[i];
            f = ((float) r) / (float) 32768;
            if( f > 1 ) f = 1;
            if( f < -1 ) f = -1;
            floats[i] = f;
        }
        return floats;
    }

    private byte[] f2b (float[] farr) {
        byte[] bytes = new byte[farr.length*4];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(farr);
        return bytes;
    }

    private byte[] int16Tofloat32(byte[] bytes){
        return f2b(s2f(b2s(bytes)));
    }





    public void pushShort(short[] shorts) throws Exception{
        //pushFloat32(s2f(shorts));
    }

    public void pushFloat32(float[] floats) throws Exception{
        //pushByte(f2b(floats));
    }

    public void inference() throws Exception{
        grpcRequests();
        result = new Result();
    }

    public void close() throws Exception {
        if (!this.inferenced) {
            throw new Exception("canot close stream if this one was not inferenced");
        }
        if (this.channel == null) {
            throw new Exception("stream was already closed");
        }
        requestObserver.onCompleted();
        this.channel.shutdown();
    }



    private void grpcRequests() throws Exception{
        sendToGrpc();
        if(this.senseResultListener ==null){
            throw new Exception("Listener not registered.");
        }
        RequestStream streamRequest = RequestStream.newBuilder()
                .setData(ByteString.readFrom(streamer))
                .setApikey(apiKey)
                .setSr(samplingRate)
                .setDtype("float32")
                .setApiVersion(Constants.API_VERSION)
                .setUserAgent(Constants.USER_AGENT)
                .build();
        requestObserver.onNext(streamRequest);
    }


    private void sendToGrpc() throws Exception{
        if (this.inferenced) {
            throw new RuntimeException("Stream was already inferenced");
        }
        this.inferenced = true;
        SSLContext grpcSSLContext = getSSLContext();
        this.channel = OkHttpChannelBuilder.forAddress(host, Constants.PORT).sslSocketFactory(grpcSSLContext.getSocketFactory()).build();
        this.stub = SenseGrpc.newStub(channel);

        final CountDownLatch finishLatch = new CountDownLatch(1);

        class BistreamObserver implements StreamObserver<Response>{
            @Override
            public void onNext(Response out) {
                result.appendNewResult(out.getOutputs(),maxEventsHistorySize);
                senseResultListener.onResult(result);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                senseResultListener.onError("ERROR : "+ status);
                failed = t;
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                senseResultListener.onComplete();
                finishLatch.countDown();

            }
        }
        requestObserver = stub.withDeadlineAfter(deadline, TimeUnit.MINUTES)
                .senseStream(new BistreamObserver());



    }

    public void setListener(SenseResultListener listener){
        this.senseResultListener = listener;
    }

}
