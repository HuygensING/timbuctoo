package nl.knaw.huygens.timbuctoo.experimental.bulkupload;

public class InvalidExcelFileException extends Exception {
  public InvalidExcelFileException(Exception inner) {
    super(inner);
  }
}
