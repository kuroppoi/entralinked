package entralinked;

import java.util.Collection;
import java.util.List;

public record CommandLineArguments(boolean disableGui) {
    
    public CommandLineArguments(Collection<String> args) {
        this(args.contains("disablegui"));
    }
    
    public CommandLineArguments(String... args) {
        this(List.of(args));
    }
}
