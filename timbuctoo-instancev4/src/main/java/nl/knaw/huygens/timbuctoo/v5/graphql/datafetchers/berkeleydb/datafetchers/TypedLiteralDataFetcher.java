package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedLanguageValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class TypedLiteralDataFetcher extends WalkTriplesDataFetcher<TypedValue> {

  public TypedLiteralDataFetcher(String predicate) {
    super(predicate, Direction.OUT);
  }

  @Override
  protected TypedValue makeItem(CursorQuad quad, DataSet dataSet) {
    if (quad.getValuetype().isPresent()) {
      if (quad.getLanguage().isPresent()) {
        return TypedLanguageValue.create(quad.getObject(), quad.getValuetype().get(),
            quad.getLanguage().get(), dataSet);
      } else {
        return TypedValue.create(quad.getObject(), quad.getValuetype().get(), dataSet);
      }
    } else {
      throw new IllegalStateException("Source is not a quad referencing a value");
    }
  }
}
