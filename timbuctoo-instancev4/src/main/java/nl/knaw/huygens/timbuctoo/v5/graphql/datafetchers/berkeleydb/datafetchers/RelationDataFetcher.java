package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

public class RelationDataFetcher extends WalkTriplesDataFetcher<SubjectReference> {

  public RelationDataFetcher(String predicate, Direction direction, QuadStore tripleStore) {
    super(predicate, direction, tripleStore);
  }

  @Override
  protected SubjectReference makeItem(CursorQuad triple) {
    if (triple.getValuetype().isPresent()) {
      throw new IllegalStateException("Source is not a triple referencing a URI");
    } else {
      return new LazyTypeSubjectReference(triple.getObject(), tripleStore);
    }

  }
}
