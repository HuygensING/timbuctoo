package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsedworkbook;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.relationtypes.RelationTypeDescription;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.glassfish.jersey.internal.util.Producer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ParsedCollectionRange {
  private static final String TIMBUCTOO_SUFFIX = "_data";
  public final LinkedHashMap<String, PropertyColumns> properties = new LinkedHashMap<>();
  public final LinkedHashMap<String, RelationColumns> relations = new LinkedHashMap<>();
  private final String name;
  private final RowIterable rowIterable;

  ParsedCollectionRange(String name, RowIterable iterable) {
    this.name = name;
    this.rowIterable = iterable;
  }

  static Optional<ParsedCollectionRange> from(Name range, Workbook wb) {
    final String rangeName = range.getNameName();

    //if the range ends with TIMBUCTOO_SUFFIX we assume its meant for us
    if (rangeName.endsWith(TIMBUCTOO_SUFFIX)) {
      final String nameWithoutSuffix = rangeName.substring(0, rangeName.length() - TIMBUCTOO_SUFFIX.length());


      AreaReference aref;
      try {
        aref = new AreaReference(range.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
      } catch (IllegalArgumentException e) {
        //non-contiguous range
        CellReference firstcellRef = AreaReference.generateContiguous(range.getRefersToFormula())[0].getFirstCell();
        Cell firstCell = wb
          .getSheet(firstcellRef.getSheetName())
          .getRow(firstcellRef.getRow())
          .getCell(firstcellRef.getCol(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        Helpers.addFailure(
          firstCell,
          "Range is non-contiguous (i.e. there are multiple seperate ranges) this is not yet supported."
        );
        return Optional.empty();
      }

      //Loop over all cells. Create a property per column
      //FIXME allow multicolumn properties
      CellReference firstCell = aref.getFirstCell();
      CellReference lastCell = aref.getLastCell();
      int minRow = Math.min(firstCell.getRow(), lastCell.getRow());
      int maxRow = Math.max(firstCell.getRow(), lastCell.getRow());
      int minCol = Math.min(firstCell.getCol(), lastCell.getCol());
      int maxCol = Math.max(firstCell.getCol(), lastCell.getCol());

      Sheet sheet = wb.getSheet(firstCell.getSheetName());
      final RowIterable iterable = new RowIterable(sheet, minRow + 1, maxRow);
      final ParsedCollectionRange parsedCollectionRange = new ParsedCollectionRange(nameWithoutSuffix, iterable);


      if (minRow == -1) {
        minRow = 0;
        maxRow = sheet.getLastRowNum();
      }
      if (minCol == -1) {
        minCol = 0;
        for (int i = minRow; i <= maxRow; i++) {
          maxCol = Math.max(maxCol, sheet.getRow(i).getLastCellNum());
        }
      }

      for (int column = minCol; column <= maxCol; ) {
        int start = column;
        column++;

        ParsedColumns.factory(sheet.getRow(minRow), start)
          .ifPresent(columns -> {
            if (columns instanceof PropertyColumns) {
              PropertyColumns pc = (PropertyColumns) columns;
              if (parsedCollectionRange.properties.containsKey(pc.getName())) {
                pc.markError("This property was already defined earlier");
              } else {
                parsedCollectionRange.properties.put(pc.getName(), pc);
              }
            } else {
              RelationColumns rc = (RelationColumns) columns;
              if (parsedCollectionRange.relations.containsKey(rc.getRelationTypeName())) {
                rc.markError("This relation is already defined earlier");
              } else {
                parsedCollectionRange.relations.put(rc.getRelationTypeName(), rc);
              }
            }
          });
      }
      return Optional.of(parsedCollectionRange);
    } else {
      return Optional.empty();
    }
  }

  public void asWorkSheet(XSSFSheet sheet, CreationHelper createHelper) {
    XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);

    sheet.createRow(0); //for the names
    sheet.createRow(1); //for the subFields
    int curCol = 0;
    for (Map.Entry<String, PropertyColumns> property : this.properties.entrySet()) {
      PropertyColumns prop = property.getValue();
      int addedCols = prop.addHeader(sheet, curCol, property.getKey(), createHelper);
      int propCol = curCol;
      for (Optional<XSSFDataValidationConstraint> optionalConstraint : prop.getValidValuesPerColumn(dvHelper)) {
        if (optionalConstraint.isPresent()) {
          XSSFDataValidationConstraint dvConstraint = optionalConstraint.get();
          CellRangeAddressList addressList = new CellRangeAddressList(2, 10000, propCol, propCol);
          XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
          validation.setShowErrorBox(true);
          sheet.addValidationData(validation);
        }
        propCol++;
      }
      curCol += addedCols;
    }

    //autoSizeColumn
  }

  public void writeRelations(SavingState state, Collection collection, EdgeProducer edgeProducer) {
    for (Map.Entry<Integer, Vertex> entry : state.getVerticeList(collection).entrySet()) {
      int index = entry.getKey();
      Vertex rowVertex = entry.getValue();
      Optional<Row> row = rowIterable.get(index);
      if (row.isPresent()) {
        for (RelationColumns relationColumn: relations.values()) {
          relationColumn.applyData(rowVertex, row.get(), state::getVertexById, edgeProducer);
        }
      } else {
        //FIXME log invariant failure
      }
    }
  }

  public void writeProperties(SavingState state, Collection collection, Producer<Vertex> vertexFactory) {
    RowIterable.RowIterator iterator = rowIterable.iterator();
    Map<String, LocalProperty> propertyDescriptors = collection.getWriteableProperties();
    Vertex vertex = null;
    while (iterator.hasNext()) {
      final Row row = iterator.next();
      boolean hasData = false;
      if (vertex == null) {
        vertex = vertexFactory.call();
      }
      for (PropertyColumns propColumn : properties.values()) {
        final LocalProperty propDescriptor = propertyDescriptors.get(propColumn.getName());
        if (propDescriptor != null) {
          final Optional<String> data = propColumn.applyData(vertex, propDescriptor, row);
          if (data.isPresent()) {
            hasData = true;
            if (propColumn.isIdentityColumn()) {
              state.addIndexedVertex(collection, data.get(), vertex);
            }
          }
        }
      }
      if (hasData) {
        state.newVertex(collection, vertex, iterator.getCur());
        vertex = null;
      }
    }
  }

  public String getName() {
    return name;
  }

  public boolean isValid(Vre vre, LinkedHashMap<String, ParsedCollectionRange> worksheets,
                         Map<String, RelationTypeDescription> relationDescriptions) {
    boolean valid = true;
    final Optional<Collection> optCollection = vre.getCollectionForCollectionName(name);
    final Set<String> collectionsWithIdentityColumn = Sets.newHashSet();
    if (optCollection.isPresent()) {
      final Collection collection = optCollection.get();
      for (PropertyColumns propertyColumns : this.properties.values()) {
        if (propertyColumns.isValid(collection, rowIterable.iterator())) {
          if (propertyColumns.isIdentityColumn()) {
            if (collectionsWithIdentityColumn.contains(propertyColumns.getName())) {
              propertyColumns.markError("More then one column marked as the identity.");
              valid = false;
            }
            collectionsWithIdentityColumn.add(propertyColumns.getName());
          }
        } else {
          valid = false;
        }
      }

      for (RelationColumns relationColumns: this.relations.values()) {
        if (relationColumns.isValid(vre, collection, relationDescriptions)) {
          final String targetType = relationColumns.getTargetType();
          if (worksheets.containsKey(targetType)) {
            if (collectionsWithIdentityColumn.contains(targetType)) {
              relationColumns.markError("Target collection " + name + " has no identity column (a column marked with " +
                "a '*' at the end that contains only unique values, whose values are used here to refer to)");
              valid = false;
            } else {
              //yeey, it is valid!
            }
          } else {
            relationColumns.markError("Target collection " + targetType + " is not present in the excel sheet");
            valid = false;
          }
        } else {
          valid = false;
        }
      }
      return valid;
    } else {
      return true; //ignore
    }
  }

  public interface EdgeProducer {
    boolean call(Vertex from, Vertex to, String label);
  }

  public static class RowIterable implements Iterable<Row> {

    private final Sheet sheet;
    private final int start;
    private final int end;

    public RowIterable(Sheet sheet, int start, int end) {
      this.sheet = sheet;
      this.start = start;
      this.end = end;
    }

    public Optional<Row> get(int index) {
      return Optional.ofNullable(sheet.getRow(index));
    }

    @Override
    public RowIterator iterator() {
      return new RowIterator();
    }

    public class RowIterator implements Iterator<Row> {
      private int cur;

      public RowIterator() {
        cur = start;
      }

      public int getCur() {
        return cur;
      }

      @Override
      public boolean hasNext() {
        return cur <= end;
      }

      @Override
      public Row next() {
        Row result = null;
        while (result == null) {
          result = sheet.getRow(cur);
          cur += 1;
        }

        return result;
      }
    }
  }
}
