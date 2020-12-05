package com.github.stefan521.grpc.calculator.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

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
}
