package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;


import com.google.common.collect.Sets;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;

public class EntitySheet {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(EntitySheet.class);

  private final SXSSFSheet sheet;
  private final GraphWrapper graphWrapper;
  private final Vres mappings;
  private final String type;
  private final EdgePropertyGetter edgePropertyGetter;

  private Set<String> loadedIds = Sets.newHashSet();

  public EntitySheet(Collection collection, SXSSFWorkbook workbook, GraphWrapper graphWrapper, Vres vres,
                     String[] relationTypes, String relationAcceptedProperty) {

    final Vre vre = collection.getVre();
    this.sheet = workbook.createSheet(collection.getCollectionName());
    this.type = collection.getEntityTypeName();
    this.graphWrapper = graphWrapper;
    this.mappings = vres;

    edgePropertyGetter = new EdgePropertyGetter(vre.getVreName(), relationAcceptedProperty, mappings, relationTypes);
  }

  // Renders the vertices of this collection to the excel sheet
  public void renderToWorkbook(Set<Vertex> vertices) {

    // 3) Prepare the data cell information, initializing the propertyColDescriptions
    Tuple<PropertyColumnMetadata, List<PropertyData>> sheetData =  prepareSheetData(vertices);

    // The column metadata is the left side of the returned Tuple
    PropertyColumnMetadata propertyColumnMetadata = sheetData.getLeft();
    // The list of data cell descriptions is the right side
    List<PropertyData> propertyDataList = sheetData.getRight();

    // 4) Render the column metadata to the sheet as header rows
    propertyColumnMetadata.renderToSheet(sheet);


    // 5) Load the cell data into the sheet
    final AtomicInteger currentRow = new AtomicInteger(3);

    propertyDataList.forEach(propertyData -> {
      final String timId = propertyData.getTimId();
      if (loadedIds.contains(timId)) {
        return;
      }

      int rows = propertyData.renderToSheet(sheet, propertyColumnMetadata, currentRow.get());

      currentRow.getAndAdd(rows);
      loadedIds.add(timId);
    });
  }

  // Returns list of tuples of <tim_id, <propertyName, cell value description>>
  private Tuple<PropertyColumnMetadata, List<PropertyData>> prepareSheetData(Set<Vertex> vertices) {

    // Read the mappings to get the properties to export
    Map<String, LocalProperty> mapping = mappings.getCollectionForType(type).get().getWriteableProperties();

    // Initialize holder class for property column metadata
    PropertyColumnMetadata propertyColumnMetadata = new PropertyColumnMetadata();

    // Load the sheet cell data in List of holder classes for property data
    List<PropertyData> sheetData = graphWrapper.getGraph().traversal().V(vertices).map(entityT -> {

      // Initialize holder class for property data of the current entity
      PropertyData propertyData = new PropertyData(entityT.get().value("tim_id"));

      // Traversals to fill the property (meta)data holder with excel descriptions for the properties of the
      // current entity
      List<GraphTraversal> propertyGetters = mapping
        .entrySet().stream()
        .map(prop ->
          prop.getValue().getExcelDescription()
              .sideEffect(getExcelDataTraverser(propertyColumnMetadata, propertyData, prop))
        ).collect(Collectors.toList());


      // Add edge data getter to the propertyGetters.
      propertyGetters.add(edgePropertyGetter.getEdgeExcelDataTraversal(propertyColumnMetadata, propertyData));

      // Force side effects on this entity traverser to happen
      graphWrapper.getGraph().traversal().V(entityT.get().id())
        .union(propertyGetters.toArray(new GraphTraversal[propertyGetters.size()])).forEachRemaining(x -> {
          // side effects
        });

      // Return the data cell descriptions with their tim_id
      return propertyData;
    }).toList();

    return new Tuple<>(propertyColumnMetadata, sheetData);
  }

  // Excel processor for property data and metadata
  private Consumer<Traverser<Try<ExcelDescription>>> getExcelDataTraverser(
    PropertyColumnMetadata propertyColumnMetadata, PropertyData propertyData, Map.Entry<String, LocalProperty> prop) {
    return x -> {
      x.get().onSuccess(excelDescription -> {
        // Add new / update column metadata for this property
        propertyColumnMetadata.addColumnInformation(excelDescription, prop.getKey());

        // Add data for this property
        propertyData.putProperty(prop.getKey(), excelDescription);
      });

      x.get().onFailure(e -> {
        LOG.error("Something went wrong while reading the property '{}' of '{}'", prop.getKey(), type, e);
      });
    };
  }

}
