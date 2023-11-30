package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginatedList;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.PaginationArguments;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class WalkTriplesDataFetcher<T extends DatabaseResult> implements RelatedDataFetcher<T> {
  private final String predicate;
  private final Direction direction;

  public WalkTriplesDataFetcher(String predicate, Direction direction) {
    this.predicate = predicate;
    this.direction = direction;
  }

  protected abstract T makeItem(CursorQuad quad, DataSet dataSet);

  public PaginatedList<T> getList(SubjectReference source, PaginationArguments arguments, DataSet dataSet) {
    String cursor = arguments.getCursor();
    try (Stream<CursorQuad> q = dataSet.getQuadStore().getQuads(source.getSubjectUri(), predicate, direction, cursor)) {
      return PaginationHelper.getPaginatedList(q, qd -> this.makeItem(qd, dataSet), arguments, Optional.empty());
    }
  }

  public T getItem(SubjectReference source, DataSet dataSet) {
    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuads(source.getSubjectUri(), predicate, direction, "")) {
      return quads.findFirst()
        .map(q -> this.makeItem(q, dataSet))
        .orElse(null);
    }
  }
}
