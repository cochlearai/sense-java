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

import ai.cochlear.sense.proto.CochlGrpc;
import ai.cochlear.sense.proto.SenseClient;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

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
	private final boolean smartFiltering;
	private final String host;
	private boolean inferenced;
	private SSLContext grpcSSLContext;

	/**
	 * Supported FileFormat
	 */
	private enum FileFormat {
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
		public String apiKey;
		public InputStream reader;
		public String format;
		public String host = Constants.HOST;
		public boolean smartFiltering = false;

		/**
		 * api key of cochlear.ai projects available at https://dashboard.cochlear.ai
		 * @param apiKey - Copy Your Cochlear API Key Received by homepage
		 * @return itself
		 */
		public Builder withApiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		/**
		 *data reader to the file data
		 * @param reader must be InputStream
		 * @return itself
		 */
		public Builder withReader(InputStream reader) {
			this.reader = reader;
			return this;
		}

		/**
		 * Set format of the audio file
		 * @param format can be mp3, flac, wav, ogg, etc...
		 * @return itself
		 */
		public Builder withFormat(String format) {
			this.format = format;
			return this;
		}

		/**
		 * host endpoint that will receive grpc communication.
		 * If this method is not used, default host is used.
		 * @param host - defaults to cochlear.ai production server
		 * @return itself
		 */
		public Builder withHost(String host) {
			this.host = host;
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
		 * creates the File instance
		 * @return File Instance
		 * @throws CertificateException related with GRPC secure certificate communication
		 * @throws UnrecoverableKeyException related with GRPC secure certificate communication
		 * @throws NoSuchAlgorithmException related with GRPC secure certificate communication
		 * @throws KeyStoreException related with GRPC secure certificate communication
		 * @throws KeyManagementException related with GRPC secure certificate communication
		 * @throws IOException related with GRPC secure certificate communication
		 */
		public File build() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
			return new File(this.apiKey,this.reader, this.format,this.host, this.smartFiltering);
		}

	}

	private File(String apiKey, InputStream reader, String format, String host, boolean smartFiltering) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
		formatError(format);
		grpcSSLContext = new InternalCertificate().get();

		this.apiKey = apiKey;
		this.reader = reader;
		this.format = format;
		this.host = host;
		this.inferenced = false;
		this.smartFiltering = smartFiltering;
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
	 * @throws RuntimeException returned when the file was already inferenced
	 * @throws IOException returned when error with GRPC IO
	 * @throws InterruptedException returned on interruption
	 */
	public Result inference() throws Exception {
		if (this.inferenced) {
			throw new RuntimeException("File was already inferenced");
		}
		this.inferenced = true;

		final ManagedChannel channel = OkHttpChannelBuilder.forTarget(host).sslSocketFactory(grpcSSLContext.getSocketFactory()).build();

		CochlGrpc.CochlStub stub = CochlGrpc.newStub(channel);

		io.grpc.Metadata metadata = new Metadata()
				.withApiKey(this.apiKey)
				.withFileFormat(this.format)
				.withSmartFiltering(this.smartFiltering)
				.toGRPCMetadata();

		stub = MetadataUtils.attachHeaders(stub, metadata);

		final Result[] result = {null};
		final Status[] errorStatus = {null};

		final CountDownLatch finishLatch = new CountDownLatch(1);

		StreamObserver<SenseClient.CochlSense> responseObserver = new StreamObserver<SenseClient.CochlSense>() {
		    @Override
		    public void onNext(SenseClient.CochlSense out) {
		    	result[0] = new Result(out);
		    }

		    @Override
		    public void onError(Throwable t) {
		    	if (errorStatus[0] != null){
		    		return;
				}
		    	errorStatus[0] = Status.fromThrowable(t);
		    	finishLatch.countDown();
		    }

		    @Override
		    public void onCompleted()
			{
		    	finishLatch.countDown();
		    }
		};

		StreamObserver<SenseClient.Audio> requestObserver = stub.sensefile(responseObserver);

		byte[] audioBytes = new byte[Constants.MAX_DATA_SIZE];
		int offset = 0;
		for(int numberBytes = reader.read(audioBytes); numberBytes> 0; numberBytes = reader.read(audioBytes)) {
			SenseClient.Audio request = SenseClient.Audio.newBuilder()
					.setData(ByteString.copyFrom(audioBytes))
					.setSegmentStartTime(0)
					.setSegmentOffset(offset)
					.build();
			offset += numberBytes;
			requestObserver.onNext(request);
		}

		requestObserver.onCompleted();

		finishLatch.await();

		if(errorStatus[0] != null) {
			throw errorStatus[0].asException();
		}

		return result[0];
	}
}