package ai.cochlear.sense.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.31.1)",
    comments = "Source: SenseClient.proto")
public final class CochlGrpc {

  private CochlGrpc() {}

  public static final String SERVICE_NAME = "sense.full.v1.Cochl";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio,
      ai.cochlear.sense.proto.SenseClient.CochlSense> getSensefileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sensefile",
      requestType = ai.cochlear.sense.proto.SenseClient.Audio.class,
      responseType = ai.cochlear.sense.proto.SenseClient.CochlSense.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio,
      ai.cochlear.sense.proto.SenseClient.CochlSense> getSensefileMethod() {
    io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio, ai.cochlear.sense.proto.SenseClient.CochlSense> getSensefileMethod;
    if ((getSensefileMethod = CochlGrpc.getSensefileMethod) == null) {
      synchronized (CochlGrpc.class) {
        if ((getSensefileMethod = CochlGrpc.getSensefileMethod) == null) {
          CochlGrpc.getSensefileMethod = getSensefileMethod =
              io.grpc.MethodDescriptor.<ai.cochlear.sense.proto.SenseClient.Audio, ai.cochlear.sense.proto.SenseClient.CochlSense>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sensefile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  ai.cochlear.sense.proto.SenseClient.Audio.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  ai.cochlear.sense.proto.SenseClient.CochlSense.getDefaultInstance()))
              .build();
        }
      }
    }
    return getSensefileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio,
      ai.cochlear.sense.proto.SenseClient.CochlSense> getSensestreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "sensestream",
      requestType = ai.cochlear.sense.proto.SenseClient.Audio.class,
      responseType = ai.cochlear.sense.proto.SenseClient.CochlSense.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio,
      ai.cochlear.sense.proto.SenseClient.CochlSense> getSensestreamMethod() {
    io.grpc.MethodDescriptor<ai.cochlear.sense.proto.SenseClient.Audio, ai.cochlear.sense.proto.SenseClient.CochlSense> getSensestreamMethod;
    if ((getSensestreamMethod = CochlGrpc.getSensestreamMethod) == null) {
      synchronized (CochlGrpc.class) {
        if ((getSensestreamMethod = CochlGrpc.getSensestreamMethod) == null) {
          CochlGrpc.getSensestreamMethod = getSensestreamMethod =
              io.grpc.MethodDescriptor.<ai.cochlear.sense.proto.SenseClient.Audio, ai.cochlear.sense.proto.SenseClient.CochlSense>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "sensestream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  ai.cochlear.sense.proto.SenseClient.Audio.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  ai.cochlear.sense.proto.SenseClient.CochlSense.getDefaultInstance()))
              .build();
        }
      }
    }
    return getSensestreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CochlStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CochlStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CochlStub>() {
        @java.lang.Override
        public CochlStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CochlStub(channel, callOptions);
        }
      };
    return CochlStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CochlBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CochlBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CochlBlockingStub>() {
        @java.lang.Override
        public CochlBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CochlBlockingStub(channel, callOptions);
        }
      };
    return CochlBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CochlFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CochlFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CochlFutureStub>() {
        @java.lang.Override
        public CochlFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CochlFutureStub(channel, callOptions);
        }
      };
    return CochlFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class CochlImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.Audio> sensefile(
        io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense> responseObserver) {
      return asyncUnimplementedStreamingCall(getSensefileMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.Audio> sensestream(
        io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense> responseObserver) {
      return asyncUnimplementedStreamingCall(getSensestreamMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSensefileMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                ai.cochlear.sense.proto.SenseClient.Audio,
                ai.cochlear.sense.proto.SenseClient.CochlSense>(
                  this, METHODID_SENSEFILE)))
          .addMethod(
            getSensestreamMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                ai.cochlear.sense.proto.SenseClient.Audio,
                ai.cochlear.sense.proto.SenseClient.CochlSense>(
                  this, METHODID_SENSESTREAM)))
          .build();
    }
  }

  /**
   */
  public static final class CochlStub extends io.grpc.stub.AbstractAsyncStub<CochlStub> {
    private CochlStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CochlStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CochlStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.Audio> sensefile(
        io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSensefileMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.Audio> sensestream(
        io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getSensestreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class CochlBlockingStub extends io.grpc.stub.AbstractBlockingStub<CochlBlockingStub> {
    private CochlBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CochlBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CochlBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class CochlFutureStub extends io.grpc.stub.AbstractFutureStub<CochlFutureStub> {
    private CochlFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CochlFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CochlFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SENSEFILE = 0;
  private static final int METHODID_SENSESTREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CochlImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CochlImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SENSEFILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sensefile(
              (io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense>) responseObserver);
        case METHODID_SENSESTREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sensestream(
              (io.grpc.stub.StreamObserver<ai.cochlear.sense.proto.SenseClient.CochlSense>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CochlGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getSensefileMethod())
              .addMethod(getSensestreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
