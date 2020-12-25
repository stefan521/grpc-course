package com.github.stefan521.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class GreetingServer {
    private final static Logger logger = Logger.getLogger(GreetingServer.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Hello gRPC");

        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime()
            .addShutdownHook(new Thread( () -> {
                logger.info("Received shutdown request");
                server.shutdown();
                logger.info("Successfully stopped the server");
            }));

        server.awaitTermination(); // blocks main thread until server terminates
    }
}
