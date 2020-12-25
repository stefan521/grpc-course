package blog.server;

import com.github.stefan521.grpc.blog.server.BlogServiceImpl;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.ReadBlogRequest;
import com.proto.blog.ReadBlogResponse;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.delegatesTo;

@RunWith(JUnit4.class)
public class BlogServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    MongoClient mongoClient = mock(MongoClient.class);

    public String setUp() {
        String serverName = InProcessServerBuilder.generateName();

        try {
            grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                    .directExecutor()
                    .addService(new BlogServiceImpl(mongoClient)).build().start()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverName;
    }

    public BlogServiceGrpc.BlogServiceStub asyncStub() {
        String serverName = setUp();

        return BlogServiceGrpc.newStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    public BlogServiceGrpc.BlogServiceBlockingStub blockingStub() {
        String serverName = setUp();

        return BlogServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
        );
    }

    @Test
    public void readBlogSuccessTest() {
        String uuid = "5fd542c59b7c0e6fd3177f02";
        Map<String, String> document = new HashMap<>();
        document.put("title", "How To Cook");
        document.put("author_id", "Pete");
        document.put("content", "With Love");
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);

        MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
        FindIterable<Document> mockDocumentIterable = mock(FindIterable.class);
        Document mockDocument = mock(Document.class, delegatesTo(
                new Document() {
                    @Override
                    public String getString(Object key) {
                        return document.get(key.toString());
                    }
                }
        ));

        when(mongoClient.getDatabase("myDb")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("blog")).thenReturn(mongoCollection);
        when(mongoCollection.find(eq("_id", new ObjectId(uuid)))).thenReturn(mockDocumentIterable);
        when(mockDocumentIterable.first()).thenReturn(mockDocument);

        ReadBlogResponse response = blockingStub().readBlog(ReadBlogRequest.newBuilder().setBlogId(uuid).build());

        assertEquals(document.get("author_id"), response.getBlog().getAuthorId());
        assertEquals(document.get("title"), response.getBlog().getTitle());
        assertEquals(document.get("content"), response.getBlog().getContent());
    }

    @Test
    public void readBlogFailureTest() {
        String uuid = "5fd542c59b7c0e6fd3177f02";
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
        FindIterable<Document> mockDocumentIterable = mock(FindIterable.class);

        when(mongoClient.getDatabase("myDb")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("blog")).thenReturn(mongoCollection);
        when(mongoCollection.find(eq("_id", new ObjectId(uuid)))).thenReturn(mockDocumentIterable);

        exception.expect(StatusRuntimeException.class);
        exception.expectMessage("NOT_FOUND");

        blockingStub().readBlog(ReadBlogRequest.newBuilder().setBlogId(uuid).build());
    }
}
