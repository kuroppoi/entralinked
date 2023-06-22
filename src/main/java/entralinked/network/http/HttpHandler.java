package entralinked.network.http;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.servlet.JavalinServletContext;

public interface HttpHandler {
    
    public void addHandlers(Javalin javalin);
    
    public default void configureJavalin(JavalinConfig config) {}
    public default void clearTasks(Context ctx) {
        ((JavalinServletContext)ctx).getTasks().clear();
    }
}
