package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet.EntitySheet;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getCollectionByVreId;

public class ExcelExportService {

  private final Vres mappings;
  private final GraphWrapper graphWrapper;


  public ExcelExportService(Vres vres, GraphWrapper graphWrapper) {
    this.mappings = vres;
    this.graphWrapper = graphWrapper;
  }


  /**
   * Exports a list of vertices as excel workbook
   * @param vertices the vertices to export
   * @return the export as workbook
   */
  public SXSSFWorkbook searchResultToExcel(List<Vertex> vertices, String rootType, int depth,
                                           List<String> relationNames) throws IOException {

    SXSSFWorkbook workbook = new SXSSFWorkbook();

    Collection rootCollection = mappings.getCollectionForType(rootType).get();
    String detectedVre = rootCollection.getVre().getVreName();

    Map<Collection, Set<Vertex>> verticesPerType = Maps.newHashMap();
    verticesPerType.put(rootCollection, Sets.newHashSet(vertices));

    GraphTraversal<Vertex, Vertex> current = graphWrapper.getGraph().traversal().V(vertices);
    for (int i = 0; i < depth - 1; i++) {

      current = relationNames == null ?
        current.bothE().otherV() :
        current.bothE(relationNames.toArray(new String[relationNames.size()])).otherV();

      current.asAdmin().clone().forEachRemaining(entity -> {

        final Optional<Collection> collection = getCollectionByVreId(entity, mappings, detectedVre);

        if (collection.isPresent()) {
          Set<Vertex> vertexSet;
          if (verticesPerType.containsKey(collection.get())) {
            vertexSet = verticesPerType.get(collection.get());
          } else {
            vertexSet = Sets.newHashSet();
            verticesPerType.put(collection.get(), vertexSet);
          }
          vertexSet.add(entity);
        }
      });
    }


    for (Map.Entry<Collection, Set<Vertex>> entry : verticesPerType.entrySet()) {
      new EntitySheet(entry.getKey(), workbook, graphWrapper, mappings).renderToSheet(entry.getValue());
    }
    // TODO: relate sheets through edges.

    return workbook;
  }
}
