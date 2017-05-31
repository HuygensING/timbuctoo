package nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;

import java.util.Map;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class CollectionIndexSchemaFactory {

  public GraphQLObjectType createQuerySchema(Map<String, GraphQLObjectType> typesMap,
                                             DataFetcherFactory fetcherFactory,
                                             PaginationArgumentsHelper paginationArgumentsHelper) {

    GraphQLObjectType.Builder result = newObject()
      .name("Query");

    for (Map.Entry<String, GraphQLObjectType> typeMapping : typesMap.entrySet()) {
      String typeUri = typeMapping.getKey();
      GraphQLObjectType objectType = typeMapping.getValue();
      String typeName = objectType.getName();
      GraphQLFieldDefinition.Builder field = newFieldDefinition()
        .name(typeName)
        .dataFetcher(new CollectionFetcherWrapper(fetcherFactory.collectionFetcher(typeUri)));
      paginationArgumentsHelper.makePaginatedList(field, objectType);
      result.field(field);
    }

    return result.build();
  }

}
