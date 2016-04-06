package nl.knaw.huygens.timbuctoo.bulkupload;

import org.apache.poi.ss.usermodel.Workbook;

public class InvalidWorkbookException extends Exception {
  public InvalidWorkbookException(Workbook validationResult) {

  }
}
