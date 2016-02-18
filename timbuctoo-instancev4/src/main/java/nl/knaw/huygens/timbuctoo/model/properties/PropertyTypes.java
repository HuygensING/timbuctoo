package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.function.Supplier;

import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

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

  public static TimbuctooProperty defaultNameInFullProperty(String propName) {
    return new TimbuctooProperty(
      () -> __.<Object, String>values(propName)
        .map(prop -> Try.of(() -> jsn(Converters.personNames.tinkerpopToJava(prop.get()).defaultName().getFullName()))),
      (value) -> {
        throw new IOException("Displayname cannot be set, set the 'names' property instead.");
      }
    );
  }

  public static TimbuctooProperty wwPersonNameOrTempName() {
    return new TimbuctooProperty(
      () -> __.<Object, Try<JsonNode>>coalesce(
        defaultNameInFullProperty("wwperson_names").get().get(),
        localProperty("wwperson_tempName").get().get()
      ),
      (value) -> {
        throw new IOException("Displayname cannot be set, set the 'names' property instead.");
      }
    );
  }

  public static TimbuctooProperty wwdocumentDisplayNameProperty() {

    Supplier<GraphTraversal<?, Try<JsonNode>>> dateGetter = localProperty("wwdocument_date", datable).get();
    Supplier<GraphTraversal<?, Try<JsonNode>>> titleGetter = localProperty("wwdocument_title").get();


    return new TimbuctooProperty(
      () -> __.as("doc")
        .coalesce(dateGetter.get(), __.map(x -> Try.success(jsn("")))).as("date").select("doc")
        .coalesce(titleGetter.get(), __.map(x -> Try.success(jsn("")))).as("title")
        .select("title", "date")
        .map(x -> {
          Try<JsonNode> date = (Try<JsonNode>) x.get().get("date");
          Try<JsonNode> title = (Try<JsonNode>) x.get().get("title");
          return Try.success(jsn(
            title.getOrElse(jsn("")).asText() +
              " (" + date.getOrElse(jsn("<date>")).asText() + ")"
          ));
        }),
      (value) -> {
        throw new IOException("Displayname cannot be set, set the 'names' property instead.");
      }
    );
  }
}
