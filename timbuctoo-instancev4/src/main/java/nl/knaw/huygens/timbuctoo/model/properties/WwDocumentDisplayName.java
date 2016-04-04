package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class WwDocumentDisplayName extends ReadableProperty {
  public WwDocumentDisplayName() {
    super(() ->
      __.as("doc").union(
        new PropertyOrDefault(localProperty("wwdocument_date", datable), jsn("")).get()
      ).as("date")
      .select("doc").union(
        new PropertyOrDefault(localProperty("wwdocument_title"), jsn("")).get()
      ).as("title")
      .select("title", "date")
      .map(x -> {
        Try<JsonNode> date = (Try<JsonNode>) x.get().get("date");
        Try<JsonNode> title = (Try<JsonNode>) x.get().get("title");
        return Try.success(jsn(
          title.getOrElse(jsn("")).asText() +
            " (" + date.getOrElse(jsn("<date>")).asText() + ")"
        ));
      })
    );
  }
}
