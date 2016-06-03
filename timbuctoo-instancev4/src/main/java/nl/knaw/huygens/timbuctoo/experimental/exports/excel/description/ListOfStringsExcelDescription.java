package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import java.util.List;


public class ListOfStringsExcelDescription implements ExcelDescription {
  private static final int VALUE_WIDTH = 1;
  private final ArrayNode value;
  private String type;

  public ListOfStringsExcelDescription(ArrayNode jsonValue, String typeId) {
    this.value = jsonValue;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    return 1;
  }

  @Override
  public int getCols() {
    return value.size();
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[][] getCells() {
    List<String> result = Lists.newArrayList();
    for (JsonNode node : value) {
      result.add(node.asText());
    }

    return new String[][] {
      result.toArray(new String[value.size()])
    };
  }

  @Override
  public int getValueWidth() {
    return VALUE_WIDTH;
  }

  @Override
  public List<String> getValueDescriptions() {
    List<String> result = Lists.newArrayList();

    for (int i = 0; i < value.size(); i++) {
      result.add(Integer.toString(i + 1));
    }

    return result;
  }
}
