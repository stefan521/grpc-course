package com.github.stefan521.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class GreetingClient {

    protected ManagedChannel channel;

    public GreetingClient() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() // not something to use in production, disables SSL
                .build();
    }

    public GreetingClient(ManagedChannel managedChannel) {
        channel = managedChannel;
    }

    private void run() {
        doUnaryCallWithDeadline("Stefan");

        channel.shutdown();
    }

    public String greet(String name) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                            .setFirstName(name)
                )
                .build();

        GreetResponse response = greetClient.greet(greetRequest);

        return response.getResult();
    }

    public void doServerStreamingCall(String name) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName(name).build())
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest).forEachRemaining(greetManyTimesResponse ->
            System.out.println(greetManyTimesResponse.getResult())
        );
    }

    public void doClientStreamingCall(List<String> names) {
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

        names.forEach(name ->
                requestObserver.onNext(LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName(name)
                                .build())
                        .build())
        );

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doBiDiStreamingCall(List<String> names) {
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

        names.forEach(
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

    public void doUnaryCallWithDeadline(String name) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // Should work.
        try {
            GreetWithDeadlineResponse response = blockingStub
                    .withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder().setFirstName(name).build())
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
                                    .setGreeting(Greeting.newBuilder().setFirstName(name).build())
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
