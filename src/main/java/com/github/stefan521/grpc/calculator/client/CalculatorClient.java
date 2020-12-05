package com.github.stefan521.grpc.calculator.client;

import com.proto.greet.CalculatorServiceGrpc;
import com.proto.greet.DecomposeIntoPrimesRequest;
import com.proto.greet.SumRequest;
import com.proto.greet.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        // Unary call
//        SumRequest request = SumRequest.newBuilder()
//                .setFirstOperand(10)
//                .setSecondOperand(39)
//                .build();
//
//        SumResponse response = calculatorClient.sum(request);
//
//        System.out.println("We got the sum result of " + response);

        // Server Streaming
        long numberToDecompose = 9941241298521L;
        DecomposeIntoPrimesRequest decomposeIntoPrimesRequest = DecomposeIntoPrimesRequest.newBuilder()
                .setNumber(numberToDecompose)
                .build();

        calculatorClient.decomposeIntoPrimes(decomposeIntoPrimesRequest).forEachRemaining(primeFactor -> {
            System.out.println("Next prime factor of " + numberToDecompose + " is " + primeFactor);
        });

        channel.shutdown();

        // Client Streaming
    }
}
