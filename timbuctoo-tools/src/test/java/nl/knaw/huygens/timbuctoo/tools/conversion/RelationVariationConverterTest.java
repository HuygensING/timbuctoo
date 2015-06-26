package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;

public class RelationVariationConverterTest {
  private ElementConverterFactory converterFactory;
  private RelationVariationConverter instance;

  @Before
  public void setup() {
    converterFactory = mock(ElementConverterFactory.class);
    instance = new RelationVariationConverter(converterFactory);
  }

  @Test
  public void addToEdgeLetsAEdgeConverterAddAllThePropertiesToTheEdge() throws Exception {

    EdgeConverter<Relation> edgeConverter = edgeConverter();

    // setup
    Relation variant = new Relation();
    Edge edge = mock(Edge.class);

    // action
    instance.addToEdge(edge, variant);

    // verify
    edgeConverter.addValuesToElement(edge, variant);
  }

  @SuppressWarnings("unchecked")
  private EdgeConverter<Relation> edgeConverter() {
    EdgeConverter<Relation> edgeConverter = mock(EdgeConverter.class);
    when(converterFactory.forRelation(Relation.class)).thenReturn(edgeConverter);
    return edgeConverter;
  }

}
