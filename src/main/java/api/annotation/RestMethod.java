package api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RestMethod {

    /**
     * @return Pattern representing the URL path that matches the method
     */
    String pathPattern() default "";

    /**
     * @return Type of method (GET, POST, PUT, DELETE)
     */
    MethodType methodType();

}
