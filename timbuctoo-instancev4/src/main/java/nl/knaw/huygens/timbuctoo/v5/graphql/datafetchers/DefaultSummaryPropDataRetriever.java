package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;

import java.util.List;
import java.util.Optional;

public class DefaultSummaryPropDataRetriever {
  private final List<SummaryProp> defaultProperties;

  public DefaultSummaryPropDataRetriever(List<SummaryProp> defaultProperties) {
    this.defaultProperties = defaultProperties;
  }

  public Optional<CursorQuad> retrieveDefaultProperty(SubjectReference source, QuadStore quadStore) {
    Optional<CursorQuad> quad = Optional.empty();
    for (SummaryProp prop : defaultProperties) {
      for (String path : prop.getPath()) {
        String uri = quad.isPresent() ? quad.get().getObject() : source.getSubjectUri();
        quad = quadStore.getQuads(uri, path, Direction.OUT, "").findAny();

        if (!quad.isPresent()) {
          break;
        }
      }
      if (quad.isPresent()) {
        break;
      }
    }

    return quad;
  }
}
