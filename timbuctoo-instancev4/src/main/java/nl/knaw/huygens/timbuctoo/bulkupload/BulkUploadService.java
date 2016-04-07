package nl.knaw.huygens.timbuctoo.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook.CollectionSheet;
import nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook.ParsedWorkbook;
import nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook.PropertyColumns;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BulkUploadService {

  private final Vre vre;
  private final GraphWrapper graphwrapper;

  public BulkUploadService(Vre vre, GraphWrapper graphwrapper) {
    this.vre = vre;
    this.graphwrapper = graphwrapper;
  }

  public Optional<Workbook> saveToDb(Workbook wb) {
    ValidationResult validationResult = saveToDb(ParsedWorkbook.from(wb));
    if (!validationResult.isValid()) {
      setValidationMessages(validationResult, wb);
      return Optional.of(wb);
    } else {
      return Optional.empty();
    }

  }

  //package local, for testing
  ValidationResult saveToDb(ParsedWorkbook wb) {
    ValidationResult validationResult = validate(wb);

    if (validationResult.isValid()) {
      //saveToDb();
    }
    return validationResult;
  }


  public Workbook getEmptyTemplate(String... propsToLeaveOut) {
    ParsedWorkbook workbook = new ParsedWorkbook();
    //each property can generate the two rows needed for the excel
    //furthermore all registered relations are generated
    vre.getCollections().forEach((collName, coll) -> {
      if (coll.isRelationCollection()) {
        return;
      }
      CollectionSheet sheet = workbook.withSheet(collName);
      List<Vertex> vertices = graphwrapper.getCurrentEntitiesFor(coll.getEntityTypeName()).toList();
      GraphTraversal<Vertex, Vertex> collectionTraversal = null;
      if (vertices.size() > 0) {
        collectionTraversal = graphwrapper.getGraph().traversal().V(vertices);
      }

      for (Map.Entry<String, LocalProperty> entry : coll.getWriteableProperties().entrySet()) {
        LocalProperty prop = entry.getValue();
        PropertyColumns propertyColumns = sheet.withProperty(entry.getKey(), prop);
        if (collectionTraversal != null) {
          propertyColumns.addData(collectionTraversal);
        }
      }
    });
    return workbook.asWorkBook();
  }

  private ValidationResult validate(ParsedWorkbook wb) {
    //A workbook is valid if all sheets have the name of a collection and all sheets:
    // - have only columns that are present in the collection description as writeable properties
    // - have only data that can be converted
    // - first row contains (a multi)cell(s) with the property name
    // - second row contains subproperty names if needed (for a personName for example)
    // - the identity column contains only unique values

    //Next to the data columns a column is allowed that contains a sheet-local unique value for each row
    //A series of columns may be labeled "relation" and contain a value of the identity column of a different sheet
    return new ValidationResult();
  }

  private void setValidationMessages(ValidationResult validationResult, Workbook wb) {
  }

}
