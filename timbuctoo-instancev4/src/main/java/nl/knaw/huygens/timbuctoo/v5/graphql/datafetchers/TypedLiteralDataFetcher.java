package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

public class TypedLiteralDataFetcher extends RelatedDataFetcher {
  public TypedLiteralDataFetcher(String predicate, boolean isList, TripleStore tripleStore) {
    super(predicate, tripleStore, isList);
  }

  @Override
  protected BoundSubject makeItem(Quad quad) {
    return new BoundSubject(quad.getObject(), quad.getValuetype().orElse(null));
  }
}
