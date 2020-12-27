package com.github.stefan521.grpc.calculator.server;

import com.github.stefan521.grpc.blog.server.BlogServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CalculatorServer {
    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);
    public static void main(String[] args) throws InterruptedException, IOException {

        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Shutting down Sum server");

            server.shutdown();
        }));

        server.start();

        server.awaitTermination();
    }
}
