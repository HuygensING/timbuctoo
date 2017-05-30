package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;

import java.util.Map;

public interface DataFetcherFactory {
  DataFetcher relationFetcher(String predicate, boolean isList);

  DataFetcher typedLiteralFetcher(String predicate, boolean isList);

  DataFetcher unionFetcher(String predicate, boolean isList);

  DataFetcher entityUriDataFetcher();

  DataFetcher collectionFetcher(String typeUri);

  TypeResolver objectResolver(Map<String, String> typeMappings, Map<String, GraphQLObjectType> typesMap);

  TypeResolver valueResolver(Map<String, GraphQLObjectType> typesMap);
}
