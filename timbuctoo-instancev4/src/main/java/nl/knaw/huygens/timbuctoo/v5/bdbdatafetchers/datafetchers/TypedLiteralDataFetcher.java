package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;

public class TypedLiteralDataFetcher extends WalkTriplesDataFetcher {

  public TypedLiteralDataFetcher(String predicate, QuadStore tripleStore) {
    super(predicate, Direction.OUT, tripleStore);
  }

  @Override
  protected TypedValue makeItem(CursorQuad triple) {
    return triple.getValuetype()
      .map(valueType -> TypedValue.create(triple.getObject(), valueType))
      .orElseGet(() -> TypedValue.create(triple.getObject()));
  }
}
