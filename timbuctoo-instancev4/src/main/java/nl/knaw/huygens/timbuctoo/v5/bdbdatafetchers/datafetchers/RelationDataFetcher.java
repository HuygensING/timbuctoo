package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

public class RelationDataFetcher extends WalkTriplesDataFetcher<SubjectReference> {

  public RelationDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    super(predicate, direction, tripleStore);
  }

  @Override
  protected SubjectReference makeItem(CursorQuad triple) {
    return SubjectReference.create(triple.getObject());
  }
}
