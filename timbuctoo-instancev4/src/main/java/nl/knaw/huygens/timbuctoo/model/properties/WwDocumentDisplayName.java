package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.io.IOException;
import java.util.Collection;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class WwDocumentDisplayName extends TimbuctooProperty {
  public WwDocumentDisplayName() {
    super(() ->
        __.as("doc").union(
          new PropertyOrDefault(localProperty("wwdocument_date", datable), jsn("")).get().get()
        ).as("date")
        .select("doc").union(
          new PropertyOrDefault(localProperty("wwdocument_title"), jsn("")).get().get()
        ).as("title")
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

  //cannot be set. Don't show in gui
  @Override
  public String getGuiTypeId() {
    return null;
  }

  @Override
  public Collection<String> getOptions() {
    return null;
  }
}
