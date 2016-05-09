package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Name;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


//FIXME: rename to collectionrange
public class CollectionRange {
  private static final String TIMBUCTOO_SUFFIX = "_data";
  public final LinkedHashMap<String, PropertyColumns> properties = new LinkedHashMap<>();
  public final LinkedHashMap<String, RelationColumns> relations = new LinkedHashMap<>();
  private final String name;

  CollectionRange(String name) {
    this.name = name;
  }

  static Optional<CollectionRange> from(Name range, Workbook wb) {
    final String rangeName = range.getNameName();

    //if the range ends with TIMBUCTOO_SUFFIX we assume its meant for us
    if (rangeName.endsWith(TIMBUCTOO_SUFFIX)) {
      final String nameWithoutSuffix = rangeName.substring(0, rangeName.length() - TIMBUCTOO_SUFFIX.length());
      final CollectionRange collectionRange = new CollectionRange(nameWithoutSuffix);


      AreaReference aref;
      try {
        aref = new AreaReference(range.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
      } catch (IllegalArgumentException e) {
        //non-contiguous range
        AreaReference[] arefs = AreaReference.generateContiguous(range.getRefersToFormula());
        Helpers.addFailure(
          arefs[0].getFirstCell(),
          wb,
          "Range is non-contiguous (i.e. there are multiple seperate ranges) this is not yet supported."
        );
        return Optional.empty();
      }

      //Loop over all cells. Create a property per column
      //FIXME allow multicolumn properties
      //FIXME allow full column ranges
      CellReference firstCell = aref.getFirstCell();
      CellReference lastCell = aref.getLastCell();
      int minRow = Math.min(firstCell.getRow(), lastCell.getRow());
      int maxRow = Math.max(firstCell.getRow(), lastCell.getRow());
      int minCol = Math.min(firstCell.getCol(), lastCell.getCol());
      int maxCol = Math.max(firstCell.getCol(), lastCell.getCol());

      Sheet sheet = wb.getSheet(firstCell.getSheetName());

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

      for (int column = minCol; column <= maxCol; column++) {
        Columns.factory(minRow, minRow + 1, maxRow, sheet, column).ifPresent(columns -> {
          if (columns instanceof PropertyColumns) {
            PropertyColumns pc = (PropertyColumns) columns;
            if (collectionRange.properties.containsKey(pc.getName())) {
              pc.markError("This property was already defined earlier");
            } else {
              collectionRange.properties.put(pc.getName(), pc);
            }
          } else {
            RelationColumns rc = (RelationColumns) columns;
            if (collectionRange.relations.containsKey(rc.getName())) {
              rc.markError("This relation is already defined earlier");
            } else {
              collectionRange.relations.put(rc.getName(), rc);
            }
          }
        });
      }
      return Optional.of(collectionRange);
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

  public void writeRelations(SaveingState state, Collection collection, EdgeProducer edgeProducer) {
    for (Map.Entry<Integer, Vertex> entry : state.getVerticeList(collection).entrySet()) {
      int index = entry.getKey();
      Vertex rowVertex = entry.getValue();
      for (RelationColumns relationColumn: relations.values()) {
        relationColumn.applyData(rowVertex, index, state::getVertexById, edgeProducer);
      }
    }
  }

  public void writeProperties(SaveingState state, Collection collection, Producer<Vertex> vertexFactory) {
    Optional<PropertyColumns> property = properties.values().stream().findAny();
    if (property.isPresent()) {
      final int size = property.get().getSize();
      Map<String, LocalProperty> propertyDescriptors = collection.getWriteableProperties();
      for (int row = 0; row < size; row++) {
        boolean hasData = false;
        for (PropertyColumns propColumn : properties.values()) {
          if (propColumn.hasData(row)) {
            hasData = true;
          }
        }
        if (hasData) {
          Vertex vertex = vertexFactory.call();
          state.newVertex(collection, vertex, row);
          for (PropertyColumns propColumn : properties.values()) {
            final LocalProperty propDescriptor = propertyDescriptors.get(propColumn.getName());
            if (propDescriptor != null) {
              final Optional<String> data = propColumn.applyData(vertex, propDescriptor, row);
              if (propColumn.isUnique()) {
                data.ifPresent(val -> state.addIndexedVertex(collection, val, vertex));
              }
            }
          }
        }
      }
    }
  }

  public String getName() {
    return name;
  }

  public boolean isValid(Vre vre, LinkedHashMap<String, CollectionRange> worksheets,
                         Map<String, RelationDescription> relationDescriptions) {
    boolean valid = true;
    final Optional<Collection> optCollection = vre.getCollectionForCollectionName(name);
    final Set<String> collectionsWithIdentityColumn = Sets.newHashSet();
    if (optCollection.isPresent()) {
      final Collection collection = optCollection.get();
      for (PropertyColumns propertyColumns : this.properties.values()) {
        if (propertyColumns.isValid(collection)) {
          if (propertyColumns.isUnique()) {
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
}
