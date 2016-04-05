package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.LinkedHashMap;

public class ParsedWorkbook {

  public LinkedHashMap<String, CollectionSheet> worksheets = new LinkedHashMap<>();

  public static ParsedWorkbook from(Workbook wb) {
    return new ParsedWorkbook();
  }

  public CollectionSheet withSheet(String name) {
    CollectionSheet sheet = new CollectionSheet();
    worksheets.put(name, sheet);
    return sheet;
  }

  public Workbook asWorkBook() {
    XSSFWorkbook wb = new XSSFWorkbook();
    CreationHelper createHelper = wb.getCreationHelper();
    worksheets.forEach((name, parsedSheet) -> {
      parsedSheet.asWorkSheet(wb.createSheet(WorkbookUtil.createSafeSheetName(name)), createHelper);
    });

    return wb;
  }

}
