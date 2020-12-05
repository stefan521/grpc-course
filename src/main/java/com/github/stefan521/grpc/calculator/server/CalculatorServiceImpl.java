package com.github.stefan521.grpc.calculator.server;

import com.proto.greet.*;
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
        ArrayList<Integer> numbers = new ArrayList<>();

        return new StreamObserver<AverageIntegersRequest>() {
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
}
