package nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex;

import graphql.relay.Relay;
import graphql.schema.GraphQLObjectType;

import java.util.Map;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class CollectionIndexSchemaFactory {

  public GraphQLObjectType createQuerySchema(Map<String, GraphQLObjectType> typesMap,
                                             CollectionIndexFetcherFactory fetcherFactory) {
    Relay relay = new Relay();

    GraphQLObjectType.Builder result = newObject()
      .name("Collections");

    for (Map.Entry<String, GraphQLObjectType> typeMapping : typesMap.entrySet()) {
      String typeUri = typeMapping.getKey();
      GraphQLObjectType objectType = typeMapping.getValue();
      String typeName = objectType.getName();
      if (typeName.endsWith("Connection")) {
        typeName = typeName.substring(0, typeName.length() - "Connection".length());
      }
      result.field(newFieldDefinition()
        .name(typeName)
        .type(objectType)
        .argument(relay.getConnectionFieldArguments())
        .dataFetcher(fetcherFactory.collectionFetcher(typeUri))
      );
    }


    return result.build();
  }

}
