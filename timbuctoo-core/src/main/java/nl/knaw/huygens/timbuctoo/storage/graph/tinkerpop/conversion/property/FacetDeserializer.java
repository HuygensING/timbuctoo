package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

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

class FacetDeserializer extends JsonDeserializer<Facet> {

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
