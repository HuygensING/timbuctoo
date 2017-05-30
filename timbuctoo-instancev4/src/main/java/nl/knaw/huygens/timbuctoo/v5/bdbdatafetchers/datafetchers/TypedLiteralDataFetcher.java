package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;

public class TypedLiteralDataFetcher extends WalkTriplesDataFetcher {

  public TypedLiteralDataFetcher(String predicate, boolean isList, BdbTripleStore tripleStore) {
    super(predicate, isList, tripleStore);
  }

  @Override
  protected BoundSubject makeItem(Quad triple) {
    return triple.getValuetype()
      .map(valueType -> new BoundSubject(triple.getObject(), valueType))
      .orElseGet(() -> new BoundSubject(triple.getObject()));
  }
}
