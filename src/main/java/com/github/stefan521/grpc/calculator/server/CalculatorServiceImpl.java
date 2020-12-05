package com.github.stefan521.grpc.calculator.server;

import com.proto.greet.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        int firstOperand = request.getFirstOperand();
        int secondOperand = request.getSecondOperand();

        int result = firstOperand + secondOperand;

        SumResponse response = SumResponse.newBuilder()
                .setSumResult(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void decomposeIntoPrimes(DecomposeIntoPrimesRequest request, StreamObserver<DecomposeIntoPrimesResponse> responseObserver) {
        long numberToDecompose = request.getNumber();
        long divisor = 2L;

        while (numberToDecompose > 1) {
            if (numberToDecompose % divisor == 0) {
                DecomposeIntoPrimesResponse response = DecomposeIntoPrimesResponse.newBuilder()
                        .setPrimeFactor(divisor)
                        .build() ;

                responseObserver.onNext(response);
                numberToDecompose /= divisor;
            } else {
                divisor += 1L;
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AverageIntegersRequest> averageIntegers(StreamObserver<AverageIntegersResponse> responseObserver) {
        return new StreamObserver<AverageIntegersRequest>() {
            ArrayList<Integer> numbers = new ArrayList<>(); // keep state in the StreamObserver, not outside

            @Override
            public void onNext(AverageIntegersRequest value) {
                numbers.add(value.getNumber());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                int total = numbers.stream().mapToInt(num -> num).sum();
                double average = (double) total / numbers.size();

                responseObserver.onNext(
                        AverageIntegersResponse.newBuilder()
                                .setAverage(average)
                                .build()
                );

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<MaxIntegersRequest> maxInteger(StreamObserver<MaxIntegersResponse> responseObserver) {
        return new StreamObserver<MaxIntegersRequest>() {
            int currentMax = Integer.MIN_VALUE;

            @Override
            public void onNext(MaxIntegersRequest value) {
                int nextValue = value.getNumber();

                if ( nextValue > currentMax) {
                    currentMax = nextValue;
                    responseObserver.onNext(MaxIntegersResponse.newBuilder()
                            .setMaximum(currentMax)
                            .build());
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNumber();

        if (number >= 0) {
            double numberRoot = Math.sqrt(number);

            responseObserver.onNext(
                    SquareRootResponse.newBuilder()
                            .setNumberRoot(numberRoot)
                            .build()
            );
        } else {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                    .withDescription("Received negative number")
                            .augmentDescription("Number sent: " + number)
                    .asRuntimeException()
            );
        }
    }
}
