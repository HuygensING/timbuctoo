package nl.knaw.huygens.repository.indexdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexAnnotation {
  String[] accessors() default {};
  String fieldName() default "";
  Class<? extends CustomIndexer> customIndexer() default CustomIndexer.NoopIndexer.class;
  boolean isFaceted() default false;
  boolean isComplex() default false;
  boolean canBeEmpty() default false;
}
