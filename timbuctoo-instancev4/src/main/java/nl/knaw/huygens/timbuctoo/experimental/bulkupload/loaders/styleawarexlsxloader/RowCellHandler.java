package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;

import java.util.HashMap;

public class RowCellHandler {
  private final Importer importer;
  private final ResultHandler handler;
  private boolean entityStarted;

  public RowCellHandler(Importer importer, ResultHandler handler) {
    this.importer = importer;
    this.handler = handler;
  }

  public void start(String name) {
    entityStarted = false;
    handler.startSheet(name, importer.startCollection(name));
  }

  public void startRow(int rowNum) {
    handler.startRow();
  }

  public void cell(short column, String value, StylesMapper.StyleTypes style) {
    switch (style) {
      case PROPERTY_TYPE:
        handler.handle(column, value, importer.registerPropertyType(column, value));
        break;
      case IDENTITY_COLUMN:
        handler.handle(column, value, importer.registerPropertyName(column, value).and(
          importer.registerUnique(column, true)
        ));
        break;
      case PROPERTY_NAME:
        handler.handle(column, value, importer.registerPropertyName(column, value));
        break;
      case VALUE:
        if (!entityStarted) {
          entityStarted  = true;
          handler.startValuePart();
          importer.startEntity();
        }
        handler.handle(column, value, importer.setValue(column, value));
        break;
      case PROPERTY_METADATA:
        handler.handle(column, value, importer.registerMetadata(column, value));
        break;
      case NONE:
        break;
      default:
        throw new RuntimeException("Not all enum cases have been handled");
    }
  }

  public void endRow(int rowNum) {
    if (entityStarted) {
      entityStarted = false;
      handler.endRow(importer.finishEntity());
    } else {
      handler.endRow(new HashMap<>());
    }
  }

  public void finish() {
    handler.endSheet(importer.finishCollection());
  }

}
