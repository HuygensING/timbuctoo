package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import java.util.List;

public interface ExcelDescription {

  // Get the total amount of rows this property value takes up
  int getRows();

  // Get the total amount of cols this property value takes up
  int getCols();

  // Returns the property type
  String getType();

  // Returns the cell data
  String[][] getCells();

  // Returns the amount of columns one property value takes up
  int getValueWidth();

  // Contains a list describing what the values in each column are.
  // Can be list indices for lists, or targetCollection names for relations
  List<String> getValueDescriptions();
}
