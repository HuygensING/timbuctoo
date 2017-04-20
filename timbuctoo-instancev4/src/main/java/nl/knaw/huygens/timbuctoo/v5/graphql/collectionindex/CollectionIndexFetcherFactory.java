package nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex;

import graphql.schema.DataFetcher;

public interface CollectionIndexFetcherFactory {
  DataFetcher collectionFetcher(String typeUri);
}
