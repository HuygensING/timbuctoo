package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

public class RelationDataFetcher extends RelatedDataFetcher {

  public RelationDataFetcher(String predicate, boolean isList, TripleStore tripleStore) {
    super(predicate, tripleStore, isList);
  }

  @Override
  protected BoundSubject makeItem(Quad quad) {
    return new BoundSubject(quad.getObject());
  }


}
