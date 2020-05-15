package ai.cochlear.sense.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.1)",
    comments = "Source: SenseClient.proto")
public final class SenseGrpc {

  private SenseGrpc() {}

  public static final String SERVICE_NAME = "sense.full.v1.Sense";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<Request,
      Response> getSenseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sense",
      requestType = Request.class,
      responseType = Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<Request,
      Response> getSenseMethod() {
    io.grpc.MethodDescriptor<Request, Response> getSenseMethod;
    if ((getSenseMethod = SenseGrpc.getSenseMethod) == null) {
      synchronized (SenseGrpc.class) {
        if ((getSenseMethod = SenseGrpc.getSenseMethod) == null) {
          SenseGrpc.getSenseMethod = getSenseMethod =
              io.grpc.MethodDescriptor.<Request, Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sense"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  Request.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  Response.getDefaultInstance()))
              .build();
        }
      }
    }
    return getSenseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<RequestStream,
      Response> getSenseStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sense_stream",
      requestType = RequestStream.class,
      responseType = Response.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<RequestStream,
      Response> getSenseStreamMethod() {
    io.grpc.MethodDescriptor<RequestStream, Response> getSenseStreamMethod;
    if ((getSenseStreamMethod = SenseGrpc.getSenseStreamMethod) == null) {
      synchronized (SenseGrpc.class) {
        if ((getSenseStreamMethod = SenseGrpc.getSenseStreamMethod) == null) {
          SenseGrpc.getSenseStreamMethod = getSenseStreamMethod =
              io.grpc.MethodDescriptor.<RequestStream, Response>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sense_stream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  RequestStream.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  Response.getDefaultInstance()))
              .build();
        }
      }
    }
    return getSenseStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SenseStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SenseStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SenseStub>() {
        @Override
        public SenseStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SenseStub(channel, callOptions);
        }
      };
    return SenseStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SenseBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SenseBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SenseBlockingStub>() {
        @Override
        public SenseBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SenseBlockingStub(channel, callOptions);
        }
      };
    return SenseBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SenseFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SenseFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SenseFutureStub>() {
        @Override
        public SenseFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SenseFutureStub(channel, callOptions);
        }
      };
    return SenseFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class SenseImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<Request> sense(
        io.grpc.stub.StreamObserver<Response> responseObserver) {
      return asyncUnimplementedStreamingCall(getSenseMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<RequestStream> senseStream(
        io.grpc.stub.StreamObserver<Response> responseObserver) {
      return asyncUnimplementedStreamingCall(getSenseStreamMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSenseMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                Request,
                Response>(
                  this, METHODID_SENSE)))
          .addMethod(
            getSenseStreamMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                RequestStream,
                Response>(
                  this, METHODID_SENSE_STREAM)))
          .build();
    }
  }

  /**
   */
  public static final class SenseStub extends io.grpc.stub.AbstractAsyncStub<SenseStub> {
    private SenseStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SenseStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SenseStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<Request> sense(
        io.grpc.stub.StreamObserver<Response> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSenseMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<RequestStream> senseStream(
        io.grpc.stub.StreamObserver<Response> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getSenseStreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class SenseBlockingStub extends io.grpc.stub.AbstractBlockingStub<SenseBlockingStub> {
    private SenseBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SenseBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SenseBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class SenseFutureStub extends io.grpc.stub.AbstractFutureStub<SenseFutureStub> {
    private SenseFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SenseFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SenseFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SENSE = 0;
  private static final int METHODID_SENSE_STREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SenseImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SenseImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SENSE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sense(
              (io.grpc.stub.StreamObserver<Response>) responseObserver);
        case METHODID_SENSE_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.senseStream(
              (io.grpc.stub.StreamObserver<Response>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SenseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getSenseMethod())
              .addMethod(getSenseStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
