package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.InvalidExcelFileException;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.BulkLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
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

public class StyleAwareXlsxLoader implements BulkLoader<InputStream, String> {

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
      final RowCellHandler rowCellHandler = new RowCellHandler(importer, handler);
      final StylesMapper stylesMapper = new StylesMapper(workbook.getStylesSource());

      XSSFReader.SheetIterator worksheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
      while (worksheets.hasNext()) {
        final InputStream sheet = worksheets.next();

        XMLReader sheetParser = saxFactory.newSAXParser().getXMLReader();
        sheetParser.setContentHandler(new SheetXmlParser(stylesMapper, sharedStringsTable, rowCellHandler));

        rowCellHandler.start(worksheets.getSheetName());
        sheetParser.parse(new InputSource(sheet));
        rowCellHandler.finish();
      }
      return handler.endImport(importer.finishImport());
    } catch (SAXException | IOException | OpenXML4JException | ParserConfigurationException e) {
      throw new InvalidExcelFileException(e);
    }
  }
}
