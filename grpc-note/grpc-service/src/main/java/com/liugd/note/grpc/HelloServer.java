package com.liugd.note.grpc;

import com.liugd.note.grpcs.lib.HelloReply;
import com.liugd.note.grpcs.lib.HelloRequest;
import com.liugd.note.grpcs.lib.MyServiceGrpc;
import com.liugd.note.grpcs.lib.MyServiceGrpc.MyServiceImplBase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public class HelloServer {

    public static void main(String[] args) throws IOException, InterruptedException {


        HelloServer helloServer = new HelloServer();
        helloServer.start();

        helloServer.blockUntilShutdown();


    }

    private Server server ;

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new HelloServerImpl())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // jvm关闭前执行
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                HelloServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            System.err.println("*** server shut down");

        }));

    }

    private void stop() throws InterruptedException {
        if (server != null)
            server.shutdown().awaitTermination(30, TimeUnit.MILLISECONDS);
    }

    /**
     * 阻塞等待主线程终止
     * @throws InterruptedException
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    class HelloServerImpl extends MyServiceImplBase {


        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

            System.out.println(request.getName());
            HelloReply helloReply = HelloReply.newBuilder().setMessage("测试01").build();
            responseObserver.onNext(helloReply);
            responseObserver.onCompleted();
        }
    }

}
