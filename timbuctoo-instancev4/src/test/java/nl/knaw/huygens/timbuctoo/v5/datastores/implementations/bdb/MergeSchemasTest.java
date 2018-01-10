package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.PredicateMatcher.predicateMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

public class MergeSchemasTest {
  @Test
  public void mergeSchemaAddsCustomTypeToSchema() throws Exception {
    MergeSchemas mergeSchemas = new MergeSchemas();
    Map<String, Type> customSchema = new HashMap<>();
    customSchema.put("CustomType", new Type("TypeName"));

    Map<String, Type> mergedSchema = mergeSchemas.mergeSchema(new HashMap<>(), customSchema);

    assertThat(mergedSchema, hasEntry(is("CustomType"), hasProperty("name", is("TypeName"))));
  }

  @Test
  public void mergeSchemaAddsGeneratedTypeToSchema() throws Exception {
    MergeSchemas mergeSchemas = new MergeSchemas();
    Map<String, Type> generatedSchema = new HashMap<>();
    generatedSchema.put("GeneratedType", new Type("TypeName"));

    Map<String, Type> mergedSchema = mergeSchemas.mergeSchema(generatedSchema, new HashMap<>());

    assertThat(mergedSchema, hasEntry(is("GeneratedType"), hasProperty("name", is("TypeName"))));
  }

  @Test
  public void mergeSchemaCombinesPredicatesForSameType() throws Exception {
    MergeSchemas mergeSchemas = new MergeSchemas();
    Map<String, Type> generatedSchema = new HashMap<>();
    generatedSchema.put("Type", createTypeWithPredicate("generated", Direction.OUT));
    Map<String, Type> customSchema = new HashMap<>();
    customSchema.put("Type", createTypeWithPredicate("custom", Direction.IN));

    Map<String, Type> mergedSchema = mergeSchemas.mergeSchema(generatedSchema, customSchema);


    assertThat(mergedSchema, hasEntry(is("Type"), hasProperty("predicates", containsInAnyOrder(
      predicateMatcher().withName("generated").withDirection(Direction.OUT),
      predicateMatcher().withName("custom").withDirection(Direction.IN)
    ))));
  }

  private Type createTypeWithPredicate(String generated, Direction out) {
    Type generatedType = new Type("");
    generatedType.getOrCreatePredicate(generated, out);
    return generatedType;
  }


}
