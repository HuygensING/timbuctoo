package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

public class PropertyColumns {
  public final List<LinkedHashMap<String, Object>> cellsPerRow = new ArrayList<>();
  private final SortedSet<String> initialSubColumns;
  private final String[] allowedValues;
  private final LocalProperty property;
  private List<Vertex> vertices;

  public PropertyColumns(LocalProperty property) {
    this.property = property;
    if (property.getParts().isPresent()) {
      this.initialSubColumns = Sets.newTreeSet(property.getParts().get());
    } else {
      this.initialSubColumns = Sets.newTreeSet();
    }
    if (property.getOptions().isPresent()) {
      Collection<String> allowedValues = property.getOptions().get();
      this.allowedValues = allowedValues.toArray(new String[allowedValues.size()]);
    } else {
      this.allowedValues = null;
    }

  }

  public List<String> getColumns() {
    return Lists.newArrayList(initialSubColumns);
  }

  public void addData(GraphTraversal<Vertex, Vertex> vertices) {
  }

  //public void addData(ExcelSheetRange e) {
  //
  //}

  public List<Optional<XSSFDataValidationConstraint>> getValidValuesPerColumn(XSSFDataValidationHelper dvHelper) {
    if (allowedValues == null) {
      return Lists.newArrayList(Optional.empty());
    } else {
      XSSFDataValidationConstraint constraint = (XSSFDataValidationConstraint) dvHelper
        .createExplicitListConstraint(allowedValues);
      return  Lists.newArrayList(Optional.of(constraint));
    }
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
}
