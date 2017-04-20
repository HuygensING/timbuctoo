package nl.knaw.huygens.timbuctoo.v5.graphql.entity;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;

public class GraphQlTypeGenerator {
  public Map<String, GraphQLObjectType> makeGraphQlTypes(Map<String, Type> types, TypeNameStore typeNameStore,
                                                         DataFetcherFactory dataFetcherFactory) {
    Map<String, String> typeMappings = new HashMap<>();
    Map<String, GraphQLObjectType> typesMap = new HashMap<>();
    Map<String, GraphQLObjectType> wrappedValueTypes = new HashMap<>();
    TypeResolver objectResolver = object -> {
      String typeUri = ((BoundSubject) object).getType();
      GraphQLObjectType objectType = typesMap.get(typeUri);
      if (objectType == null) {
        return wrappedValueTypes.get(typeUri);
      } else {
        return objectType;
      }
    };
    TypeResolver valueTypeResolver = object -> typesMap.get(((BoundSubject) object).getType());
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
        .type(Scalars.GraphQLString)
      )
      .typeResolver(valueTypeResolver)
      .build();
    for (Type type : types.values()) {
      String typeUri = type.getName();
      String typeName = typeNameStore.makeGraphQlname(typeUri);
      typeMappings.put(typeName, typeUri);
      GraphQLObjectType objectType = makeObjectType(
        type,
        valueTypeResolver, typeMappings,
        wrappedValueTypes,
        objectResolver,
        entityInterface, typeNameStore, dataFetcherFactory, valueInterface
      );
      typesMap.put(typeUri, objectType);
    }
    return typesMap;
  }

  private static GraphQLObjectType makeObjectType(Type type, TypeResolver valueTypeResolver,
                                                  Map<String, String> typeMappings,
                                                  Map<String, GraphQLObjectType> wrappedValueTypes,
                                                  TypeResolver typeResolver,
                                                  GraphQLInterfaceType entityInterface,
                                                  TypeNameStore typeNameStore,
                                                  DataFetcherFactory dataFetcherFactory,
                                                  GraphQLInterfaceType valueInterface) {
    GraphQLObjectType.Builder graphqlType = newObject()
      .name(typeNameStore.makeGraphQlname(type.getName()))
      .withInterface(entityInterface)
      .field(newFieldDefinition()
        .name("uri")
        .type(Scalars.GraphQLID)
        .dataFetcher(dataFetcherFactory.entityUriDataFetcher())
      );
    for (Predicate predicate : type.getPredicates().values()) {
      GraphQLFieldDefinition fieldDefinition = makeField(predicate, typeMappings, wrappedValueTypes,
        typeResolver, valueTypeResolver, typeNameStore, dataFetcherFactory, valueInterface
      );
      if (fieldDefinition != null) {
        graphqlType.field(fieldDefinition);
      }
    }
    return graphqlType.build();
  }

  private static GraphQLFieldDefinition makeField(Predicate pred, Map<String, String> typeMappings,
                                                  Map<String, GraphQLObjectType> wrappedValueTypes,
                                                  TypeResolver objectResolver, TypeResolver valueTypeResolver,
                                                  TypeNameStore typeNameStore, DataFetcherFactory dataFetcherFactory,
                                                  GraphQLInterfaceType valueInterface) {
    String fieldName = typeNameStore.makeGraphQlname(pred.getName());
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
          wrappedValueTypes,
          dataFetcherFactory,
          typeNameStore,
          valueInterface
        );
      } else {
        List<Object> types = new ArrayList<>();
        for (String valueType : pred.getValueTypes()) {
          types.add(valueType(valueType, wrappedValueTypes, typeMappings, typeNameStore, valueInterface));
        }
        return unionField(result, pred, typeMappings, valueTypeResolver, dataFetcherFactory, fieldName, types);
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        return objectField(result, pred, typeNameStore, dataFetcherFactory);
      } else {
        List<Object> types = new ArrayList<>();
        for (String referenceType : pred.getReferenceTypes()) {
          types.add(new GraphQLTypeReference(typeNameStore.makeGraphQlname(referenceType)));
        }
        for (String valueType : pred.getValueTypes()) {
          types.add(valueType(valueType, wrappedValueTypes, typeMappings, typeNameStore, valueInterface));
        }

        return unionField(result, pred, typeMappings, objectResolver, dataFetcherFactory, fieldName, types);
      }
    }
  }

  private static GraphQLFieldDefinition objectField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                                    TypeNameStore typeNameStore,
                                                    DataFetcherFactory dataFetcherFactory) {
    return result
      .dataFetcher(dataFetcherFactory.relationFetcher(pred.getName(), pred.isList()))
      .type(wrap(new GraphQLTypeReference(typeNameStore.makeGraphQlname(
        pred.getReferenceTypes().iterator().next()
      )), pred.isOptional(), pred.isList()))
      .build();
  }

  private static GraphQLFieldDefinition unionField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                                   Map<String, String> typeMappings,
                                                   TypeResolver valueTypeResolver,
                                                   DataFetcherFactory dataFetcherFactory, String fieldName,
                                                   List<Object> types) {
    GraphQLUnionType.Builder unionType = newUnionType()
      .name("Union_" + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", ""))
      .typeResolver(valueTypeResolver);
    for (Object type : types) {
      if (type instanceof GraphQLTypeReference) {
        unionType.possibleType((GraphQLTypeReference) type);
      } else {
        unionType.possibleType((GraphQLObjectType) type);
      }
    }
    return result
      .dataFetcher(dataFetcherFactory.unionFetcher(pred.getName(), pred.isList(), fieldName, typeMappings))
      .type(wrap(unionType.build(), pred.isOptional(), pred.isList()))
      .build();
  }

  private static GraphQLFieldDefinition valueField(GraphQLFieldDefinition.Builder fieldDefinition,
                                                   Predicate pred, Map<String, String> typeMappings,
                                                   Map<String, GraphQLObjectType> wrappedValueTypes,
                                                   DataFetcherFactory dataFetcherFactory,
                                                   TypeNameStore typeNameStore, GraphQLInterfaceType valueInterface) {
    return fieldDefinition
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
        pred.isList()
        )
      )
      .build();
  }

  private static GraphQLOutputType wrap(GraphQLOutputType outputType, boolean isOptional, boolean isList) {
    if (isList) {
      return list(outputType);
    } else if (!isOptional) {
      return nonNull(outputType);
    } else {
      return outputType;
    }
  }

  private static GraphQLObjectType valueType(String typeUri, Map<String, GraphQLObjectType> wrappedValueTypes,
                                             Map<String, String> typeMappings, TypeNameStore typeNameStore,
                                             GraphQLInterfaceType valueInterface) {
    if (!wrappedValueTypes.containsKey(typeUri)) {
      String typeName = "valuetype_" + typeNameStore.makeGraphQlname(typeUri);
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
    return wrappedValueTypes.get(typeUri);
  }

  private static GraphQLScalarType matchedValueType(String valueType) {
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
