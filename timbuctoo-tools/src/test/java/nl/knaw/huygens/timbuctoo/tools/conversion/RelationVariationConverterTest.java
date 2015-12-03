package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
