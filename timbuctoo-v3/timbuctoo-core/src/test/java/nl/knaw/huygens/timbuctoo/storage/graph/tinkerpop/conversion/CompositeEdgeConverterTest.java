package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

/*
 * #%L
 * Timbuctoo core
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;

public class CompositeEdgeConverterTest {
  private EdgeConverter<SubARelation> edgeConverter1;
  private EdgeConverter<SubARelation> edgeConverter2;
  private CompositeEdgeConverter<SubARelation> instance;
  private SubARelation entity;
  private Edge edgeMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    edgeConverter1 = mock(EdgeConverter.class);
    edgeConverter2 = mock(EdgeConverter.class);

    List<EdgeConverter<? super SubARelation>> delegates = Lists.newArrayList();
    delegates.add(edgeConverter1);
    delegates.add(edgeConverter2);

    instance = new CompositeEdgeConverter<SubARelation>(delegates);

    entity = new SubARelation();
    edgeMock = mock(Edge.class);
  }

  @Test
  public void addValuesToVertexDelegatesToTheWrappedVertexConverters() throws Exception {

    // action
    instance.addValuesToElement(edgeMock, entity);

    // verify
    verify(edgeConverter1).addValuesToElement(edgeMock, entity);
    verify(edgeConverter2).addValuesToElement(edgeMock, entity);

  }

  @Test(expected = ConversionException.class)
  public void addValuesToVertexThrowsAConversionExceptionWhenOneOfTheDelegatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(edgeConverter1).addValuesToElement(edgeMock, entity);

    // action
    instance.addValuesToElement(edgeMock, entity);

  }
}
