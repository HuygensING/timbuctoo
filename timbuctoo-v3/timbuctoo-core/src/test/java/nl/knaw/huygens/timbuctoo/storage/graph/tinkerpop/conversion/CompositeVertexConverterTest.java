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
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class CompositeVertexConverterTest {
  private VertexConverter<SubADomainEntity> vertexConverter1;
  private VertexConverter<SubADomainEntity> vertexConverter2;
  private CompositeVertexConverter<SubADomainEntity> instance;
  private SubADomainEntity entity;
  private Vertex vertexMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    vertexConverter1 = mock(VertexConverter.class);
    vertexConverter2 = mock(VertexConverter.class);

    List<VertexConverter<? super SubADomainEntity>> delegates = Lists.newArrayList();
    delegates.add(vertexConverter1);
    delegates.add(vertexConverter2);

    instance = new CompositeVertexConverter<>(delegates);

    entity = new SubADomainEntity();
    vertexMock = mock(Vertex.class);
  }

  @Test
  public void addValuesToVertexDelegatesToTheWrappedVertexConverters() throws Exception {

    // action
    instance.addValuesToElement(vertexMock, entity);

    // verify
    verify(vertexConverter1).addValuesToElement(vertexMock, entity);
    verify(vertexConverter2).addValuesToElement(vertexMock, entity);

  }

  @Test(expected = ConversionException.class)
  public void addValuesToVertexThrowsAConversionExceptionWhenOneOfTheDelegatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(vertexConverter1).addValuesToElement(vertexMock, entity);

    // action
    instance.addValuesToElement(vertexMock, entity);

  }
}
