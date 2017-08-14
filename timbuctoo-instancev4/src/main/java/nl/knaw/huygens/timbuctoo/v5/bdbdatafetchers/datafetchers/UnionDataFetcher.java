package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher extends WalkTriplesDataFetcher<DatabaseResult> {
  private final QuadStore tripleStore;

  public UnionDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    super(predicate, direction, tripleStore);
    this.tripleStore = tripleStore;
  }

  @Override
  protected DatabaseResult makeItem(CursorQuad quad) {
    if (quad.getValuetype().isPresent()) {
      return TypedValue.create(quad.getObject(), quad.getValuetype().get());
    } else {
      try (Stream<CursorQuad> quads = tripleStore.getQuads(quad.getObject(), RDF_TYPE, Direction.OUT, "")) {
        final Set<String> types = quads
          .map(CursorQuad::getObject)
          .collect(toSet());
        return SubjectReference.create(quad.getObject(), types);
      }
    }
  }
}
