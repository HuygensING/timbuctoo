package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader;

import com.google.common.collect.ImmutableMap;
import org.apache.poi.xssf.model.StylesTable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class StylesMapper {

  public enum StyleTypes {
    VALUE,
    PROPERTY_NAME,
    NONE
  }

  private Map<String, StyleTypes> nameToStyleType = ImmutableMap.of(
    "timbuctoo_value", StyleTypes.VALUE,
    "timbuctoo_propertyname", StyleTypes.PROPERTY_NAME
  );
  private Map<String, StyleTypes> idToStyleType = new HashMap<>();

  public StylesMapper(StylesTable styles) {
    NodeList cellXfs = null;
    NodeList cellStyles = null;
    final NodeList styleNodes = styles.getCTStylesheet().getDomNode().getChildNodes();
    for (int i = 0; i < styleNodes.getLength(); i++) {
      final Node item = styleNodes.item(i);
      if ("cellXfs".equals(item.getLocalName())) {
        cellXfs = item.getChildNodes();
      } else if ("cellStyles".equals(item.getLocalName())) {
        cellStyles = item.getChildNodes();
      }
      if (cellStyles != null && cellXfs != null) {
        break;
      }
    }
    if (cellXfs == null || cellStyles == null) {
      //log exception
      return;
    }
    for (int i = 0; i < cellStyles.getLength(); i++) {
      String cellStyleName = cellStyles.item(i).getAttributes().getNamedItem("name").getNodeValue();
      String cellStyleXfId = cellStyles.item(i).getAttributes().getNamedItem("xfId").getNodeValue();
      final StyleTypes styleType = nameToStyleType.get(cellStyleName);
      if (styleType != null && styleType != StyleTypes.NONE) {
        for (int j = 0; j < styles.getNumCellStyles(); j++) {
          if (cellXfs.item(j).getAttributes().getNamedItem("xfId").getNodeValue().equals(cellStyleXfId)) {
            idToStyleType.put(j + "", styleType);
          }
        }
      }
    }
  }

  public StyleTypes getStyleFor(String cellStyleStr) {
    if (cellStyleStr == null) {
      return StyleTypes.NONE;
    }
    StyleTypes result = idToStyleType.get(cellStyleStr);
    if (result == null) {
      return StyleTypes.NONE;
    } else {
      return result;
    }
  }
}
