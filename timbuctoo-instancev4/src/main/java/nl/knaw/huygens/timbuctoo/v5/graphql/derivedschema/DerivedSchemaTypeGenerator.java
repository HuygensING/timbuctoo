package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DerivedSchemaTypeGenerator {

  private final PaginationArgumentsHelper argumentsHelper;

  public DerivedSchemaTypeGenerator(
    PaginationArgumentsHelper argumentsHelper) {
    this.argumentsHelper = argumentsHelper;
  }

  public String makeGraphQlTypes(String rootType, Map<String, Type> types, TypeNameStore nameStore) {
    GraphQlTypesContainer typesContainer = new GraphQlTypesContainer(rootType, nameStore, this.argumentsHelper);

    for (Type type : types.values()) {
      typesContainer.openObjectType(type.getName());
      for (Predicate predicate : type.getPredicates()) {
        fieldForDerivedType(predicate, typesContainer);
      }
      typesContainer.closeObjectType(type.getName());
    }
    return typesContainer.getSchema();
  }

  private static void fieldForDerivedType(Predicate pred, GraphQlTypesContainer typesContainer) {
    if (pred.getReferenceTypes().size() == 0) {
      if (pred.getValueTypes().size() == 0) {
        System.out.println("This shouldn't happen! The predicate has no value types and no reference types!");
      } else if (pred.getValueTypes().size() == 1) {
        typesContainer.valueField(
          null,
          pred,
          pred.getUsedValueTypes().iterator().next()
        );
      } else {
        Set<String> types = new HashSet<>();
        for (String valueType : pred.getUsedValueTypes()) {
          types.add(typesContainer.valueType(valueType));
        }
        typesContainer.unionField(null, pred, types);
      }
    } else {
      if (pred.getReferenceTypes().size() == 1 && pred.getValueTypes().size() == 0) {
        typesContainer.objectField(
          null,
          pred,
          typesContainer.getObjectTypeName(pred.getUsedReferenceTypes().iterator().next())
        );
      } else {
        Set<String> refs = new HashSet<>();
        for (String referenceType : pred.getUsedReferenceTypes()) {
          refs.add(typesContainer.getObjectTypeName(referenceType));
        }
        for (String valueType : pred.getUsedValueTypes()) {
          refs.add(typesContainer.valueType(valueType));
        }

        typesContainer.unionField(
          null,
          pred,
          refs
        );
      }
    }
  }


}
