package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;

import java.util.List;

public class HyperlinksExcelDescription implements ExcelDescription {

  private final ArrayNode value;
  private final String type;

  public HyperlinksExcelDescription(ArrayNode jsonValue, String typeId) {
    this.value = jsonValue;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    // ----------------------
    // | label | any string |
    // | url   | any uri    |
    // | label | ...        |
    // | url   | ...        |
    // ----------------------
    return value.size() * 2;
  }

  @Override
  public int getCols() {
    return 2;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[][] getCells() {
    List<String[]> result = Lists.newArrayList();

    for (JsonNode node : value) {
      result.add(new String[] {"label", node.get("label").asText()});
      result.add(new String[] {"url", node.get("url").asText()});
    }

    return result.toArray(new String[result.size()][]);
  }
}
