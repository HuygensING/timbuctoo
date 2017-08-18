package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class LazyTypeSubjectReference implements SubjectReference {
  private final String subjectUri;
  private final QuadStore quadStore;
  private Set<String> types;

  public LazyTypeSubjectReference(String subjectUri, QuadStore quadStore) {
    this.subjectUri = subjectUri;
    this.quadStore = quadStore;
  }

  @Override
  public String getSubjectUri() {
    return subjectUri;
  }

  @Override
  public Set<String> getTypes() {
    if (types == null) {
      try (Stream<CursorQuad> quads = quadStore.getQuads(subjectUri, RDF_TYPE, Direction.OUT, "")) {
        types = quads
          .map(CursorQuad::getObject)
          .collect(toSet());
      }
    }
    return types;
  }
}
