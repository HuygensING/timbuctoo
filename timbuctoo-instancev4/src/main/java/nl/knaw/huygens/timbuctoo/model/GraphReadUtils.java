package nl.knaw.huygens.timbuctoo.model;

import javaslang.Value;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.Iterator;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.arrayToEncodedArray;

//Most database access should probably be done through the tinkerpopjsoncrudservice
public class GraphReadUtils {

  public static final String TYPES_PROP = "types";

  public static Optional<Try<String[]>> getEntityTypes(Element element) {
    return getProp(element, TYPES_PROP, String.class)
      .map(encodedEntityTypes ->
        Try.of(() -> arrayToEncodedArray.tinkerpopToJava(encodedEntityTypes, String[].class)
      ));
  }

  public static String[] getEntityTypesOrDefault(Element element) {
    return getEntityTypes(element).flatMap(Value::toJavaOptional).orElse(new String[]{});
  }

  public static <V> Optional<V> getProp(final Element vertex, final String key, Class<? extends V> clazz) {
    try {
      Iterator<? extends Property<Object>> revProp = vertex.properties(key);
      if (revProp.hasNext()) {
        return Optional.of(clazz.cast(revProp.next().value()));
      } else {
        return Optional.empty();
      }
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

}
