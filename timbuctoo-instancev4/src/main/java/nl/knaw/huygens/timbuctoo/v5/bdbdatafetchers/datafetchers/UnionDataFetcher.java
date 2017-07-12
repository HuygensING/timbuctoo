package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class UnionDataFetcher extends WalkTriplesDataFetcher {
  private final QuadStore tripleStore;

  public UnionDataFetcher(String predicate, QuadStore tripleStore) {
    super(predicate, tripleStore);
    this.tripleStore = tripleStore;
  }

  @Override
  protected TypedValue makeItem(CursorQuad quad) {
    if (quad.getValuetype().isPresent()) {
      return TypedValue.create(quad.getObject(), quad.getValuetype().get());
    } else {
      try (Stream<CursorQuad> quads = tripleStore.getQuads(quad.getObject(), RDF_TYPE, "")) {
        final Set<String> types = quads
          .map(CursorQuad::getObject)
          .collect(toSet());
        return TypedValue.create(quad.getObject(), types);
      }
    }
  }
}
