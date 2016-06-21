package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.excel.allsheetloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.ResultHandler;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.excel.RowCellHandler;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;

import java.util.HashMap;

public class AllCellRowCellHandler implements RowCellHandler {
  private final Importer importer;
  private final ResultHandler handler;
  private boolean entityStarted;
  private boolean headerRow = false;

  public AllCellRowCellHandler(Importer importer, ResultHandler handler) {
    this.importer = importer;
    this.handler = handler;
  }

  @Override
  public void start(String name) {
    entityStarted = false;
    headerRow = true;
    handler.startSheet(name, importer.startCollection(name));
  }

  @Override
  public void startRow(int rowNum) {
    handler.startRow();
  }

  @Override
  public void cell(short column, String value, String cellStyleStr) {
    if (headerRow) {
      handler.handle(column, value, importer.registerPropertyName(column, value));
    } else {
      if (!entityStarted) {
        entityStarted  = true;
        importer.startEntity();
      }
      handler.handle(column, value, importer.setValue(column, value));
    }
  }

  @Override
  public void endRow(int rowNum) {
    if (headerRow) {
      headerRow = false;
      handler.startValuePart();
    }
    if (entityStarted) {
      entityStarted = false;
      handler.endRow(importer.finishEntity());
    } else {
      handler.endRow(new HashMap<>());
    }

  }

  @Override
  public void finish() {
    importer.finishCollection();
    handler.endSheet();
  }
}
