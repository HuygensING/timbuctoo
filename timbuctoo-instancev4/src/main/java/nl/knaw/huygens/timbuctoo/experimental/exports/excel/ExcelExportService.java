package nl.knaw.huygens.timbuctoo.experimental.exports.excel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet.EntitySheet;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getCollectionByVreId;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;

public class ExcelExportService {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ExcelExportService.class);

  private final Vres mappings;
  private final GraphWrapper graphWrapper;


  public ExcelExportService(Vres vres, GraphWrapper graphWrapper) {
    this.mappings = vres;
    this.graphWrapper = graphWrapper;
  }


  /**
   * Exports a list of vertices as excel workbook
   * @param vertices list of vertices from the search result
   * @param rootType entity type of the vertices based on the search result
   * @param depth depth of relations to traverse for result
   * @param relationNames names of the relations to filter
   * @return the export as workbook
   */
  public SXSSFWorkbook searchResultToExcel(List<Vertex> vertices, String rootType, int depth,
                                           List<String> relationNames) {

    SXSSFWorkbook workbook = new SXSSFWorkbook();

    // Relation names to filter on
    final String[] relationTypes = relationNames == null ?
      null : relationNames.toArray(new String[relationNames.size()]);

    // Get the collection for this list of vertices (deduced from the search params)
    Collection rootCollection = mappings.getCollectionForType(rootType).get();
    Vre vre = rootCollection.getVre();
    String detectedVre = vre.getVreName();

    // Map to hold all the vertices in which come out of the relation traversals per entity type
    Map<String, Set<Vertex>> verticesPerType = Maps.newHashMap();

    // Load the first set of vertices based on the search result List<Vertex>
    verticesPerType.put(rootCollection.getEntityTypeName(), Sets.newHashSet(vertices));

    // Start new GraphTraversal based on the search results
    GraphTraversal<Vertex, Vertex> current = graphWrapper.getGraph().traversal().V(vertices);

    // Get the relationCollection for the traversal's _accepted prop.
    // FIXME: string concatenating methods like this should be delegated to a configuration class
    Collection relationCollection = vre.getRelationCollection().orElse(null);
    String relationPropTypeName = relationCollection == null ? "accepted" :
      relationCollection.getEntityTypeName() + "_accepted";

    // Until depth is reached traverse edges in both directions
    for (int i = 0; i < depth - 1; i++) {

      // ...optionally filtered by relation type
      current = relationTypes == null ?
        current.bothE().has(relationPropTypeName, true).otherV() :
        current.bothE(relationTypes).has(relationPropTypeName, true).otherV();

      // load all the vertices in verticesPerType map
      current.asAdmin().clone().forEachRemaining(entity -> {

        // Is this entity present in the current VRE ?
        final Optional<Collection> collection = getCollectionByVreId(entity, mappings, detectedVre);

        // If so, add it to the map of entities to render into a sheet
        if (collection.isPresent()) {
          final String typeName = collection.get().getEntityTypeName();
          Set<Vertex> vertexSet = verticesPerType.containsKey(typeName) ?
            verticesPerType.get(typeName) : Sets.newHashSet();

          if (!verticesPerType.containsKey(typeName)) {
            verticesPerType.put(typeName, vertexSet);
          }
          vertexSet.add(entity);
        } else {
          // If not, this is an invariant entity
          LOG.error(databaseInvariant, "Expected entity to be in VRE={} with tim_id={}",
            detectedVre,
            getProp(entity, "tim_id", String.class).orElse("missing tim_id"));
        }
      });
    }

    // Render an excel sheet per entity type into the workbook
    for (Map.Entry<String, Set<Vertex>> entry : verticesPerType.entrySet()) {
      Optional<Collection> collection = mappings.getCollectionForType(entry.getKey());
      new EntitySheet(collection.get(), workbook, graphWrapper, mappings, relationTypes, relationPropTypeName)
        .renderToWorkbook(entry.getValue());
    }

    return workbook;
  }
}
