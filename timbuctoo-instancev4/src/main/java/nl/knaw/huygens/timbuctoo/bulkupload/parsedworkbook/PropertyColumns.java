package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook.Helpers.getValueAsString;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class PropertyColumns extends Columns {
  public final List<LinkedHashMap<String, Object>> cellsPerRow = new ArrayList<>();
  private final Cell captionCell;
  private List<Cell> items = new ArrayList<>();
  private String name;
  private boolean unique;

  public PropertyColumns(int minRow, int maxRow, int headerRow, Sheet sheet, String caption, int column) {
    captionCell = sheet.getRow(headerRow).getCell(column);
    if (caption.endsWith("*")) {
      name = caption.substring(0, caption.length() - 1);
      unique = true;//fixme rename unique to identity
    } else {
      name = caption;
    }
    for (int r = minRow; r <= maxRow; r++) {
      Cell propVal = sheet.getRow(r).getCell(column);
      items.add(propVal);
    }
  }

  public List<String> getColumns() {
    return Lists.newArrayList();
  }

  public void addData(GraphTraversal<Vertex, Vertex> vertices, LocalProperty property) {
    //vertices.union(property.traversal()).forEachRemaining(x -> {
    //  x.get()
    //});
  }

  public int getSize() {
    return items.size();
  }


  public List<Optional<XSSFDataValidationConstraint>> getValidValuesPerColumn(XSSFDataValidationHelper dvHelper) {
    return Lists.newArrayList(Optional.empty());
  }

  public int addHeader(XSSFSheet sheet, int start, String caption, CreationHelper createHelper) {
    Row propNames = sheet.getRow(0);
    Row propSubFields = sheet.getRow(1);
    int offset = 0;

    propNames.createCell(start).setCellValue(createHelper.createRichTextString(caption));
    for (String fieldCaption : getColumns()) {
      propSubFields.createCell(start + offset++).setCellValue(createHelper.createRichTextString(fieldCaption));
    }
    if (offset > 1) {
      sheet.addMergedRegion(new CellRangeAddress(
        //rows
        0, 0,
        //columns
        start, start + offset - 1
      ));
    } else {
      offset = 1; //needed if offset = 0
    }
    return offset;
  }

  public Optional<String> applyData(Vertex vertex, LocalProperty property, int index) {
    final Cell data = this.items.get(index);
    try {
      final Optional<String> cellValue = getValueAsString(data);
      if (cellValue.isPresent()) {
        property.setJson(vertex, jsn(cellValue.get()));
        Helpers.addSuccess(data);
        return Optional.of(cellValue.get());
      }
    } catch (IOException e) {
      Helpers.addFailure(data, e.getMessage());
    }
    return Optional.empty();
  }

  public boolean hasData(int index) {
    final Cell data = this.items.get(index);
    return Helpers.hasData(data);
  }

  public String getName() {
    return name;
  }

  public boolean isUnique() {
    return unique;
  }

  public void markError(String msg) {
    Helpers.addFailure(captionCell, msg);
  }

  public boolean isValid(Collection collection) {
    if (collection.getWriteableProperties().containsKey(name)) {
      if (unique) {
        Set<String> cellValues = Sets.newHashSet();
        for (Cell propVal : items) {
          try {
            final Optional<String> valueAsString = getValueAsString(propVal);
            if (valueAsString.isPresent()) {
              String val = valueAsString.get();
              if (cellValues.contains(val)) {
                Helpers.addFailure(propVal, String.format("%s appears earlier as well, but this column is marked as " +
                  "an identity column ('%s' has a * at the end). If you make a relation using '%s' I won't know " +
                  "which row you mean. You can \n\n  (a) make this value differ from the other value\n  (b) remove " +
                  "the * that marks this row as an identity column.\n\nIf you pick (b) you can no longer make " +
                  "relations to this collection. You can, however, add a new column that contains " +
                  "guaranteed-to-be-unique-values. (such as a list of increasing numbers) and add the star to that " +
                  "column. You can then refer to those numbers in the relations.\n\nUsually, you'd want to make " +
                  "this value unique.", val, name, val)
                );
              }
            } else {
              //ignore blank cells
            }
          } catch (IOException e) {
            Helpers.addFailure(propVal, "An identity column should not contain cells with errors");
          }
        }
      }
      return true;
    } else {
      markError("This property is not defined in the VRE configuration");
      return false;
    }
  }
}
