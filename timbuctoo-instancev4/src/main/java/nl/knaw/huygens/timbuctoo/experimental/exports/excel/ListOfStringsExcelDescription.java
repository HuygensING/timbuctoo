package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;

import java.util.List;


public class ListOfStringsExcelDescription implements ExcelDescription {
  private final ArrayNode value;
  private String type;

  public ListOfStringsExcelDescription(ArrayNode jsonValue, String typeId) {
    this.value = jsonValue;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    return value.size();
  }

  @Override
  public int getCols() {
    return 1;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[][] getCells() {
    List<String[]> result = Lists.newArrayList();
    for (JsonNode node : value) {
      result.add(new String[] {node.asText()});
    }

    return result.toArray(new String[value.size()][]);
  }
}
