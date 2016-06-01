package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;


public class StringExcelDescription implements ExcelDescription {


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
    return 1;
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
}
