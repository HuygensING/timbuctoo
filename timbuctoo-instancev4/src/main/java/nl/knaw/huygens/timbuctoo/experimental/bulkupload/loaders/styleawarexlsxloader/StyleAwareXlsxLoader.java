package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.BulkLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import org.apache.poi.xssf.eventusermodel.XSSFReader;

/*

this class:

 - iterates over the workbook and sends the value to the current responder
 - does workbook specific parsing (determining if what valuetype is in a cell)
 - when a responder returns a new responder it will link it to the correct columns
 - it writes the errors to the sheet
*/

public class StyleAwareXlsxLoader implements BulkLoader<XSSFReader> {

  public void loadWorkbookAndMarkErrors(XSSFReader wb, Importer importer) {
    //HashMap<Integer, State> registeredActions = new HashMap<>();
    //final HashMap<Long, String> nameForId = new HashMap<>();
    //
    //final CTCellStyles cellStyles = wb.getStylesSource()
    //  .getCTStylesheet()
    //  .getCellStyles();
    //
    //for (CTCellStyle style : cellStyles.getCellStyleList()) {
    //  nameForId.put(style.getXfId(), style.getName());
    //}
    //
    //for (Sheet sheet : wb) {
    //  for (Row row : sheet) {
    //    for (Cell cell : row) {
    //      applyAction((XSSFCell) cell, nameForId, rootState, registeredActions);
    //    }
    //  }
    //}
  }
  //
  //private void applyAction(XSSFCell cell, HashMap<Long, String> styleLookup, Importer importer,
  //                         HashMap<Integer, State> registeredActions) {
  //  final int columnIndex = cell.getColumnIndex();
  //  final State currentState;
  //
  //  if (registeredActions.containsKey(columnIndex)) {
  //    currentState = registeredActions.get(columnIndex);
  //  } else {
  //    currentState = importer;
  //  }
  //
  //  final Optional<State> nextState = currentState.respondToContents(
  //    CellHelper.getValueAsStringAndMarkError(cell, "Errors cannot be imported").orElse(""),
  //    getValueTypeFor(cell, styleLookup),
  //    () -> CellHelper.addSuccess(cell),
  //    error -> CellHelper.addFailure(cell, error)
  //  );
  //  if (nextState.isPresent()) {
  //    //FIXME: multicolumn cells
  //    registeredActions.put(cell.getColumnIndex(), nextState.get());
  //  }
  //}
  //
  //private ValueType getValueTypeFor(XSSFCell cell, HashMap<Long, String> styleLookup) {
  //  final long xfId = cell.getCellStyle().getCoreXf().getXfId();
  //  final String styleName = styleLookup.get(xfId);
  //  switch (styleName) {
  //    case "timbuctooCollectionName":
  //      return ValueType.COLLECTIONHEADER;
  //    case "timbuctooIdentityColumn":
  //      return ValueType.IDENTITY_PROPERTYNAME;
  //    case "timbuctooPropertyName":
  //      return ValueType.PROPERTYNAME;
  //    case "timbuctooValue":
  //      return ValueType.VALUE;
  //    default:
  //      return ValueType.NONE;
  //  }
  //}

}
