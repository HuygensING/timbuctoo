package nl.knaw.huygens.timbuctoo.storage;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetOption;
import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.facetedsearch.model.RangeFacet;

import java.io.IOException;
import java.util.Iterator;

public class FacetDeserializer extends JsonDeserializer<Facet> {

  @Override
  public Facet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode treeNode = jp.readValueAsTree();
    if(FacetType.RANGE.toString().equals(treeNode.get("type").asText())){
      return createRangeFacet(treeNode);
    }
    return createDefaultFacet(treeNode);
  }

  protected RangeFacet createRangeFacet(JsonNode treeNode) {
    ArrayNode optionsNode = (ArrayNode) treeNode.get("options");
    RangeFacet rangeFacet = new RangeFacet( //
      treeNode.get("name").asText(), //
      treeNode.get("title").asText(), //
      getLowerLimit(optionsNode), //
      getUpprtLimit(optionsNode));

    return rangeFacet;
  }

  private long getUpprtLimit(ArrayNode optionsNode) {
    return optionsNode.iterator().next().get("upperLimit").asLong();
  }

  private long getLowerLimit(ArrayNode optionsNode) {
    return optionsNode.iterator().next().get("lowerLimit").asLong();
  }

  protected Facet createDefaultFacet(JsonNode treeNode) {
    DefaultFacet defaultFacet = new DefaultFacet(treeNode.get("name").asText(), treeNode.get("title").asText());

    ArrayNode options = (ArrayNode) treeNode.get("options");

    for(Iterator<JsonNode> iterator = options.iterator(); iterator.hasNext();){
      JsonNode option = iterator.next();
      defaultFacet.addOption(createOption(option));
    }


    return defaultFacet;
  }

  private FacetOption createOption(JsonNode option) {
    return new FacetOption(option.get("name").asText(), option.get("count").asLong());
  }
}
