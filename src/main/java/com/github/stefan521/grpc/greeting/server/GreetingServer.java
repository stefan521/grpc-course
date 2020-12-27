package com.github.stefan521.grpc.greeting.server;

import com.github.stefan521.grpc.blog.server.BlogServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GreetingServer {
    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

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
