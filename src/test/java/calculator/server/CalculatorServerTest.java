package calculator.server;

import com.github.stefan521.grpc.calculator.server.CalculatorServiceImpl;
import com.proto.greet.*;
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

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class CalculatorServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public String setUp() {
        String serverName = InProcessServerBuilder.generateName();

        try {
            grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                    .directExecutor()
                    .addService(new CalculatorServiceImpl()).build().start()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverName;
    }

    public CalculatorServiceGrpc.CalculatorServiceStub asyncStub() {
        String serverName = setUp();

        return CalculatorServiceGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    public CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub() {
        String serverName = setUp();

        return CalculatorServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    @Test
    public void sumTest() {
        SumRequest sumRequest = SumRequest.newBuilder()
                .setFirstOperand(10)
                .setSecondOperand(91)
                .build();

        SumResponse sumResponse = blockingStub().sum(sumRequest);

        assertEquals(101, sumResponse.getSumResult());
    }

    @Test
    public void decomposeIntoPrimesTest() {
        List<Long> expectedFactors = new ArrayList<>(Arrays.asList(2L, 3L, 3L));
        List<Long> factors = new ArrayList<>();
        DecomposeIntoPrimesRequest req = DecomposeIntoPrimesRequest.newBuilder().setNumber(18).build();

        Iterator<DecomposeIntoPrimesResponse> response = blockingStub().decomposeIntoPrimes(req);

        response.forEachRemaining(factor -> factors.add(factor.getPrimeFactor()));
        Collections.sort(factors);

        assertEquals(expectedFactors, factors);
    }

    @Test
    public void squareRootTestSuccess() {
        SquareRootRequest request = SquareRootRequest.newBuilder().setNumber(81).build();

        SquareRootResponse response = blockingStub().squareRoot(request);

        assertEquals(9, response.getNumberRoot(), 0.01);
    }

    @Test
    public void squareRootTestFail() {
        SquareRootRequest request = SquareRootRequest.newBuilder().setNumber(-81).build();

        exception.expect(StatusRuntimeException.class);
        exception.expectMessage("INVALID_ARGUMENT: Received negative number");

        blockingStub().squareRoot(request);
    }

    @Test
    public void maxIntegerTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // need to wait for async test to complete.
        List<Integer> actualMaxes = new ArrayList<>();
        List<Integer> expectedMaxes = new ArrayList<>(Arrays.asList(10, 21, 109));
        List<Integer> numbers = new ArrayList<>(Arrays.asList(10, 21, 12, -1, 109, 28));

        StreamObserver<MaxIntegersRequest> responseStream = asyncStub().maxInteger(new StreamObserver<MaxIntegersResponse>() {
            @Override
            public void onNext(MaxIntegersResponse response) {
                actualMaxes.add(response.getMaximum());
            }

            @Override
            public void onError(Throwable err) {
                err.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                assertEquals(expectedMaxes, actualMaxes);
                latch.countDown();
            }
        });

        numbers.forEach(number -> responseStream.onNext(
                MaxIntegersRequest.newBuilder().setNumber(number).build()
        ));

        responseStream.onCompleted();

        latch.await();
    }

    @Test
    public void averageIntegersTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Integer> numbers = new ArrayList<>(Arrays.asList(6, 10, 4, 2));
        final ArrayList<Double> responses = new ArrayList<>();

        StreamObserver<AverageIntegersRequest> requestObserver = asyncStub().averageIntegers(
                new StreamObserver<AverageIntegersResponse>() {
                    @Override
                    public void onNext(AverageIntegersResponse value) {
                        responses.add(value.getAverage());
                    }

                    @Override
                    public void onError(Throwable err) {
                        err.printStackTrace();
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                }
        );

        numbers.forEach(number -> requestObserver.onNext(
                AverageIntegersRequest.newBuilder().setNumber(number).build()
        ));

        requestObserver.onCompleted();
        latch.await();
        assertEquals(1, responses.size());
        assertEquals(5.5, responses.get(0), 0.01);
    }

}
