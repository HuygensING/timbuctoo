package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class RelationDataFetcher extends WalkTriplesDataFetcher {

  public RelationDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    super(predicate, direction, tripleStore);
  }

  @Override
  protected TypedValue makeItem(CursorQuad triple) {
    return TypedValue.create(triple.getObject());
  }
}
