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

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class RevisionConverter {

  private Graph graph;
  private VariationConverter variationConverter;
  private ConversionVerifierFactory verifierFactory;

  public RevisionConverter(Graph graph, VariationConverter variationConverter, ConversionVerifierFactory verifierFactory) {
    this.graph = graph;
    this.variationConverter = variationConverter;
    this.verifierFactory = verifierFactory;
  }

  public <T extends DomainEntity> Vertex convert(String oldId, String newId, List<T> variations, int revision) throws IllegalArgumentException, IllegalAccessException, StorageException {
    Vertex vertex = graph.addVertex(null);
    List<Class<? extends DomainEntity>> variantTypes = Lists.newArrayList();
    for (T variant : variations) {
      variant.setId(newId);
      variationConverter.addDataToVertex(vertex, variant);
      variantTypes.add(variant.getClass());
    }

    for (Class<? extends DomainEntity> type : variantTypes) {
      EntityConversionVerifier verifier = verifierFactory.createFor(type, revision);
      verifier.verifyConversion(oldId, newId, vertex.getId());
    }

    return vertex;

  }
}
