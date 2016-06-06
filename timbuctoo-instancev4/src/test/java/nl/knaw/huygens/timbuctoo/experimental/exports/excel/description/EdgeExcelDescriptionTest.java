package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;


import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EdgeExcelDescriptionTest {


  @Test
  public void mapsEdgesToTheCorrectCells() {
    final String typesJson = jsnA(jsn("person"), jsn("wwperson")).toString();
    Edge edge1 = mock(Edge.class);
    Edge edge2 = mock(Edge.class);
    Edge edge3 = mock(Edge.class);
    Vertex vertex1 = mock(Vertex.class);
    Vertex vertex2 = mock(Vertex.class);
    Vertex vertex3 = mock(Vertex.class);
    VertexProperty<Object> typesProp = mock(VertexProperty.class);
    given(typesProp.value()).willReturn(typesJson);
    Iterator<VertexProperty<Object>> typesPropIt = mock(Iterator.class);
    given(typesPropIt.hasNext()).willReturn(true);
    given(typesPropIt.next()).willReturn(typesProp);
    given(vertex1.properties("types")).willReturn(typesPropIt);
    given(edge1.inVertex()).willReturn(vertex1);
    given(edge2.inVertex()).willReturn(vertex2);
    given(edge3.inVertex()).willReturn(vertex3);
    given(vertex1.value("tim_id")).willReturn("id1");
    given(vertex2.value("tim_id")).willReturn("id2");
    given(vertex3.value("tim_id")).willReturn("id3");
    List<Edge> edges = Lists.newArrayList(edge1, edge2, edge3);

    ExcelDescription instance = new EdgeExcelDescription(edges, HuygensIng.mappings, "WomenWriters");

    assertThat(instance.getCols(), equalTo(3));
    assertThat(instance.getRows(), equalTo(1));
    assertThat(instance.getType(), equalTo("relation"));
    assertThat(instance.getValueWidth(), equalTo(1));
    assertThat(instance.getValueDescriptions(), contains("wwpersons", "wwpersons", "wwpersons"));

    assertThat(instance.getCells(), equalTo(new String[][] {
      {"id1", "id2", "id3"}
    }));


  }


}
