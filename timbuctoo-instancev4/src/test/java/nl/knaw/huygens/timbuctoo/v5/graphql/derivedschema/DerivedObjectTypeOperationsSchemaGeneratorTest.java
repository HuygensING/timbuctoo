package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
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
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    String uri = "http://example.org/type";
    when(typeNameStore.makeGraphQlname(uri)).thenReturn("Type");
    String rootType = "RootType";
    DerivedObjectTypeOperationsSchemaGenerator instance =
      new DerivedObjectTypeOperationsSchemaGenerator(uri, typeNameStore, rootType);

    String schema = instance.getSchema().toString();

    assertThat(schema, is("type RootType_Type {\n" +
      "  edit(uri: String! entity: RootType_TypeInput!): RootType_Type\n" +
      "}\n"));
  }

}
