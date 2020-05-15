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

/**
 * file represents a class that can inference audio coming from an audio file.
 * An audio file is any source of audio data which duration is known at runtime.
 * Because duration is known at runtime, server will wait for the whole file to be received before to start inferencing.
 * All inferenced data will be received in one payload.
 * A file can be for instance, a mp3 file stored locally, a wav file accessible from an url etc...
 * So far wav, flac, mp3, ogg, mp4 are supported.
 * If you are using another file encoding format, let us know at support@cochlear.ai so that we can priorize it in our internal roadmap.
 *
 * @author  Sejong Jeong
 * @version 1.0
 */
public class File {
	private final String apiKey;
	private final InputStream reader;
	private final String format;
	private final String host;
	private boolean inferenced;
	private SSLContext grpcSSLContext;

	/**
	 * Supported FileFormat
	 */
	private static enum FileFormat {
		mp3,
		wav,
		ogg,
		flac,
		mp4
	}

	/**
	 * To create a file instance, you need to use a Builder instance.
	 * Builder is following the builder pattern and calling its build method will create a file instance.
	 */
	public static class Builder {
		private String apiKey;
		private InputStream reader;
		private String format;
		private String host = "sense.cochlear.ai";

		/**
		 * api key of cochlear.ai projects available at https://dashboard.cochlear.ai
		 * @param apiKey - Copy Your Cochlear API Key Received by homepage
		 */
		public Builder withApiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		/**
		 *data reader to the file data
		 * @param reader must be InputStream
		 */
		public Builder withReader(InputStream reader) {
			this.reader = reader;
			return this;
		}

		/**
		 * format of the audio file : can be mp3, flac, wav, ogg, etc...
		 * @param format
		 * @return
		 */
		public Builder withFormat(String format) {
			this.format = format;
			return this;
		}

		/**
		 * host address that performs grpc communication.
		 * If this method is not used, default host is used.
		 * @param host - defalut host is "sense.cochlear.ai"
		 * @return
		 */
		public Builder withHost(String host) {
			this.host = host;
			return this;
		}

		/**
		 * creates a File instance
		 * @return File Instance
		 * @throws CertificateException
		 * @throws UnrecoverableKeyException
		 * @throws NoSuchAlgorithmException
		 * @throws KeyStoreException
		 * @throws KeyManagementException
		 * @throws IOException
		 */
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

	/**
	 * When calling inference, a GRPC connection will be established with the backend, audio data of the File will be sent and a Result instance will be returned in case of success.
	 * Note that network is not reached until inference method is called.
	 * Note that inference can be called only once per file instance.
	 * @return Result
	 * @throws RuntimeException
	 * @throws IOException
	 * @throws InterruptedException
	 */
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