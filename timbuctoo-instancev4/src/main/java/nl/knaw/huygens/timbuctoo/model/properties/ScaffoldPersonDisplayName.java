package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javaslang.control.Try;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

class ScaffoldPersonDisplayName extends ReadableProperty {
  private static final List<String> NAME_PARTS = Lists.newArrayList(
    "preposition",
    "intraposition",
    "givenName",
    "familyName",
    "postposition"
  );

  public static final String TYPE = "scaffold-person-displayname";

  ScaffoldPersonDisplayName(String prefix) {
    super(() ->
      __.map(x -> {
        final Map<String, String> nameParts = Maps.newHashMap();
        final Vertex vertex = (Vertex) x.get();
        vertex.properties().forEachRemaining(prop -> {
          for (String namePart : NAME_PARTS) {
            if (prop.key().endsWith(namePart) && prop.key().startsWith(prefix)) {
              nameParts.put(namePart, (String) prop.value());
            }
          }
        });

        final List<String> names = NAME_PARTS
          .stream()
          .map(part -> nameParts.containsKey(part) ? nameParts.get(part) : "")
          .filter(part -> part.length() > 0)
          .collect(Collectors.toList());

        return Try.success((JsonNode) jsn(String.join(" ", names)));
      }),
      () ->
        __.map(x -> {
          final Map<String, String> nameParts = Maps.newHashMap();
          final Vertex vertex = (Vertex) x.get();
          vertex.properties().forEachRemaining(prop -> {
            for (String namePart : NAME_PARTS) {
              if (prop.key().endsWith(namePart) && prop.key().startsWith(prefix)) {
                nameParts.put(namePart, (String) prop.value());
              }
            }
          });

          final List<String> names = NAME_PARTS
            .stream()
            .map(part -> nameParts.containsKey(part) ? nameParts.get(part) : "")
            .filter(part -> part.length() > 0)
            .collect(Collectors.toList());

          return Try.success((Object) String.join(" ", names));
        })
    );
  }


  @Override
  public String getTypeId() {
    return TYPE;
  }

  @Override
  public String getUniqueTypeId() {
    return TYPE;
  }
}
