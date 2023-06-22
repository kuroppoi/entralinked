package entralinked.utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "ConsumerAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE, 
    printObject = true)
public class ConsumerAppender extends AbstractAppender {
    
    protected static final Map<String, List<Consumer<String>>> consumerMap = new ConcurrentHashMap<>();
    
    protected ConsumerAppender(String name, Filter filter, Layout<? extends Serializable> layout, 
            boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }
    
    @PluginFactory
    public static ConsumerAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new ConsumerAppender(name, filter, layout, ignoreExceptions, null);
    }
    
    public static void addConsumer(String appenderName, Consumer<String> consumer) {
        List<Consumer<String>> consumers = consumerMap.getOrDefault(appenderName, new ArrayList<>());
        consumers.add(consumer);
        consumerMap.putIfAbsent(appenderName, consumers);
    }
    
    @Override
    public void append(LogEvent event) {
        String formattedMessage = getLayout().toSerializable(event).toString();
        List<Consumer<String>> consumers = consumerMap.get(getName());
        
        if(consumers != null) {
            for(Consumer<String> consumer : consumers) {
                consumer.accept(formattedMessage);
            }
        }
    }
}