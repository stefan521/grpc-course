package com.github.stefan521.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

public class BlogServer {

    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(50051)
                .addService(new BlogServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Shutting down Sum server");

            server.shutdown();
        }));

        try {
            server.start();
            server.awaitTermination();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
