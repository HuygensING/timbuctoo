package nl.knaw.huygens.timbuctoo.experimental.exports;

public interface ExcelDescription {

  int getRows();

  int getCols();

  String getType();

  String[][] getCells();
}
