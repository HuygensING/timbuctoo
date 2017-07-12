package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;

public class RelationDataFetcher extends WalkTriplesDataFetcher {

  public RelationDataFetcher(String predicate, QuadStore tripleStore) {
    super(predicate, tripleStore);
  }

  @Override
  protected TypedValue makeItem(CursorQuad triple) {
    return TypedValue.create(triple.getObject());
  }
}
