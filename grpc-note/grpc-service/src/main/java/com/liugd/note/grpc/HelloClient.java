package com.liugd.note.grpc;


import com.liugd.note.grpcs.lib.HelloReply;
import com.liugd.note.grpcs.lib.HelloRequest;
import com.liugd.note.grpcs.lib.MyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class HelloClient {

    /**
     * 远程连接管理器，管理连接的生命周期
     */
    private final ManagedChannel channel;
    /**
     * 远程服务存根
     */
    private final MyServiceGrpc.MyServiceBlockingStub blockingStub;

    public HelloClient(String host, int port) {
        // 初始化连接
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        // 初始化远程服务存根
        blockingStub = MyServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String sayHello(String name) {
        // 构造服务调用参数对象
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        // 调用远程服务方法
        HelloReply response = blockingStub.sayHello(request);
        return response.getMessage();
    }

    public static void main(String[] args) throws InterruptedException {
        HelloClient client = new HelloClient("127.0.0.1", 50051);
        String content = client.sayHello("Java client");
        System.out.println(content);
        client.shutdown();
    }
}