package nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;

import java.util.Optional;

public class RelationDataFetcher extends WalkTriplesDataFetcher<SubjectReference> {
  public RelationDataFetcher(String predicate, Direction direction) {
    super(predicate, direction);
  }

  @Override
  protected SubjectReference makeItem(CursorQuad triple, DataSet dataSet) {
    if (triple.getValuetype().isPresent()) {
      throw new IllegalStateException("Source is not a triple referencing a URI");
    } else {
      return new LazyTypeSubjectReference(triple.getObject(), Optional.empty(), dataSet);
    }
  }
}
