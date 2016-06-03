package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import java.util.List;

public class HyperlinksExcelDescription implements ExcelDescription {

  public static final int VALUE_WIDTH = 2;
  private final ArrayNode value;
  private final String type;

  public HyperlinksExcelDescription(ArrayNode jsonValue, String typeId) {
    this.value = jsonValue;
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
    return type;
  }

  @Override
  public String[][] getCells() {


    // -------------------------------------------
    // |       1            |       2            |
    // -------------------------------------------
    // | label | any string | label | any string |
    // | url   | any url    | url   | any url    |
    // -------------------------------------------

    List<String> labelRow = Lists.newArrayList();
    List<String> urlRow = Lists.newArrayList();

    for (JsonNode node : value) {
      labelRow.addAll(Lists.newArrayList("label", node.get("label").asText()));
      urlRow.addAll(Lists.newArrayList("url", node.get("url").asText()));
    }

    return new String[][]{
      labelRow.toArray(new String[getCols()]),
      urlRow.toArray(new String[getCols()])
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
