package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel;

import nl.knaw.huygens.timbuctoo.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.BulkLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.ResultHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public abstract class XlsxLoader implements BulkLoader<InputStream, String> {

  @Override
  public String loadData(InputStream source, Importer importer) throws InvalidExcelFileException {
    try {
      XSSFWorkbook workbook = new XSSFWorkbook(source);/* {
        public void parseSheet(Map<String, XSSFSheet> shIdMap, CTSheet ctSheet) {
          //don't parse sheets
        }
      }*/
      XSSFReader xssfReader = new XSSFReader(workbook.getPackage());
      ResultHandler handler = new ResultHandler();

      SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      saxFactory.setNamespaceAware(true);
      final SharedStringsTable sharedStringsTable = xssfReader.getSharedStringsTable();
      RowCellHandler rowCellHandler = makeRowCellHandler(workbook, importer, handler);

      XSSFReader.SheetIterator worksheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
      while (worksheets.hasNext()) {
        final InputStream sheet = worksheets.next();

        XMLReader sheetParser = saxFactory.newSAXParser().getXMLReader();
        sheetParser.setContentHandler(new SheetXmlParser(sharedStringsTable, rowCellHandler));

        rowCellHandler.start(worksheets.getSheetName());
        sheetParser.parse(new InputSource(sheet));
        rowCellHandler.finish();
      }
      return handler.endImport();
    } catch (SAXException | IOException | OpenXML4JException | ParserConfigurationException e) {
      throw new InvalidExcelFileException(e);
    }
  }

  protected abstract RowCellHandler makeRowCellHandler(XSSFWorkbook workbook, Importer importer, ResultHandler handler);
}
