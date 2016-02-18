package nl.knaw.huygens.timbuctoo.model.properties;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class PropertyTypes {

  public static TimbuctooProperty localProperty(String propName) {
    return localProperty(propName, Converters.stringToString);
  }

  public static TimbuctooProperty localProperty(String propName, Converter converter) {
    return new TimbuctooProperty(
      () -> __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))),
      (value) -> {
        if (value == null) {
          return __.sideEffect(vertex -> ((Vertex) vertex.get()).property(propName).remove());
        } else {
          return __.property(propName, converter.jsonToTinkerpop(value));
        }
      }
    );
  }
  //
  //public static TimbuctooProperty wwpersonDisplayNameProperty() {
  //  //use names parser and take defaultName().getShortName()
  //  //if name is empty ot not found return [TEMP] + tempName
  //}
  //
  //public static TimbuctooProperty wwdocumentDisplayNameProperty() {
  //
  //  GraphTraversal<?, Try<JsonNode>> wwpersonGetter = wwpersonDisplayNameProperty().get();
  //
  //  __.as("doc")
  //    .local(__.in("is_creator_of").union(wwpersonGetter).fold().map(x->""))
  //    .select("doc")
  //    .values("wwdocuments_title").map(x->"").select("doc")
  //    .values("wwdocuments_date").map(x->"").select("doc"); //make datable and grab year
  //
  //  return new TimbuctooProperty(
  //    __.<Object, String>values(propName).map(prop -> Try.of(() -> converter.tinkerpopToJson(prop.get()))),
  //    (value) -> __.property(propName, converter.jsonToTinkerpop(value))
  //  );
  //}
}
