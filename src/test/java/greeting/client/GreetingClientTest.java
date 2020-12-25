package greeting.client;

import com.github.stefan521.grpc.greeting.client.GreetingClient;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class GreetingClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final GreetServiceGrpc.GreetServiceImplBase serviceImpl =
            mock(GreetServiceGrpc.GreetServiceImplBase.class, delegatesTo(
                    new GreetServiceGrpc.GreetServiceImplBase() {
                        // By default the client will receive Status.UNIMPLEMENTED for all RPCs.
                        // You might need to implement necessary behaviors for your test here, like this:
                        //
                         @Override
                         public void greet(GreetRequest request, StreamObserver<GreetResponse> respObserver) {
                           respObserver.onNext(GreetResponse.getDefaultInstance());
                           respObserver.onCompleted();
                         }

                        @Override
                        public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
                             for (int i = 0; i < 5; i ++) {
                                 responseObserver.onNext(
                                         GreetManyTimesResponse
                                                 .newBuilder()
                                                 .setResult(request.getGreeting().getFirstName())
                                                 .build()
                                 );
                             }
                            responseObserver.onCompleted();
                        }
                    }));

    private GreetingClient client;


    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
        client = new GreetingClient(channel);
    }

    /**
     * To test the client, call from the client against the fake server, and verify behaviors or state
     * changes from the server side.
     */
    @Test
    public void greet_unaryCall() {
        String name = "Peter Parker";
        ArgumentCaptor<GreetRequest> requestCaptor = ArgumentCaptor.forClass(GreetRequest.class);

        client.greet(name);

        verify(serviceImpl)
                .greet(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<GreetResponse>>any());
        assertEquals(name, requestCaptor.getValue().getGreeting().getFirstName());
    }

    @Test
    public void greet_serverStreaming() {
        String name = "Padabam";
        ArgumentCaptor<GreetManyTimesRequest> requestCaptor = ArgumentCaptor.forClass(GreetManyTimesRequest.class);

        client.doServerStreamingCall(name);
        verify(serviceImpl).greetManyTimes(requestCaptor.capture(), ArgumentMatchers.any());

        String receivedName = requestCaptor.getValue().getGreeting().getFirstName();
        assertEquals(name, receivedName);
    }
}
