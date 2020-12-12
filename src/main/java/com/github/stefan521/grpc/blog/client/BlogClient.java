package com.github.stefan521.grpc.blog.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    ManagedChannel channel;

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
    }

    public static void main(String[] args) {
        BlogClient client = new BlogClient();
        client.run();
    }
}
