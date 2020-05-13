package ai.cochlear.sense;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLContext;

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
	private boolean inferenced;
	private SSLContext grpcSSLContext;

	private static enum FileFormat {
		mp3,
		wav,
		ogg,
		flac,
		mp4
	}

	public static class Builder {
		private String apiKey;
		private InputStream reader;
		private String format;
		private String host = "sense.cochlear.ai";

		public Builder withApiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		public Builder withReader(InputStream reader) {
			this.reader = reader;
			return this;
		}

		public Builder withFormat(String format) {
			this.format = format;
			return this;
		}

		public Builder withHost(String host) {
			this.host = host;
			return this;
		}

		public File build() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
			return new File(this.apiKey,this.reader, this.format,this.host);
		}

	}

	private File(String apiKey, InputStream reader, String format, String host) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
		formatError(format);
		grpcSSLContext = new InternalCertificate().get();

		this.apiKey = apiKey;
		this.reader = reader;
		this.format = format;
		this.host = host;
		this.inferenced = false;
	}

	private void formatError(String fileFormat) throws IllegalArgumentException{
		try {
			FileFormat.valueOf(fileFormat);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid File format");
		}
	}

	public Result inference() throws RuntimeException, IOException, InterruptedException {
		if (this.inferenced) {
			throw new RuntimeException("File was already inferenced");
		}
		this.inferenced = true;

		final ManagedChannel channel = OkHttpChannelBuilder.forAddress(host, Constants.PORT).sslSocketFactory(grpcSSLContext.getSocketFactory()).build();
		final SenseStub stub = SenseGrpc.newStub(channel);

		final String[] outputs = {new String()};
		final CountDownLatch finishLatch = new CountDownLatch(1);

		StreamObserver<Response> responseObserver = new StreamObserver<Response>() {
		    @Override
		    public void onNext(Response out) {
		    	outputs[0] = out.getOutputs();
		    }

		    @Override
		    public void onError(Throwable t) {
		    	Status status = Status.fromThrowable(t);
		    	throw status.asRuntimeException();
		    }

		    @Override
		    public void onCompleted()
			{
		    	finishLatch.countDown();
		    }
		};

		StreamObserver<Request> requestObserver = stub.sense(responseObserver);

		byte[] audioBytes = new byte[Constants.MAX_DATA_SIZE];
		while(reader.read(audioBytes) > 0) {
			Request request = Request.newBuilder()
					.setData(ByteString.copyFrom(audioBytes))
					.setApikey(this.apiKey)
					.setFormat(format)
					.setApiVersion(Constants.API_VERSION)
					.setUserAgent(Constants.USER_AGENT)
					.build();
			requestObserver.onNext(request);
		}

		requestObserver.onCompleted();

		finishLatch.await();

		return new Result(outputs[0]);
	}
}