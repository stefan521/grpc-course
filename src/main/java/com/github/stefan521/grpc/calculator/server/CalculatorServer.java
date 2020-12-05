package com.github.stefan521.grpc.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws InterruptedException, IOException {

        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Shutting down Sum server");

            server.shutdown();
        }));

        server.start();

        server.awaitTermination();
    }
}
