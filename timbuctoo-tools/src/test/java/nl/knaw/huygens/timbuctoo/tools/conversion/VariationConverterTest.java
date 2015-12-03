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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

public class VariationConverterTest {
  private ElementConverterFactory converterFactory;
  private VariationConverter instance;

  @Before
  public void setup() {
    converterFactory = mock(ElementConverterFactory.class);
    instance = new VariationConverter(converterFactory);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addDataToVertexUsesAElementConverterToAddTheData() throws ConversionException {
    VertexConverter<Person> converter = mock(VertexConverter.class);
    when(converterFactory.forType(Person.class)).thenReturn(converter);

    Person variant = new Person();
    Vertex vertex = mock(Vertex.class);

    // action
    instance.addDataToVertex(vertex, variant);

    // verify
    verify(converter).addValuesToElement(vertex, variant);
  }

}
