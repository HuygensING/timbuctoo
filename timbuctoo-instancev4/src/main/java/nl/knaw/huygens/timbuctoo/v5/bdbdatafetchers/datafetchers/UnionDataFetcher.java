package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class UnionDataFetcher extends WalkTriplesDataFetcher<DatabaseResult> {

  public UnionDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    super(predicate, direction, tripleStore);
  }

  @Override
  protected DatabaseResult makeItem(CursorQuad quad) {
    if (quad.getValuetype().isPresent()) {
      return TypedValue.create(quad.getObject(), quad.getValuetype().get());
    } else {
      return new LazyTypeSubjectReference(quad.getObject(), tripleStore);
    }
  }
}
