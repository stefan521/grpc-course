package greeting.server;

import com.github.stefan521.grpc.greeting.server.GreetServiceImpl;
import com.proto.greet.*;
import io.grpc.Deadline;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class GreetingServerTest {

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public String registerServerName() {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        try {
            // Create a server, add service, start, and register for automatic graceful shutdown.
            grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                    .directExecutor()
                    .addService(new GreetServiceImpl()).build().start()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverName;
    }

    public GreetServiceGrpc.GreetServiceStub asyncStub() {
        String serverName = registerServerName();

        return GreetServiceGrpc.newStub(
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    public GreetServiceGrpc.GreetServiceBlockingStub blockingStub() {
        String serverName = registerServerName();

        return GreetServiceGrpc.newBlockingStub(
            // Create a client channel and register for automatic graceful shutdown.
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }


    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void greeterImpl_greet() {
        String name = "Arrrrnold";

        GreetResponse reply = blockingStub().greet(
                GreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        );

        assertEquals("Hello " + name, reply.getResult());
    }

    @Test
    public void greeterImpl_greetManyTimes() {
        String name = "Stefan";
        String expectedResponsePrefix = "Hello " + name + ", response number: ";
        AtomicReference<Integer> repliesCount = new AtomicReference<>(0);

        Iterator<GreetManyTimesResponse> reply = blockingStub().greetManyTimes(
                GreetManyTimesRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        );

        reply.forEachRemaining(greetManyTimesResponse -> {
            assertEquals(expectedResponsePrefix + repliesCount.get(), greetManyTimesResponse.getResult());
            repliesCount.updateAndGet(v -> v + 1);
        });
        assertEquals(10, repliesCount.get().intValue());
    }

    @Test
    public void greeterImpl_longGreet() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<String> names = new ArrayList<>();
        names.add("stefan");
        names.add("vladimir");
        names.add("josh");

        StreamObserver<LongGreetRequest> requestStream = asyncStub().longGreet(
            new StreamObserver<LongGreetResponse>() {
                @Override
                public void onNext(LongGreetResponse greetResponse) {
                    names.forEach(name ->
                        assertTrue(greetResponse.getResult().contains(name))
                    );
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    latch.countDown();
                }
            }
        );

        names.forEach(name -> requestStream.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        ));

        requestStream.onCompleted();

        latch.await();
    }

    @Test
    public void greeterImpl_greetEveryone() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Integer> names = new HashMap<>();
        names.put("stefan", 0);
        names.put("vladimir", 0);
        names.put("josh", 0);

        StreamObserver<GreetEveryoneRequest> requestStream = asyncStub().greetEveryone(
                new StreamObserver<GreetEveryoneResponse>() {
                    @Override
                    public void onNext(GreetEveryoneResponse greetResponse) {
                        names.keySet().forEach(name -> {
                                if (greetResponse.getResult().contains(name))
                                    names.put(name, names.get(name) + 1);
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                }
        );

        names.keySet().forEach(name -> requestStream.onNext(
                GreetEveryoneRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        ));

        requestStream.onNext(
                GreetEveryoneRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName("stefan"))
                        .build()
        );

        requestStream.onCompleted();
        latch.await();

        assertEquals(2, names.get("stefan").intValue());
        assertEquals(1, names.get("josh").intValue());
        assertEquals(1, names.get("vladimir").intValue());
    }

    @Test
    public void greeterImpl_greetWithDeadlineSuccess() {
        GreetWithDeadlineResponse response = blockingStub()
                .withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                .greetWithDeadline(
                    GreetWithDeadlineRequest
                            .newBuilder()
                            .setGreeting(Greeting
                                    .newBuilder()
                                    .setFirstName("stefan")
                                    .build()
                            )
                            .build()
                );

        assertEquals("Hello stefan", response.getResult());
    }

    @Test
    public void greeterImpl_greetWithDeadlineFailure() {
        exception.expect(StatusRuntimeException.class);
        exception.expectMessage("DEADLINE_EXCEEDED");

        blockingStub()
                .withDeadline(Deadline.after(0, TimeUnit.MILLISECONDS))
                .greetWithDeadline(GreetWithDeadlineRequest.getDefaultInstance());
    }
}
