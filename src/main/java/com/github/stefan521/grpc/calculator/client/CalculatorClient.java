package com.github.stefan521.grpc.calculator.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class CalculatorClient {
    ManagedChannel channel;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext() // turns off SSL
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
        doClientStreamingCall(channel);

        channel.shutdown();
    }

    public void doUnaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstOperand(10)
                .setSecondOperand(39)
                .build();

        SumResponse response = calculatorClient.sum(request);

        System.out.println("We got the sum result of " + response);
    }

    public void doServerStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);
        long numberToDecompose = 9941241298521L;

        DecomposeIntoPrimesRequest decomposeIntoPrimesRequest = DecomposeIntoPrimesRequest.newBuilder()
                .setNumber(numberToDecompose)
                .build();

        calculatorClient.decomposeIntoPrimes(decomposeIntoPrimesRequest).forEachRemaining(primeFactor ->
                System.out.println("Next prime factor of " + numberToDecompose + " is " + primeFactor)
        );
    }

    public void doClientStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub calculatorClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<AverageIntegersRequest> requestStreamObserver = calculatorClient.averageIntegers(new StreamObserver<AverageIntegersResponse>() {
            @Override
            public void onNext(AverageIntegersResponse value) {
                System.out.println("Got the number average: " + value.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (int i = 1; i <= 4; i ++) {
            requestStreamObserver.onNext(AverageIntegersRequest.newBuilder()
                    .setNumber(i)
                    .build()
            );
        }

        requestStreamObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        CalculatorClient main = new CalculatorClient();
        main.run();
    }
}
