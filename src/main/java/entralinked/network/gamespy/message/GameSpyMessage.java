package entralinked.network.gamespy.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameSpyMessage {
    
    public String name();
    public String value() default ""; // Still not sure what purpose this serves..
}
