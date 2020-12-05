package com.github.stefan521.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GreetingClient {

    ManagedChannel channel ;

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // not something to use in production, disables SSL
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreamingCall(channel);
//        doBiDiStreamingCall(channel);
        doUnaryCallWithDeadline(channel);

        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("stefan")
                .setLastName("521")
                .build();

        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = greetClient.greet(greetRequest);

        System.out.println("Got Greet Response " + response);
        System.out.println("Shutting down channel");
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Stefan").build())
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver =  asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                            .setFirstName("Stefan")
                            .build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("John")
                        .build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Marc")
                        .build())
                .build());

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        CountDownLatch latch = new CountDownLatch(1);
        GreetServiceGrpc.GreetServiceStub client = GreetServiceGrpc.newStub(channel);

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = client.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from serer: " + value.getResult());
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

        Arrays.asList("Stephane", "Jhon", "Marc", "Patricia").forEach(
                name -> requestStreamObserver.onNext(GreetEveryoneRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName(name)
                                .build())
                        .build())
        );

        requestStreamObserver.onCompleted();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // Should work.
        try {
            GreetWithDeadlineResponse response = blockingStub
                    .withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder().setFirstName("Stefan").build())
                                    .build()
                    );

            System.out.println("Response: " + response);
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the answer");
            } else {
                e.printStackTrace();
            }
        }

        // Should expire.
        try {
            GreetWithDeadlineResponse response = blockingStub
                    .withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder().setFirstName("Stefan").build())
                                    .build()
                    );

            System.out.println("Response: " + response);
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the answer");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        GreetingClient main = new GreetingClient();
        main.run();
    }
}
