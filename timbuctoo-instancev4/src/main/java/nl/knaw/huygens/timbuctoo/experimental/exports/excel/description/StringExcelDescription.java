package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.google.common.collect.Lists;

import java.util.List;


public class StringExcelDescription implements ExcelDescription {


  public static final int VALUE_WIDTH = 1;
  private final String value;
  private String type;

  public StringExcelDescription(String value, String typeId) {
    this.value = value;
    this.type = typeId;
  }

  @Override
  public int getRows() {
    return 1;
  }

  @Override
  public int getCols() {
    return VALUE_WIDTH;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String[][] getCells() {
    return new String[][] {
      { value }
    };
  }

  @Override
  public int getValueWidth() {
    return VALUE_WIDTH;
  }

  @Override
  public List<String> getValueDescriptions() {

    return Lists.newArrayList("");
  }
}
