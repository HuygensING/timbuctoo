package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;

import java.util.List;

public class AltNamesExcelDescription implements ExcelDescription {
  private final ArrayNode jsonValue;
  private final String type;

  public AltNamesExcelDescription(JsonNode json, String typeId) {

    this.jsonValue = (ArrayNode) json;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    // ----------------------------
    // | nametype    | any string |
    // | displayName | Namepart   |
    // | nametype    | ...        |
    // | displayName | ...        |
    // ----------------------------
    return jsonValue.size() * 2;
  }

  @Override
  public int getCols() {
    return 2;
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public String[][] getCells() {
    List<String[]> result = Lists.newArrayList();

    for (JsonNode node : jsonValue) {
      result.add(new String[] {"nametype", node.get("nametype").asText()});
      result.add(new String[] {"displayName", node.get("displayName").asText()});
    }

    return result.toArray(new String[result.size()][]);
  }
}
