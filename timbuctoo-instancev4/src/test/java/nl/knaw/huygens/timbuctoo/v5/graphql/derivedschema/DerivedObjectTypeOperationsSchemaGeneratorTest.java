package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DerivedObjectTypeOperationsSchemaGeneratorTest {
  @Ignore
  @Test
  public void addsEditMethodToType() {
    String uri = "http://example.org/type";
    String rootType = "RootType";
    GraphQlNameGenerator graphQlNameGenerator = mock(GraphQlNameGenerator.class);
    when(graphQlNameGenerator.createObjectTypeName(rootType, uri)).thenReturn("Type");
    DerivedObjectTypeOperationsSchemaGenerator instance =
      new DerivedObjectTypeOperationsSchemaGenerator(uri, rootType, graphQlNameGenerator);

    String schema = instance.getSchema().toString();

    assertThat(schema, is("type TypeMutations {\n" +
      "  edit(uri: String! entity: TypeInput!): Type\n" +
      "}\n"));
  }

}
