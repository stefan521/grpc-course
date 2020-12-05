package com.github.stefan521.grpc.calculator.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class CalculatorClient {
    ManagedChannel channel;

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext() // turns off SSL
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

        System.out.println("Shutting down server");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest request = SumRequest.newBuilder()
                .setFirstOperand(10)
                .setSecondOperand(39)
                .build();

        SumResponse response = calculatorClient.sum(request);

        System.out.println("We got the sum result of " + response);
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);
        long numberToDecompose = 9941241298521L;

        DecomposeIntoPrimesRequest decomposeIntoPrimesRequest = DecomposeIntoPrimesRequest.newBuilder()
                .setNumber(numberToDecompose)
                .build();

        calculatorClient.decomposeIntoPrimes(decomposeIntoPrimesRequest).forEachRemaining(primeFactor ->
                System.out.println("Next prime factor of " + numberToDecompose + " is " + primeFactor)
        );
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub calculatorClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<AverageIntegersRequest> requestStreamObserver = calculatorClient.averageIntegers(new StreamObserver<AverageIntegersResponse>() {
            @Override
            public void onNext(AverageIntegersResponse value) {
                System.out.println("Got the number average: " + value.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
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

    private void doBiDiStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub client = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<MaxIntegersRequest> requestStream = client.maxInteger(new StreamObserver<MaxIntegersResponse>() {
            @Override
            public void onNext(MaxIntegersResponse value) {
                System.out.println("Server replied with new max " + value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done");
                latch.countDown();
            }
        });

        Arrays.asList(1, 5, 3, 6, 2, 20).forEach(num -> requestStream.onNext(MaxIntegersRequest.newBuilder()
                .setNumber(num)
                .build())
        );

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
