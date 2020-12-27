package com.github.stefan521.grpc.blog.client;

import com.github.stefan521.grpc.blog.server.BlogServiceImpl;
import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlogClient {
    ManagedChannel channel;
    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    private void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

//        createBlog(channel);

//        readBlog(channel, "5fd542c59b7c0e6fd3177f02");
//        readBlog(channel, "fake-blog-id");

//        readBlog(channel, "5fd542df9b7c0e6fd3177f03");
//        updateBlog(channel, "5fd542df9b7c0e6fd3177f03");
//        readBlog(channel, "5fd542df9b7c0e6fd3177f03");

//        deleteBlog(channel, "5fd542c59b7c0e6fd3177f02");

        listBlogs(channel);
    }

    private void createBlog(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        CreateBlogResponse response = blogClient.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(
                        Blog.newBuilder()
                                .setTitle("Cooking Tips")
                                .setAuthorId("Chef Gordon")
                                .setContent("Don't cook bland food.")
                                .build()
                )
                .build());

        logger.info("Created Blog Response " + response);
    }

    private void readBlog(ManagedChannel channel, String blogId) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        ReadBlogResponse response = blogClient.readBlog(
                ReadBlogRequest.newBuilder()
                        .setBlogId(blogId)
                        .build()
        );

        logger.info("Read Blog Response " + response);
    }

    private void listBlogs(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        blogClient.listBlogs(ListBlogsRequest.newBuilder().build()).forEachRemaining(
                listBlogsResponse -> logger.info(listBlogsResponse.getBlog().toString())
        );
    }

    private void deleteBlog(ManagedChannel channel, String blogId) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        DeleteBlogResponse response = blogClient.deleteBlog(
                DeleteBlogRequest.newBuilder()
                        .setBlogId(blogId)
                        .build()
        );

        logger.info("Delete Blog Response " + response);
    }

    private void updateBlog(ManagedChannel channel, String blogId) {
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        UpdateBlogResponse response = blogClient.updateBlog(
                UpdateBlogRequest.newBuilder()
                        .setBlog(
                                Blog.newBuilder()
                                        .setId(blogId)
                                        .setAuthorId("Chef Mateo")
                                        .setContent("Gordon swears too much")
                                        .setTitle("Culinary Diplomacy")
                                        .build()
                        )
                        .build()
        );

        logger.info("Update Blog Response " + response);
    }

    public static void main(String[] args) {
        logger.info("Hello! I'm a Blog gRPC client");

        BlogClient client = new BlogClient();
        client.run();
    }
}
