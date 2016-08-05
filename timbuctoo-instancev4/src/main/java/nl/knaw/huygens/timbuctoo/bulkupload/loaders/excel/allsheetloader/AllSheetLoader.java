package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.ResultHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.RowCellHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.XlsxLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AllSheetLoader extends XlsxLoader {
  @Override
  protected RowCellHandler makeRowCellHandler(XSSFWorkbook workbook, Importer importer, ResultHandler handler) {
    return new AllCellRowCellHandler(importer, handler);
  }
}
