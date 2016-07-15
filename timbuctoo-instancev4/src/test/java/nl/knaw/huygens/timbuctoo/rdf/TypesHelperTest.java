package nl.knaw.huygens.timbuctoo.rdf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TypesHelperTest {
  @Test
  public void updateTypeInformationGeneratesANewTypesPropertyFromTheCollections() throws Exception {
    Collection collection1 = mock(Collection.class);
    when(collection1.getDescription()).thenReturn(new CollectionDescription("entityTypeName", "vreName"));
    Collection collection2 = mock(Collection.class);
    when(collection2.getDescription()).thenReturn(new CollectionDescription("otherEntityType", "vreName"));
    TypesHelper instance = new TypesHelper();
    Vertex vertex = mock(Vertex.class);

    instance.updateTypeInformation(vertex, Sets.newHashSet(collection1, collection2));

    ArgumentCaptor<String> typesCaptor = ArgumentCaptor.forClass(String.class);
    verify(vertex).property(argThat(is("types")), typesCaptor.capture());
    List<String> typesList = new ObjectMapper().readValue(typesCaptor.getValue(), new TypeReference<List<String>>() {
    });
    // TODO Check if archetype is added
    assertThat(typesList, containsInAnyOrder("entityTypeName", "otherEntityType"));
  }


}
