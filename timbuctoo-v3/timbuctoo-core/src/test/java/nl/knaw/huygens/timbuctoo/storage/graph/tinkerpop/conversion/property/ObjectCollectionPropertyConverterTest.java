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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.PropertyConverterTest;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ObjectCollectionPropertyConverterTest implements PropertyConverterTest{
  public static final Class<Change> COMPONENT_TYPE = Change.class;
  private List<Change> DEFAULT_VALUE = Lists.newArrayList(new Change(87l, "userId", "vreId"), new Change(88l, "userId", "vreId"));
  private static final FieldType FIELD_TYPE = FieldType.REGULAR;
  private static final String FIELD_NAME = "objectCollection";
  private static final Class<TestSystemEntityWrapper> TYPE = TestSystemEntityWrapper.class;
  public static final String PROPERTY_NAME = "propertyName";
  private TestSystemEntityWrapper entity;
  private String completePropertyName;
  private ObjectCollectionPropertyConverter<Change> instance;
  private Vertex vertexMock;

  @Before
  public void setUp() throws Exception {
    vertexMock = mock(Vertex.class);
    entity = new TestSystemEntityWrapper();
    completePropertyName = FIELD_TYPE.completePropertyName(TYPE, PROPERTY_NAME);

    instance = new ObjectCollectionPropertyConverter(COMPONENT_TYPE);
    setupInstance(instance);
  }

  private void setupInstance(ObjectCollectionPropertyConverter objectValueFieldWrapper) throws Exception {
    objectValueFieldWrapper.setContainingType(TYPE);
    objectValueFieldWrapper.setField(TYPE.getDeclaredField(FIELD_NAME));
    objectValueFieldWrapper.setFieldType(FIELD_TYPE);
    objectValueFieldWrapper.setFieldName(FIELD_NAME);
    objectValueFieldWrapper.setPropertyName(PROPERTY_NAME);
  }

  @Test
  @Override
  public void setPropertyOfElementSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception {
    // setup
    String serializedValue = serializeValue(DEFAULT_VALUE);

    entity.setObjectCollection(DEFAULT_VALUE);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).setProperty(completePropertyName, serializedValue);
  }

  private String serializeValue(List<Change> changes) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String serializedValue = objectMapper.writeValueAsString(changes);
    return serializedValue;
  }

  @Test
  @Override
  public void setPropertyOfElementRemovesThePropertyIfTheValueIsNull() throws Exception {
    // setup
    entity.setObjectValue(null);

    // action
    instance.setPropertyOfElement(vertexMock, entity);

    // verify
    verify(vertexMock).removeProperty(completePropertyName);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalAccessException() throws Exception {
    // setup
    ObjectCollectionPropertyConverter<Change> instance = new ObjectCollectionPropertyConverter<Change>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalAccessException();
      }
    };

    setupInstance(instance);

    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  @Override
  public void setPropertyOfElementThrowsAConversionExceptionIfGetFieldValueThrowsAnIllegalArgumentExceptionIsThrown() throws Exception {
    // setup
    ObjectCollectionPropertyConverter<Change> instance = new ObjectCollectionPropertyConverter<Change>(COMPONENT_TYPE) {
      @Override
      protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
        throw new IllegalArgumentException();
      }
    };

    setupInstance(instance);

    // action
    instance.setPropertyOfElement(vertexMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void setPropertyOfElementThrowsAConversionExceptionIfFormatThrowsAnIllegalArgumentException() throws Exception {
    // setup
    entity.setObjectCollection(DEFAULT_VALUE);

    ObjectCollectionPropertyConverter<Change> instance = new ObjectCollectionPropertyConverter<Change>(COMPONENT_TYPE) {
      @Override
      protected Object format(Object value) throws IllegalArgumentException {
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
    when(vertexMock.getProperty(completePropertyName)).thenReturn(serializeValue(DEFAULT_VALUE));

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getObjectCollection(), is(DEFAULT_VALUE));
  }

  @Test
  @Override
  public void addValueToEntityAddsNullWhenTheValueIsNull() throws Exception {
    // setup
    when(vertexMock.getProperty(completePropertyName)).thenReturn(null);

    // action
    instance.addValueToEntity(entity, vertexMock);

    // verify
    assertThat(entity.getObjectCollection(), is(nullValue()));
  }

  @Test(expected = ConversionException.class)
  @Override
  public void addValueToEntityThrowsAConversionExceptionWhenFillFieldThrowsAnIllegalAccessExceptionIsThrown() throws Exception {
    // setup
    ObjectCollectionPropertyConverter instance = new ObjectCollectionPropertyConverter<Change>(COMPONENT_TYPE) {
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
    // setup
    ObjectCollectionPropertyConverter instance = new ObjectCollectionPropertyConverter<Change>(COMPONENT_TYPE) {
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
    verify(vertexMock).removeProperty(completePropertyName);
  }
}
