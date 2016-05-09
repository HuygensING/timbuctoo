package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class ParsedWorkbook {

  public LinkedHashMap<String, CollectionRange> worksheets = new LinkedHashMap<>();

  public static ParsedWorkbook from(Workbook wb) {

    ParsedWorkbook result = new ParsedWorkbook();

    //loop over all ranges
    final int numberOfNames = wb.getNumberOfNames();
    for (int i = 0; i < numberOfNames; i++) {
      Name range = wb.getNameAt(i);
      CollectionRange.from(range, wb)
        .ifPresent(sheet -> result.worksheets.put(sheet.getName(), sheet));
    }
    return result;
  }

  public Workbook asWorkBook() {
    XSSFWorkbook wb = new XSSFWorkbook();
    CreationHelper createHelper = wb.getCreationHelper();
    worksheets.forEach((name, parsedSheet) -> {
      parsedSheet.asWorkSheet(wb.createSheet(WorkbookUtil.createSafeSheetName(name)), createHelper);
    });

    return wb;
  }

  private Vertex makeVertex(SaveingState state, Collection collection, GraphWrapper graphwrapper) {
    state.allowCommit();
    final String typeName = collection.getEntityTypeName();
    //FIXME re-use code from crudservice create
    final Vertex vertex = graphwrapper.getGraph().addVertex(
      "rev", 1,
      "tim_id", UUID.randomUUID().toString(),
      "types", jsnA(jsn(typeName)).toString()
    );
    ((Neo4jVertex) vertex).addLabel(typeName);

    return vertex;
  }

  public boolean saveToDb(GraphWrapper graphwrapper, Vre vre, Map<String, RelationDescription> descriptions) {
    boolean success = true;
    for (CollectionRange collectionRange : worksheets.values()) {
      if (!collectionRange.isValid(vre, worksheets, descriptions)) {
        success = false;
      }
    }

    try (SaveingState state = new SaveingState(graphwrapper)) {
      for (CollectionRange sheet : worksheets.values()) {
        final Optional<Collection> collectionOpt = vre.getCollectionForCollectionName(sheet.getName());
        if (collectionOpt.isPresent()) {
          final Collection collection = collectionOpt.get();
          sheet.writeProperties(state, collection, () -> makeVertex(state, collection, graphwrapper));
        }
      }
      //Second iteration this time for the relations
      for (CollectionRange sheet : worksheets.values()) {
        final Optional<Collection> collectionOpt = vre.getCollectionForCollectionName(sheet.getName());
        if (collectionOpt.isPresent()) {
          final Collection collection = collectionOpt.get();
          sheet.writeRelations(
            state,
            collection,
            (from, to, label) -> makeEdge(state, from, to, label, collection, descriptions)
          );
        }
      }
    }
    return success;
  }

  private boolean makeEdge(SaveingState state, Vertex from, Vertex to, String label, Collection collection,
                        Map<String, RelationDescription> descriptions) {
    state.allowCommit();
    final RelationDescription description = descriptions.get(label);
    if (description != null) {
      if (Objects.equals(label, description.getRegularName())) {
        final String entityTypeName = collection.getEntityTypeName();
        final String abstractName = collection.getAbstractType();
        //FIXME reuse code from jsonCrudservice
        from.addEdge(description.getRegularName(), to,
          // FIXME: string concatenating methods like this should be delegated to a configuration class
          entityTypeName + "_accepted", true,
          "types", jsnA(jsn(entityTypeName), jsn(abstractName)).toString(),
          "typeId", description.getId(),
          "tim_id", UUID.randomUUID().toString(),
          "isLatest", true
        );
      } else {
        to.addEdge(description.getRegularName(), from);
      }
      return true;
    }
    return false;
  }

}
