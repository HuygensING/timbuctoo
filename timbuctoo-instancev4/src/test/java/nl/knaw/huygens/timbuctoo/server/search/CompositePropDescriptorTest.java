package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CompositePropDescriptorTest {

  @Test
  public void getReturnsTheValueOfTheFirstPropDescriptionIfItIsNotNull() {
    PropDescriptor propDescriptor1 = mock(PropDescriptor.class);
    String valueOfDesc1 = "notNull";
    given(propDescriptor1.get(any(Vertex.class))).willReturn(valueOfDesc1);
    PropDescriptor propDescriptor2 = mock(PropDescriptor.class);
    CompositePropDescriptor instance = new CompositePropDescriptor(propDescriptor1, propDescriptor2);

    String value = instance.get(mock(Vertex.class));

    assertThat(value, is(valueOfDesc1));

    verify(propDescriptor1).get(any(Vertex.class));
    verifyZeroInteractions(propDescriptor2);
  }

  @Test
  public void getReturnsTheValueOfTheSecondPropDescriptionIfTheValueOfTheFirstOneItIsNull() {
    PropDescriptor propDescriptor1 = mock(PropDescriptor.class);
    given(propDescriptor1.get(any(Vertex.class))).willReturn(null);
    PropDescriptor propDescriptor2 = mock(PropDescriptor.class);
    String valueOfDesc2 = "notNull";
    given(propDescriptor2.get(any(Vertex.class))).willReturn(valueOfDesc2);
    CompositePropDescriptor instance = new CompositePropDescriptor(propDescriptor1, propDescriptor2);

    String value = instance.get(mock(Vertex.class));

    assertThat(value, is(valueOfDesc2));

    verify(propDescriptor1).get(any(Vertex.class));
    verify(propDescriptor2).get(any(Vertex.class));
  }
}
