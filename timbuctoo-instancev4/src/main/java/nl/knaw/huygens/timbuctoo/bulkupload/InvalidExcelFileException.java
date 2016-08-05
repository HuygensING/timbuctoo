package nl.knaw.huygens.timbuctoo.bulkupload;

public class InvalidExcelFileException extends Exception {
  public InvalidExcelFileException(Exception inner) {
    super(inner);
  }
}
