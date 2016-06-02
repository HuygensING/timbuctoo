package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

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
  public SXSSFWorkbook searchResultToExcel(List<Vertex> vertices, String rootType, int depth) {
    SXSSFWorkbook workbook = new SXSSFWorkbook();

    Collection rootCollection = mappings.getCollectionForType(rootType).get();
    String detectedVre = rootCollection.getVre().getVreName();

    Map<Collection, Set<Vertex>> verticesPerType = Maps.newHashMap();
    verticesPerType.put(rootCollection, Sets.newHashSet(vertices));

    GraphTraversal<Vertex, Vertex> current = graphWrapper.getGraph().traversal().V(vertices);
    for (int i = 0; i < depth - 1; i++) {

      List<Vertex> currentList = current.bothE().otherV().asAdmin().clone().toList();
      current = current.bothE().otherV();
      currentList.forEach(entity -> {
        final List<Collection> filteredTypes = Arrays.asList(getEntityTypesOrDefault(entity))
          .stream()
          .map(type -> mappings.getCollectionForType(type).get())
          .filter(collection -> collection.getVre().getVreName().equals(detectedVre))
          .collect(toList());

        final Collection collection = filteredTypes.size() > 0 ? filteredTypes.get(0) : null;

        if (collection != null) {
          Set<Vertex> vertexList;
          if (verticesPerType.containsKey(collection)) {
            vertexList = verticesPerType.get(collection);
          } else {
            vertexList = Sets.newHashSet();
            verticesPerType.put(collection, vertexList);
          }
          vertexList.add(entity);
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
