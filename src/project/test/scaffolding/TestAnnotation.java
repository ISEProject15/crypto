package project.test.scaffolding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface TestAnnotation {
    String description();
}