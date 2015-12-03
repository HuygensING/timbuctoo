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
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Edge;

public class RelationVariationConverter {

  private ElementConverterFactory converterFactory;

  public RelationVariationConverter(TypeRegistry typeRegistry) {
    this(new ElementConverterFactory(typeRegistry));
  }

  RelationVariationConverter(ElementConverterFactory converterFactory) {
    this.converterFactory = converterFactory;
  }

  public <T extends Relation> void addToEdge(Edge edge, T variant) throws ConversionException {
    EdgeConverter<T> converter = getConverterFor(variant);
    converter.addValuesToElement(edge, variant);
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> EdgeConverter<T> getConverterFor(T variant) {
    return converterFactory.forRelation((Class<T>) variant.getClass());
  }

}
