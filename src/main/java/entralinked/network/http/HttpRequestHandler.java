package entralinked.network.http;

import java.io.IOException;

import io.javalin.http.Context;

@FunctionalInterface
public interface HttpRequestHandler<T> {
    
    public void process(T request, Context ctx) throws IOException;
}
