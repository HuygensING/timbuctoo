package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DerivedSchemaTypeGenerator {

  public static TypeDefinitionRegistry makeGraphQlTypes(String userId, String dataSetId, String rootType,
                                                        Map<String, Type> types,
                                                        TypeNameStore typeNameStore,
                                                        RuntimeWiring.Builder runtimeWiring,
                                                        DataFetcherFactory dataFetcherFactory,
                                                        PaginationArgumentsHelper argumentsHelper) {
    GraphQlTypesContainer typesContainer = new GraphQlTypesContainer(
      rootType,
      typeNameStore,
      userId,
      dataSetId,
      runtimeWiring,
      dataFetcherFactory,
      argumentsHelper
    );

    for (Type type : types.values()) {
      typesContainer.openObjectType(type.getName());
      for (Predicate predicate : type.getPredicates()) {
        fieldForDerivedType(predicate, typesContainer, typeNameStore);
      }
      typesContainer.closeObjectType(type.getName());
    }
    return typesContainer.getSchema();
  }

  private static void fieldForDerivedType(Predicate pred, GraphQlTypesContainer typesContainer,
                                          TypeNameStore typeNameStore) {
    String fieldName = typeNameStore.makeGraphQlnameForPredicate(pred.getName(), pred.getDirection());
    if (pred.getReferenceTypes().size() == 0) {
      if (pred.getValueTypes().size() == 0) {
        System.out.println("This shouldn't happen! The predicate has no value types and no reference types!");
      } else if (pred.getValueTypes().size() == 1) {
        typesContainer.valueField(
          fieldName,
          null,
          pred.getValueTypes().iterator().next(),
          pred.isList(),
          pred.isOptional(),
          pred.getName()
        );
      } else {
        Set<String> types = new HashSet<>();
        for (String valueType : pred.getValueTypes()) {
          types.add(typesContainer.valueType(valueType));
        }
        typesContainer.unionField(
          fieldName, null, types, pred.getName(), pred.getDirection(), pred.isOptional(), pred.isList()
        );
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        typesContainer.objectField(
          fieldName,
          null,
          pred.getName(),
          pred.getDirection(),
          typesContainer.objectType(pred.getReferenceTypes().iterator().next()),
          pred.isList(),
          pred.isOptional()
        );
      } else {
        Set<String> refs = new HashSet<>();
        for (String referenceType : pred.getReferenceTypes()) {
          refs.add(typesContainer.objectType(referenceType));
        }
        for (String valueType : pred.getValueTypes()) {
          refs.add(typesContainer.valueType(valueType));
        }

        typesContainer.unionField(
          fieldName,
          null,
          refs,
          pred.getName(),
          pred.getDirection(),
          pred.isOptional(),
          pred.isList()
        );
      }
    }
  }


}
