package nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookupFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Map;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

public class CollectionIndexSchemaFactory {

  public GraphQLObjectType createQuerySchema(Map<String, GraphQLObjectType> rdfTypeRepresentingTypes,
                                             Map<String, GraphQLObjectType> staticTypes,
                                             DataFetcherFactory fetcherFactory,
                                             PaginationArgumentsHelper paginationArgumentsHelper,
                                             String baseUri) {

    GraphQLObjectType.Builder result = newObject()
      .name("Query");

    final String uriArgument = "uri";
    final LookupFetcherWrapper lookupFetcher = new LookupFetcherWrapper(
      uriArgument,
      fetcherFactory.lookupFetcher(),
      baseUri
    );

    for (Map.Entry<String, GraphQLObjectType> typeMapping : rdfTypeRepresentingTypes.entrySet()) {
      String typeUri = typeMapping.getKey();
      GraphQLObjectType objectType = typeMapping.getValue();
      if (!typeUri.equals(RdfConstants.UNKNOWN)) {
        String typeName = objectType.getName();

        GraphQLFieldDefinition.Builder collectionField = newFieldDefinition()
          .name(typeName + "List")
          .description(typeMapping.getValue().getDescription())
          .dataFetcher(new CollectionFetcherWrapper(fetcherFactory.collectionFetcher(typeUri)));
        paginationArgumentsHelper.makePaginatedList(collectionField, objectType);
        result.field(collectionField);

        GraphQLFieldDefinition.Builder lookupField = newFieldDefinition()
          .name(typeName)
          .type(objectType)
          .description(typeMapping.getValue().getDescription())
          .dataFetcher(lookupFetcher)
          .argument(
            newArgument()
              .name(uriArgument)
              .type(nonNull(Scalars.GraphQLID))
              .description("The uri of the item that you wish to retrieve")
          );
        result.field(lookupField);
      }
    }

    GraphQLObjectType.Builder staticSchema = newObject()
      .name("StaticSchema");

    for (Map.Entry<String, GraphQLObjectType> staticType : staticTypes.entrySet()) {
      String typeUri = staticType.getKey();
      String typeName = staticType.getValue().getName();

      GraphQLFieldDefinition.Builder collectionField = newFieldDefinition()
        .name(typeName + "List")
        .description(staticType.getValue().getDescription())
        .dataFetcher(new CollectionFetcherWrapper(fetcherFactory.collectionFetcher(typeUri)));
      paginationArgumentsHelper.makePaginatedList(collectionField, staticType.getValue());
      staticSchema.field(collectionField);

      GraphQLFieldDefinition.Builder lookupField = newFieldDefinition()
        .name(typeName)
        .type(staticType.getValue())
        .description(staticType.getValue().getDescription())
        .dataFetcher(lookupFetcher)
        .argument(
          newArgument()
            .name(uriArgument)
            .type(nonNull(Scalars.GraphQLID))
            .description("The uri of the item that you wish to retrieve")
        );
      staticSchema.field(lookupField);
    }
    return result
      .field(newFieldDefinition()
        .description("The predefined schema's. These will not change in a backwards incompatible manner. You can " +
          "hardcode the queries to this part of the tree. There are no non-nullable fields however.")
        .name("static")
        .type(staticSchema)
        .dataFetcher(environment -> "")//dummmy value so that the graphql process continues
      )
      .build();
  }

}
