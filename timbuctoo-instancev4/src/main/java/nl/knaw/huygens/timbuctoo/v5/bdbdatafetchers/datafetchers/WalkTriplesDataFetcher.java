package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.PaginationHelper.getPaginatedList;

public abstract class WalkTriplesDataFetcher implements RelatedDataFetcher {
  private final String predicate;
  private final BdbTripleStore tripleStore;

  public WalkTriplesDataFetcher(String predicate, BdbTripleStore tripleStore) {
    this.predicate = predicate;
    this.tripleStore = tripleStore;
  }

  protected abstract TypedValue makeItem(CursorQuad quad);

  public PaginatedList getList(TypedValue source, PaginationArguments arguments) {
    try (Stream<CursorQuad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
      return getPaginatedList(quads, this::makeItem);
    }
  }

  public TypedValue getItem(TypedValue source) {
    try (Stream<CursorQuad> quads = tripleStore.getQuads(source.getValue(), predicate)) {
      return quads.findFirst()
        .map(this::makeItem)
        .orElse(null);
    }
  }
}
