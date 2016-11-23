package nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.styleawarexlsxloader;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.RowCellHandler;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StyleAwareRowCellHandler implements RowCellHandler {
  private final Importer importer;
  private boolean entityStarted;
  private StylesMapper stylesMapper;
  private static final Logger LOG = getLogger(StyleAwareRowCellHandler.class);

  public StyleAwareRowCellHandler(Importer importer, StylesMapper stylesMapper) {
    this.stylesMapper = stylesMapper;
    this.importer = importer;
  }

  @Override public void start(String name) {
    entityStarted = false;
    importer.startCollection(name);
  }

  @Override public void startRow(int rowNum) {
  }

  @Override public void cell(short column, String value, String cellStyleStr) {
    StylesMapper.StyleTypes style = stylesMapper.getStyleFor(cellStyleStr);

    switch (style) {
      case PROPERTY_NAME:
        importer.registerPropertyName(column, value);
        break;
      case VALUE:
        if (!entityStarted) {
          entityStarted  = true;
          importer.startEntity();
        }
        importer.setValue(column, value);
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
      importer.finishEntity();
    } else {
      LOG.error("An entity was finished before it was started");
    }
  }

  @Override public void finish() {
    importer.finishCollection();
  }

}
