package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.ResultHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.RowCellHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;

import java.util.HashMap;

public class StyleAwareRowCellHandler implements RowCellHandler {
  private final Importer importer;
  private final ResultHandler handler;
  private boolean entityStarted;
  private StylesMapper stylesMapper;

  public StyleAwareRowCellHandler(Importer importer, ResultHandler handler, StylesMapper stylesMapper) {
    this.stylesMapper = stylesMapper;
    this.importer = importer;
    this.handler = handler;
  }

  @Override public void start(String name) {
    entityStarted = false;
    handler.startSheet(name, importer.startCollection(name));
  }

  @Override public void startRow(int rowNum) {
    handler.startRow();
  }

  @Override public void cell(short column, String value, String cellStyleStr) {
    StylesMapper.StyleTypes style = stylesMapper.getStyleFor(cellStyleStr);

    switch (style) {
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
      case NONE:
        break;
      default:
        throw new RuntimeException("Not all enum cases have been handled");
    }
  }

  @Override public void endRow(int rowNum) {
    if (entityStarted) {
      entityStarted = false;
      handler.endRow(importer.finishEntity());
    } else {
      handler.endRow(new HashMap<>());
    }
  }

  @Override public void finish() {
    importer.finishCollection();
    handler.endSheet();
  }

}
