package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;

import java.util.List;

public class AltNamesExcelDescription implements ExcelDescription {
  public static final int VALUE_WIDTH = 2;
  private final ArrayNode value;
  private final String type;

  public AltNamesExcelDescription(JsonNode json, String typeId) {

    this.value = (ArrayNode) json;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    return 2;
  }

  @Override
  public int getCols() {
    return value.size() * VALUE_WIDTH;
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public String[][] getCells() {

    // -------------------------------------------------------
    // |             1            |             2            |
    // -------------------------------------------------------
    // | nametype    | any string | nametype    | any string |
    // | displayName | Namepart   | displayName | Namepart   |
    // -------------------------------------------------------

    List<String> typeRow = Lists.newArrayList();
    List<String> displayNameRow = Lists.newArrayList();

    for (JsonNode node : value) {
      typeRow.addAll(Lists.newArrayList("nametype", node.get("nametype").asText()));
      displayNameRow.addAll(Lists.newArrayList("displayName", node.get("displayName").asText()));
    }

    return new String[][]{
      typeRow.toArray(new String[getCols()]),
      displayNameRow.toArray(new String[getCols()])
    };
  }

  @Override
  public int getValueWidth() {
    return VALUE_WIDTH;
  }
}
