package nl.knaw.huygens.timbuctoo.v5.graphql.entity;

import com.google.common.collect.Lists;
import graphql.Scalars;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.relay.Relay;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphQlTypeGenerator {
  private static final Logger LOG = getLogger(GraphQlTypeGenerator.class);

  protected Relay relay = new Relay();

  public Tuple<Set<GraphQLType>, Map<String, GraphQLObjectType>> makeGraphQlTypes(Map<String, Type> types,
                                                                                  TypeNameStore typeNameStore,
                                                                                  DataFetcherFactory dataFetcherFactory
  ) {
    Map<String, GraphQLObjectType> collectionObjects = new HashMap<>();
    Map<String, GraphQLObjectType> typesMap = new HashMap<>();
    Map<String, String> nameToUri = new HashMap<>();
    TypeResolver objectResolver = environment -> {
      //Often a thing has one type. In that case this lambda is easy to implement. Simply return that type
      //In rdf things can have more then one type though (types are like java interfaces)
      //Since this lambda only allows us to return 1 type we need to do a bit more work and return one of the types that
      //the user actually requested
      Set<String> typeUris = ((BoundSubject) environment.getObject()).getType();
      for (Selection selection : environment.getField().getSelectionSet().getSelections()) {
        if (selection instanceof InlineFragment) {
          InlineFragment fragment = (InlineFragment) selection;
          String typeUri = nameToUri.get(fragment.getTypeCondition().getName());
          if (typeUris.contains(typeUri)) {
            return typesMap.get(typeUri);
          }
        } else {
          LOG.error("I have a union type whose selection is not an InlineFragment!");
        }
      }
      return typeUris.isEmpty() ? null : typesMap.get(typeUris.iterator().next());
    };

    TypeResolver valueTypeResolver = environment ->
      typesMap.get(((BoundSubject) environment.getObject()).getType().iterator().next());

    GraphQLInterfaceType entityInterface = newInterface()
      .name("Entity")
      .field(newFieldDefinition()
        .name("uri")
        .type(Scalars.GraphQLID)
      )
      .typeResolver(objectResolver)
      .build();
    GraphQLInterfaceType valueInterface = newInterface()
      .name("Value")
      .field(newFieldDefinition()
        .name("type")
        .type(nonNull(Scalars.GraphQLString))
      )
      .typeResolver(valueTypeResolver)
      .build();
    for (Type type : types.values()) {
      String typeUri = type.getName();
      String typeName = typeNameStore.makeRelayCompatibleGraphQlname(typeUri);
      nameToUri.put(typeName, typeUri);
      GraphQLObjectType objectType = makeObjectType(
        type,
        valueTypeResolver,
        nameToUri,
        typesMap,
        objectResolver,
        entityInterface,
        typeNameStore,
        dataFetcherFactory,
        valueInterface
      );
      collectionObjects.put(typeUri, makePaginatedList(objectType, typesMap));
    }

    Set<GraphQLType> allObjects = new HashSet<>();
    for (GraphQLObjectType type : typesMap.values()) {
      allObjects.add(type);
    }

    return Tuple.tuple(allObjects, collectionObjects);
  }

  private GraphQLObjectType makePaginatedList(GraphQLOutputType objectType,
                                              Map<String, GraphQLObjectType> typesMap) {
    return typesMap.computeIfAbsent(objectType.getName() + "Connection", key ->
      relay.connectionType(
        objectType.getName(),
        relay.edgeType(
          objectType.getName(),
          objectType,
          null,
          Lists.newArrayList()
        ),
        Lists.newArrayList()
      )
    );
  }

  private GraphQLObjectType makeObjectType(Type type, TypeResolver valueTypeResolver,
                                           Map<String, String> typeMappings,
                                           Map<String, GraphQLObjectType> typesMap,
                                           TypeResolver typeResolver,
                                           GraphQLInterfaceType entityInterface,
                                           TypeNameStore typeNameStore,
                                           DataFetcherFactory dataFetcherFactory,
                                           GraphQLInterfaceType valueInterface) {
    if (!typesMap.containsKey(type.getName())) {
      GraphQLObjectType.Builder graphqlType = newObject()
        .name(typeNameStore.makeRelayCompatibleGraphQlname(type.getName()))
        .withInterface(entityInterface)
        .field(newFieldDefinition()
          .name("uri")
          .type(Scalars.GraphQLID)
          .dataFetcher(dataFetcherFactory.entityUriDataFetcher())
        );
      for (Predicate predicate : type.getPredicates().values()) {
        GraphQLFieldDefinition fieldDefinition = makeField(predicate, typeMappings, typesMap,
          typeResolver, valueTypeResolver, typeNameStore, dataFetcherFactory, valueInterface
        );
        if (fieldDefinition != null) {
          graphqlType.field(fieldDefinition);
        }
      }
      typesMap.put(type.getName(), graphqlType.build());
    }
    return typesMap.get(type.getName());
  }

  private GraphQLFieldDefinition makeField(Predicate pred, Map<String, String> typeMappings,
                                                  Map<String, GraphQLObjectType> typesMap,
                                                  TypeResolver objectResolver, TypeResolver valueTypeResolver,
                                                  TypeNameStore typeNameStore, DataFetcherFactory fetcherFactory,
                                                  GraphQLInterfaceType valueInterface) {
    String fieldName = typeNameStore.makeRelayCompatibleGraphQlname(pred.getName());
    GraphQLFieldDefinition.Builder result = newFieldDefinition()
      .name(fieldName);
    if (pred.getReferenceTypes().size() == 0) {
      if (pred.getValueTypes().size() == 0) {
        System.out.println("This shouldn't happen! Typetracker has no types");
        return null;
      } else if (pred.getValueTypes().size() == 1) {
        return valueField(
          result,
          pred,
          typeMappings,
          typesMap,
          fetcherFactory,
          typeNameStore,
          valueInterface
        );
      } else {
        List<GraphQLTypeReference> refs = new ArrayList<>();
        for (String valueType : pred.getValueTypes()) {
          refs.add(valueType(valueType, typesMap, typeMappings, typeNameStore, valueInterface));
        }
        return unionField(result, pred, valueTypeResolver, fetcherFactory, refs, typesMap);
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        return objectField(result, pred, typeNameStore, fetcherFactory, typesMap);
      } else {
        List<GraphQLTypeReference> refs = new ArrayList<>();
        for (String referenceType : pred.getReferenceTypes()) {
          refs.add(new GraphQLTypeReference(typeNameStore.makeRelayCompatibleGraphQlname(referenceType)));
        }
        for (String valueType : pred.getValueTypes()) {
          refs.add(valueType(valueType, typesMap, typeMappings, typeNameStore, valueInterface));
        }

        return unionField(result, pred, objectResolver, fetcherFactory, refs, typesMap);
      }
    }
  }

  private GraphQLFieldDefinition objectField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                             TypeNameStore typeNameStore, DataFetcherFactory dataFetcherFactory,
                                             Map<String, GraphQLObjectType> wrappedValueTypes) {
    GraphQLFieldDefinition.Builder type = result
      .dataFetcher(dataFetcherFactory.relationFetcher(pred.getName(), pred.isList()))
      .type(wrap(
        new GraphQLTypeReference(
          typeNameStore.makeRelayCompatibleGraphQlname(pred.getReferenceTypes().iterator().next())),
        pred.isOptional(),
        pred.isList(),
        wrappedValueTypes
      ));
    if (pred.isList()) {
      type.argument(relay.getConnectionFieldArguments());
    }
    return type.build();
  }

  private GraphQLFieldDefinition unionField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                            TypeResolver valueTypeResolver,
                                            DataFetcherFactory dataFetcherFactory,
                                            List<GraphQLTypeReference> refs,
                                            Map<String, GraphQLObjectType> wrappedValueTypes) {
    GraphQLUnionType.Builder unionType = newUnionType()
      .name("Union_" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", ""))
      .typeResolver(valueTypeResolver);
    for (GraphQLTypeReference type : refs) {
      unionType.possibleType(type);
    }
    GraphQLFieldDefinition.Builder type = result
      .dataFetcher(dataFetcherFactory.unionFetcher(pred.getName(), pred.isList()))
      .type(wrap(unionType.build(), pred.isOptional(), pred.isList(), wrappedValueTypes));

    if (pred.isList()) {
      type.argument(relay.getConnectionFieldArguments());
    }

    return type.build();
  }

  private GraphQLFieldDefinition valueField(GraphQLFieldDefinition.Builder fieldDefinition,
                                                   Predicate pred, Map<String, String> typeMappings,
                                                   Map<String, GraphQLObjectType> wrappedValueTypes,
                                                   DataFetcherFactory dataFetcherFactory,
                                                   TypeNameStore typeNameStore, GraphQLInterfaceType valueInterface) {
    GraphQLFieldDefinition.Builder type = fieldDefinition
      .dataFetcher(dataFetcherFactory.typedLiteralFetcher(pred.getName(), pred.isList()))
      .type(wrap(
        valueType(
          pred.getValueTypes().iterator().next(),
          wrappedValueTypes,
          typeMappings,
          typeNameStore,
          valueInterface
        ),
        pred.isOptional(),
        pred.isList(),
        wrappedValueTypes
        )
      );

    if (pred.isList()) {
      type.argument(relay.getConnectionFieldArguments());
    }

    return type.build();
  }

  private GraphQLOutputType wrap(GraphQLOutputType outputType, boolean isOptional, boolean isList,
                                 Map<String, GraphQLObjectType> wrappedValueTypes) {
    if (isList) {
      return makePaginatedList(outputType, wrappedValueTypes);
    } else if (!isOptional) {
      return nonNull(outputType);
    } else {
      return outputType;
    }
  }

  private GraphQLTypeReference valueType(String typeUri, Map<String, GraphQLObjectType> wrappedValueTypes,
                                             Map<String, String> typeMappings, TypeNameStore typeNameStore,
                                             GraphQLInterfaceType valueInterface) {
    String typeName = "valuetype_" + typeNameStore.makeRelayCompatibleGraphQlname(typeUri);
    if (!wrappedValueTypes.containsKey(typeUri)) {
      typeMappings.put(typeName, typeUri);
      GraphQLScalarType matchedValueType = matchedValueType(typeUri);
      wrappedValueTypes.put(
        typeUri,
        newObject()
          .name(typeName)
          .withInterface(valueInterface)
          .field(newFieldDefinition()
            .name("value")
            .type(nonNull(matchedValueType == null ? Scalars.GraphQLString : matchedValueType))
          )
          .field(newFieldDefinition()
            .name("type")
            .type(nonNull(Scalars.GraphQLString))
            .staticValue(typeUri)
          )
          .build()
      );
    }
    return new GraphQLTypeReference(typeName);
  }

  private GraphQLScalarType matchedValueType(String valueType) {
    switch (valueType) {
      case "http://www.w3.org/TR/xmlschema11-2/#boolean":
        return Scalars.GraphQLBoolean;
      case "http://www.w3.org/TR/xmlschema11-2/#decimal":
        return Scalars.GraphQLBigDecimal;
      case "http://www.w3.org/TR/xmlschema11-2/#integer":
        return Scalars.GraphQLBigInteger;
      case "http://www.w3.org/TR/xmlschema11-2/#float":
        return Scalars.GraphQLFloat;
      case "http://www.w3.org/TR/xmlschema11-2/#byte":
        return Scalars.GraphQLByte;
      case "http://www.w3.org/TR/xmlschema11-2/#short":
        return Scalars.GraphQLShort;
      case "http://www.w3.org/TR/xmlschema11-2/#int":
        return Scalars.GraphQLInt;
      case "http://www.w3.org/TR/xmlschema11-2/#long":
        return Scalars.GraphQLLong;
      case "http://www.w3.org/2001/XMLSchema#string":
        return Scalars.GraphQLString;
      default:
        return null;
    }
  }

}
