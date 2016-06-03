package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import java.util.List;

public interface ExcelDescription {

  int getRows();

  int getCols();

  String getType();

  String[][] getCells();

  int getValueWidth();

  List<String> getValueDescriptions();
}
