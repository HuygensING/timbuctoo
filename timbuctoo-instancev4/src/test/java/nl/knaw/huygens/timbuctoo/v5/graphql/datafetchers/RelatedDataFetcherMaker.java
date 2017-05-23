package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

public class RelatedDataFetcherMaker {
  public static RelatedDataFetcher makeRelatedDataFetcher(BdbTripleStore tripleStore, boolean isList) {
    return new RelatedDataFetcher("http://example.org/predicate", tripleStore, isList) {
      @Override
      protected BoundSubject makeItem(Quad quad) {
        return new BoundSubject(quad.getSubject() + " - " + quad.getObject());
      }
    };
  }


}
