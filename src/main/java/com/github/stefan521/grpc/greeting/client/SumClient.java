package com.github.stefan521.grpc.greeting.client;

import com.proto.greet.SumRequest;
import com.proto.greet.SumResponse;
import com.proto.greet.SumServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class SumClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        SumServiceGrpc.SumServiceBlockingStub sumClient = SumServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstOperand(10)
                .setSecondOperand(39)
                .build();

        SumResponse response = sumClient.sum(request);

        System.out.println("We got the sum result of " + response);

        channel.shutdown();
    }
}
