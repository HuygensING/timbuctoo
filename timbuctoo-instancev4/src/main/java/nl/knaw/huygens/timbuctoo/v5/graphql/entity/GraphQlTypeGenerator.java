package nl.knaw.huygens.timbuctoo.v5.graphql.entity;

import graphql.Scalars;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriFetcherWrapper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLUnionType.newUnionType;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphQlTypeGenerator {
  private static final Logger LOG = getLogger(GraphQlTypeGenerator.class);

  public Map<String, GraphQLObjectType> makeGraphQlTypes(Map<String, Type> types, TypeNameStore typeNameStore,
                                                         DataFetcherFactory dataFetcherFactory) {
    Map<String, String> typeMappings = new HashMap<>();
    Map<String, GraphQLObjectType> typesMap = new HashMap<>();
    Map<String, GraphQLObjectType> wrappedValueTypes = new HashMap<>();

    TypeResolver objectResolver = environment -> {
      //Often a thing has one type. In that case this lambda is easy to implement. Simply return that type
      //In rdf things can have more then one type though (types are like java interfaces)
      //Since this lambda only allows us to return 1 type we need to do a bit more work and return one of the types that
      //the user actually requested
      Set<String> typeUris = ((TypedValue) environment.getObject()).getType();
      for (Selection selection : environment.getField().getSelectionSet().getSelections()) {
        if (selection instanceof InlineFragment) {
          InlineFragment fragment = (InlineFragment) selection;
          String typeUri = typeMappings.get(fragment.getTypeCondition().getName());
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
      typesMap.get(((TypedValue) environment.getObject()).getType().iterator().next());

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
        .dataFetcher(new UriFetcherWrapper(dataFetcherFactory.entityUriDataFetcher()))
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
        List<GraphQLObjectType> types = new ArrayList<>();
        for (String valueType : pred.getValueTypes()) {
          types.add(valueType(valueType, wrappedValueTypes, typeMappings, typeNameStore, valueInterface));
        }
        ArrayList<GraphQLTypeReference> refs = newArrayList();
        return unionField(result, pred, valueTypeResolver, dataFetcherFactory, refs, types);
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        return objectField(result, pred, typeNameStore, dataFetcherFactory);
      } else {
        List<GraphQLTypeReference> refs = new ArrayList<>();
        List<GraphQLObjectType> values = new ArrayList<>();
        for (String referenceType : pred.getReferenceTypes()) {
          refs.add(new GraphQLTypeReference(typeNameStore.makeGraphQlname(referenceType)));
        }
        for (String valueType : pred.getValueTypes()) {
          values.add(valueType(valueType, wrappedValueTypes, typeMappings, typeNameStore, valueInterface));
        }

        return unionField(result, pred, objectResolver, dataFetcherFactory, refs, values);
      }
    }
  }

  private static GraphQLFieldDefinition objectField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                                    TypeNameStore typeNameStore,
                                                    DataFetcherFactory dataFetcherFactory) {
    return result
      .dataFetcher(new DataFetcherWrapper(pred.isList(), dataFetcherFactory.relationFetcher(pred.getName())))
      .type(wrap(new GraphQLTypeReference(typeNameStore.makeGraphQlname(
        pred.getReferenceTypes().iterator().next()
      )), pred.isOptional(), pred.isList()))
      .build();
  }

  private static GraphQLFieldDefinition unionField(GraphQLFieldDefinition.Builder result, Predicate pred,
                                            TypeResolver valueTypeResolver,
                                            DataFetcherFactory dataFetcherFactory,
                                            List<GraphQLTypeReference> refs, List<GraphQLObjectType> types) {
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
    for (GraphQLTypeReference type : refs) {
      unionType.possibleType(type);
    }
    return result
      .dataFetcher(new DataFetcherWrapper(pred.isList(), dataFetcherFactory.unionFetcher(pred.getName())))
      .type(wrap(unionType.build(), pred.isOptional(), pred.isList()))
      .build();
  }

  private static GraphQLFieldDefinition valueField(GraphQLFieldDefinition.Builder fieldDefinition,
                                                   Predicate pred, Map<String, String> typeMappings,
                                                   Map<String, GraphQLObjectType> wrappedValueTypes,
                                                   DataFetcherFactory dataFetcherFactory,
                                                   TypeNameStore typeNameStore, GraphQLInterfaceType valueInterface) {
    return fieldDefinition
      .dataFetcher(new DataFetcherWrapper(pred.isList(), dataFetcherFactory.typedLiteralFetcher(pred.getName())))
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
