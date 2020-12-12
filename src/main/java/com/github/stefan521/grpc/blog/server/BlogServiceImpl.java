package com.github.stefan521.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("myDb");
    private final MongoCollection<Document> collection = mongoDatabase.getCollection("blog"); // tables are called collections in Mongo

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

    }
}
