package nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex;

import graphql.schema.GraphQLObjectType;

import java.util.Map;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLObjectType.newObject;

public class CollectionIndexSchemaFactory {

  public GraphQLObjectType createQuerySchema(Map<String, GraphQLObjectType> typesMap,
                                             CollectionIndexFetcherFactory fetcherFactory) {

    GraphQLObjectType.Builder result = newObject()
      .name("Collections");

    for (Map.Entry<String, GraphQLObjectType> typeMapping : typesMap.entrySet()) {
      String typeUri = typeMapping.getKey();
      GraphQLObjectType objectType = typeMapping.getValue();
      String typeName = objectType.getName();
      result.field(newFieldDefinition()
        .name(typeName)
        .type(list(objectType))
        .dataFetcher(fetcherFactory.collectionFetcher(typeUri))
      );
    }


    return result.build();
  }

}
