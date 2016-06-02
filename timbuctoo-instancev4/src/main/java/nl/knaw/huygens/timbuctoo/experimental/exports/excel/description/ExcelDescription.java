package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

public interface ExcelDescription {

  int getRows();

  int getCols();

  String getType();

  String[][] getCells();

  int getValueWidth();
}
