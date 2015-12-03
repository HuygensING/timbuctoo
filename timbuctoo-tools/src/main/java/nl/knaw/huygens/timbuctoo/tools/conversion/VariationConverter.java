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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Vertex;

public class VariationConverter {

  private ElementConverterFactory converterFactory;

  public VariationConverter(TypeRegistry typeRegistry) {
    this(new ElementConverterFactory(typeRegistry));
  }

  VariationConverter(ElementConverterFactory converterFactory) {
    this.converterFactory = converterFactory;
  }

  public <T extends DomainEntity> void addDataToVertex(Vertex vertex, T variant) throws ConversionException {
    getConverter(variant).addValuesToElement(vertex, variant);
  }

  @SuppressWarnings("unchecked")
  private <T extends DomainEntity> VertexConverter<T> getConverter(T variant) {
    return converterFactory.forType((Class<T>) variant.getClass());
  }

}
