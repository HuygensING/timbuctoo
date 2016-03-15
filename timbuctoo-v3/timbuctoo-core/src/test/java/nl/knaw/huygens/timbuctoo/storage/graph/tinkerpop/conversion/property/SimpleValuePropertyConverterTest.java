package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

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

import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.PropertyConverterTest;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleValuePropertyConverterTest implements PropertyConverterTest {
  private static final String FIELD_VALUE = "test";
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  private static final String FIELD_NAME = "stringValue";
  public static final String PROPERTY_NAME = "propertyName";
  private static final String COMPLETE_PROPERTY_NAME = FIELD_TYPE.completePropertyName(TYPE, PROPERTY_NAME);
  private SimpleValuePropertyConverter instance;
  private TestSystemEntityWrapper entity;
  private Vertex vertexMock;

  @Before
  public void setup() throws NoSuchFieldException {
    instance = new SimpleValuePropertyConverter();
    setupInstance(instance);

    entity = new TestSystemEntityWrapper();

    vertexMock = mock(Vertex.class);
  }

  private void setupInstance(SimpleValuePropertyConverter instance) throws NoSuchFieldException {
    instance.setContainingType(TYPE);
    instance.setField(TYPE.getDeclaredField(FIELD_NAME));
    instance.setFieldType(FIELD_TYPE);
    instance.setFieldName(FIELD_NAME);
    instance.setPropertyName(PROPERTY_NAME);
  }

  @Override
  @Test
  public void setPropertyOfElementSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    entity.setStringValue(FIELD_VALUE);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(COMPLETE_PROPERTY_NAME, FIELD_VALUE);
  }

  @Override
  @Test
  public void setPropertyOfElementRemovesThePropertyIfTheValueIsNull() throws Exception {
    // setup
    entity.setStringValue(null);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).removeProperty(COMPLETE_PROPERTY_NAME);
  }

  @Override
  @Test(expected = ConversionException.class)
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    // action
    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Override
  @Test(expected = ConversionException.class)
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test
  @Override
  public void addValueToEntitySetTheFieldOfTheEntityWithTheValue() throws Exception {
    // setup
    when(vertexMock.getProperty(COMPLETE_PROPERTY_NAME)).thenReturn(FIELD_VALUE);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getStringValue(), is(equalTo(FIELD_VALUE)));
  }

  @Test
  @Override
  public void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception {
    // setup
    when(vertexMock.getProperty(COMPLETE_PROPERTY_NAME)).thenReturn(null);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getStringValue(), is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    // setup
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };
    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, vertexMock);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnAnIllegalArgumentExceptionIsThrown() throws Exception {
    SimpleValuePropertyConverter instance = new SimpleValuePropertyConverter() {
      @Override
      protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };
    setupInstance(instance);

    // action
    instance.addValueToEntity(entity, vertexMock);
  }

  @Test
  public void removeFromRemovesThePropertyFromTheElement() {
    // action
    instance.removeFrom(vertexMock);

    // verify
    verify(vertexMock).removeProperty(COMPLETE_PROPERTY_NAME);
  }

}
