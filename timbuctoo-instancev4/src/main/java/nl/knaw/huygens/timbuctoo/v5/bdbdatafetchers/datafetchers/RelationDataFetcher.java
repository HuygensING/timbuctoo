package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

public class RelationDataFetcher extends WalkTriplesDataFetcher {

  public RelationDataFetcher(String predicate, boolean isList, BdbTripleStore tripleStore) {
    super(predicate, isList, tripleStore);
  }

  @Override
  protected BoundSubject makeItem(Quad triple) {
    return new BoundSubject(triple.getObject());
  }
}
