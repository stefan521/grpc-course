package com.github.stefan521.grpc.greeting.server;

import com.proto.greet.SumRequest;
import com.proto.greet.SumResponse;
import com.proto.greet.SumServiceGrpc;
import io.grpc.stub.StreamObserver;

public class SumServiceImpl extends SumServiceGrpc.SumServiceImplBase {
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
}
