package greeting.server;

import com.github.stefan521.grpc.greeting.server.GreetServiceImpl;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

@RunWith(JUnit4.class)
public class GreetingServerTest {

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    public GreetServiceGrpc.GreetServiceBlockingStub setUp() {
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

        return GreetServiceGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    GreetServiceGrpc.GreetServiceBlockingStub blockingStub = setUp();

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void greeterImpl_replyMessage1() {
        String name = "Arrrrnold";

        GreetResponse reply = blockingStub.greet(
                GreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        );

        assertEquals("Hello " + name, reply.getResult());
    }

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void greeterImpl_replyMessage2() {
        String name = "Stefan";

        GreetResponse reply = blockingStub.greet(
                GreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder().setFirstName(name))
                        .build()
        );

        assertEquals("Hello " + name, reply.getResult());
    }
}
