package com.github.stefan521.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class SumServer {
    public static void main(String[] args) throws InterruptedException, IOException {

        Server server = ServerBuilder.forPort(50052)
                .addService(new SumServiceImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Shutting down Sum server");

            server.shutdown();
        }));

        server.start();

        server.awaitTermination();
    }
}
