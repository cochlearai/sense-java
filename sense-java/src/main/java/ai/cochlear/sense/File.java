package ai.cochlear.sense;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.StreamObserver;

import ai.cochlear.sense.grpc.Request;
import ai.cochlear.sense.grpc.SenseGrpc;
import ai.cochlear.sense.grpc.SenseGrpc.SenseStub;
import ai.cochlear.sense.grpc.Response;


public class File {
	private final String apiKey;
	private final InputStream reader;
	private final String format;
	private final String host;
	private ManagedChannel channel;
	private SenseStub stub;
	private boolean inferenced;
	private Throwable failed = null;

	private static enum FileFormat {
		mp3,
		wav,
		ogg,
		flac,
		mp4
	}

	public File(String apiKey, InputStream reader, String format, String host) {
		formatError(format);
		this.apiKey = apiKey;
		this.reader = reader;
		this.format = format;
		this.host = host;
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

	public void fileError(String filename) {
		java.io.File file = new java.io.File(filename);
		if(!file.exists()) {
			try {
				throw new IOException("File not found");
			}
			catch (IOException e) {

			}
		}
	}

	public void formatError(String fileFormat) {
		FileFormat temp;
		try {
			temp = FileFormat.valueOf(fileFormat);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid File format");
		}
	}

	public Result inference() throws InterruptedException, RuntimeException, IOException{
		if (this.inferenced) {
			throw new RuntimeException("File was already inferenced");
		}
		this.inferenced = true;
		SSLContext grpcSSLContext = getSSLContext();
		this.channel = OkHttpChannelBuilder.forAddress(host, Constants.PORT).sslSocketFactory(grpcSSLContext.getSocketFactory()).build();
		this.stub = SenseGrpc.newStub(channel);
		final String[] result = new String[1];
		final CountDownLatch finishLatch = new CountDownLatch(1);
		StreamObserver<Response> responseObserver = new StreamObserver<Response>() {
		    @Override
		    public void onNext(Response out) {
		    	result[0] = out.getOutputs();

		    	//log
				//System.out.println(result[0]);
		    }
		    @Override
		    public void onError(Throwable t) {
		    	Status status = Status.fromThrowable(t);
		    	System.err.println(status);
		    	failed = t;
		    	finishLatch.countDown();
		    }
		    @Override
		    public void onCompleted()
			{
		    	finishLatch.countDown();
		    }
		};
		StreamObserver<Request> requestObserver = stub.sense(responseObserver);

		try {
			byte[] audioBytes = new byte[Constants.MAX_DATA_SIZE];
			while(reader.read(audioBytes) > 0) {
				Request _input = Request.newBuilder()
						.setData(ByteString.copyFrom(audioBytes))
						.setApikey(this.apiKey)
						.setFormat(format)
						.setApiVersion(Constants.API_VERSION)
						.setUserAgent(Constants.USER_AGENT)
						.build();
				requestObserver.onNext(_input);
			}
			
		}catch(RuntimeException e) {
			requestObserver.onError(e);
			throw e;
		}
		requestObserver.onCompleted();
		if(!finishLatch.await(1,TimeUnit.MINUTES)) {
			throw new RuntimeException(
					"Could not finish rpc within 1 minute, the server is likely down");
		}
		return new Result(result[0]);
	}
}