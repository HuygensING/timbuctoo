package nl.knaw.huygens.timbuctoo.model.properties;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

public class PropertyTypes {

  public static TimbuctooProperty localProperty(String propName) {
    return localProperty(propName, Converters.stringToString);
  }

  public static TimbuctooProperty localProperty(String propName, Converter converter) {
    return new TimbuctooProperty(
      __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))),
      (value) -> __.property(propName, converter.jsonToTinkerpop(value))
    );
  }
}
