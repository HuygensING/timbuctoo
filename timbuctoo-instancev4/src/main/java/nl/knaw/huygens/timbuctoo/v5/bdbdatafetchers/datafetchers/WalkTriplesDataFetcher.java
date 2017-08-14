package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.PaginationHelper.getPaginatedList;

public abstract class WalkTriplesDataFetcher<T extends DatabaseResult> implements RelatedDataFetcher<T> {
  private final String predicate;
  private final Direction direction;
  private final QuadStore tripleStore;

  public WalkTriplesDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    this.predicate = predicate;
    this.direction = direction;
    this.tripleStore = tripleStore;
  }

  protected abstract T makeItem(CursorQuad quad);

  public PaginatedList<T> getList(SubjectReference source, PaginationArguments arguments) {
    String cursor = arguments.getCursor();
    try (Stream<CursorQuad> quads = tripleStore.getQuads(source.getValue(), predicate, direction, cursor)) {
      return getPaginatedList(quads, this::makeItem, arguments.getCount(), !cursor.isEmpty());
    }
  }

  public T getItem(SubjectReference source) {
    try (Stream<CursorQuad> quads = tripleStore.getQuads(source.getValue(), predicate, direction, "")) {
      return quads.findFirst()
        .map(this::makeItem)
        .orElse(null);
    }
  }
}
