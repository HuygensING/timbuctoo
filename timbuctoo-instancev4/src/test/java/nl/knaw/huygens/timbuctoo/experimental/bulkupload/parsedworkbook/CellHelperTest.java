package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.CellHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CellHelperTest {

  @Test
  public void testAddFailureCanAddCommentsWhenTheCellAlreadyContainsOne() throws Exception {
    Workbook wb = new XSSFWorkbook();
    final Sheet sheet = wb.createSheet();
    final Row row = sheet.createRow(0);
    final Cell cell = row.createCell(0);
    CellHelper.addFailure(cell, "a");
    CellHelper.addFailure(cell, "b");
    assertThat(cell.getCellComment().getString().toString(), is("a\n\nb"));
  }

  @Test
  public void addsCommentsToExistingCommentBoxes() throws Exception {
    final String filename = "addsCommentsToExistingCommentBoxes.xlsx";
    final XSSFWorkbook workbook = getWorkbook(filename);
    final XSSFCell cell = workbook.getSheetAt(0).getRow(0).getCell(0);
    CellHelper.addFailure(cell, "a");
    assertThat(
      cell.getCellComment().getString().toString(),
      is("Microsoft Office User:\nreeds-bestaande-comment\n\na")
    );
  }

  private XSSFWorkbook getWorkbook(String filename) throws IOException {
    return new XSSFWorkbook(getClass().getResource(filename).getFile());
  }
}
