package nl.knaw.huygens.repository.indexdata;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexAnnotations {
  IndexAnnotation[] value() default {};
}
