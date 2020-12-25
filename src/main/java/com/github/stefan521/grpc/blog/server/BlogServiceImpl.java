package com.github.stefan521.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {
    static final String author = "author_id";
    static final String content = "content";
    static final String title = "title";

    private final MongoClient mongoClient;
    private MongoCollection<Document> collection; // tables are called collections in Mongo
    private final static Logger logger = Logger.getLogger(BlogServiceImpl.class.getName());

    public BlogServiceImpl(MongoClient mongoClientIn) {
        mongoClient = mongoClientIn;
        setUp();
    }

    public BlogServiceImpl() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        setUp();
    }

    private void setUp() {
        MongoDatabase mongoDatabase = mongoClient.getDatabase("myDb");
        collection = mongoDatabase.getCollection("blog");
    }

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        logger.info("Received Create Blog Request");

        Blog blog = request.getBlog();

        Document doc = new Document(author, blog.getAuthorId())
                .append(title, blog.getTitle())
                .append(content, blog.getContent());

        logger.info("Inserting Blog");

        collection.insertOne(doc);
        // mongoDb generates this when we insert the doc
        String id = doc.getObjectId("_id").toString();

        logger.info("Inserted Blog " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {

        String blogId = request.getBlogId();

        logger.info("Searching for blog with id " + blogId);

        Document document = null;

        try {
            document = collection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        logger.info("Searched for blog with id " + blogId + " result: " + document);

        if (document != null) {
            Blog blog = Blog.newBuilder()
                    .setId(blogId)
                    .setAuthorId(document.getString(author))
                    .setContent(document.getString(content))
                    .setTitle(document.getString(title))
                    .build();

            responseObserver.onNext(
                    ReadBlogResponse.newBuilder()
                            .setBlog(blog)
                            .build()
            );
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .augmentDescription("Blog " + blogId + " does not exist")
                            .asRuntimeException()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        Blog updatedBlog = request.getBlog();
        String blogId = updatedBlog.getId();

        Document document = new Document()
                .append(title, updatedBlog.getTitle())
                .append(author, updatedBlog.getAuthorId())
                .append(content, updatedBlog.getContent());

        try {
            collection.findOneAndReplace(
                    eq("_id", new ObjectId(blogId)),
                    document
            );
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        responseObserver.onNext(UpdateBlogResponse.newBuilder()
                .setBlog(updatedBlog)
                .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        DeleteResult result = null;

        try {
            result = collection.deleteOne(eq("_id", new ObjectId(request.getBlogId())));
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result != null && result.getDeletedCount() == 0) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .augmentDescription("Did not find any blog with id " + request.getBlogId())
                            .asRuntimeException()
            );
        } else {
            responseObserver.onNext(
                    DeleteBlogResponse.newBuilder()
                            .setBlogId(request.getBlogId())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(ListBlogsRequest request, StreamObserver<ListBlogsResponse> responseObserver) {
        logger.info("Received List Blogs Request");

        collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
                ListBlogsResponse.newBuilder().setBlog(documentToBlog(document)).build()
        ));

        responseObserver.onCompleted();
    }

    private Blog documentToBlog(Document document) {
        return Blog.newBuilder()
                .setTitle(document.getString(title))
                .setAuthorId(document.getString(author))
                .setContent(document.getString(content))
                .setId(document.getObjectId("_id").toString())
                .build();
    }
}
