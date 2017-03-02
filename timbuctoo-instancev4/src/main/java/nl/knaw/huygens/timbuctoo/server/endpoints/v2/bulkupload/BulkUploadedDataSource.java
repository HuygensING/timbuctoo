package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.ERROR_PREFIX;
import static nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver.VALUE_PREFIX;

public class BulkUploadedDataSource implements DataSource {
  public static final Logger LOG = LoggerFactory.getLogger(BulkUploadedDataSource.class);
  private final String vreName;
  private final String collectionName;
  private final GraphWrapper graphWrapper;
  private final TimbuctooErrorHandler errorHandler;
  private final Map<String, JexlExpression> expressions;
  private final String stringRepresentation;

  private final JoinHandler joinHandler = new HashMapBasedJoinHandler();

  public static final String HAS_NEXT_ERROR = "hasNextError";

  public BulkUploadedDataSource(String vreName, String collectionName, Map<String, String> customFields,
                                GraphWrapper graphWrapper) {
    StringBuffer result = new StringBuffer("    BulkUploadedDatasource: ");
    result
      .append(vreName).append(", ")
      .append(collectionName).append("\n");
    this.vreName = vreName;
    this.collectionName = collectionName;
    this.graphWrapper = graphWrapper;
    this.errorHandler = new TimbuctooErrorHandler(graphWrapper, vreName, collectionName);
    this.expressions = new HashMap<>();
    Map<String, Object> ns = Maps.newHashMap();
    ns.put("Json", JsonEncoder.class); // make method Json:stringify available in expressions
    ns.put("Math", Math.class); // make all methods of Math available
    ns.put("Integer", Integer.class); // make method Integer
    JexlEngine jexl = new JexlBuilder().namespaces(ns).create();
    customFields.forEach((key, value) -> {
      try {
        expressions.put(key, jexl.createExpression(value));
        result.append("      ").append(key).append(": ").append(value).append("\n");
      } catch (Exception e) { // Catch the runtime exceptions
        LOG.error("Could not compile expression", e);
      }
    });
    stringRepresentation = result.toString();
  }

  @Override
  public Iterator<Row> getRows(ErrorHandler defaultErrorHandler) {
    return graphWrapper.getGraph().traversal().V()
                       .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                       .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
                       .out(TinkerpopSaver.RAW_COLLECTION_EDGE_NAME)
                       .has(TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName)
                       .out(TinkerpopSaver.RAW_ITEM_EDGE_NAME)
                       .toStream()
                       .map(vertex -> {
                         Map<String, Object> valueMap = new HashMap<>();
                         final Iterator<VertexProperty<Object>> properties = vertex.properties();
                         while (properties.hasNext()) {
                           VertexProperty prop = properties.next();
                           if (prop.key().startsWith(VALUE_PREFIX)) {
                             valueMap.put(prop.key().substring(VALUE_PREFIX.length()), prop.value());
                           }
                           if (prop.key().equals("tim_id")) {
                             valueMap.put(prop.key(), prop.value());
                           }
                         }

                         joinHandler.resolveReferences(valueMap);

                         errorHandler.setCurrentVertex(vertex);

                         return (Row) new BulkUploadedRow(valueMap, errorHandler);
                       })
                       .iterator();
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    if (referenceJoinValue != null) {
      joinHandler.willBeJoinedOn(fieldName, referenceJoinValue, uri, outputFieldName);
    }
  }

  private static class TimbuctooErrorHandler implements ErrorHandler {
    private final String vreName;
    private final String collectionName;
    private final GraphTraversalSource traversal;
    private Vertex currentVertex;
    private Vertex lastError;


    public TimbuctooErrorHandler(GraphWrapper graphWrapper, String vreName, String collectionName) {
      this.traversal = graphWrapper.getGraph().traversal();
      this.vreName = vreName;
      this.collectionName = collectionName;
    }

    @Override
    public void linkError(Map<String, Object> rowData, String childField, String parentCollection,
                          String parentField) {

      Object fieldValue = rowData.get(childField);
      if (fieldValue != null) {
        // TODO mention collection that is used to map
        addError(childField, String.format(
          "[Mapping %s]' %s' does not exist in field '%s' of collection '%s'.",
          collectionName,
          fieldValue,
          parentField,
          parentCollection
        ));
      }
    }

    @Override
    public void valueGenerateFailed(String key, String message) {
      addError(key, message);
    }

    private void addError(String childField, String errorMessage) {
      LOG.info("Field '{}' of '{}' has an error: {}", childField, currentVertex, errorMessage);
      currentVertex.property(ERROR_PREFIX + childField, errorMessage);
      //if the current entity is not already part of the rawEntities-with-errors chain
      if (!currentVertex.edges(Direction.IN, HAS_NEXT_ERROR).hasNext()) {
        //if there is no such chain
        if (lastError == null) {
          //start it
          Vertex collection = traversal.V()
            .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
            .has(Vre.VRE_NAME_PROPERTY_NAME, vreName)
            .out(TinkerpopSaver.RAW_COLLECTION_EDGE_NAME)
            .has(TinkerpopSaver.RAW_COLLECTION_NAME_PROPERTY_NAME, collectionName)
            .next();
          collection.addEdge(HAS_NEXT_ERROR, currentVertex);
        } else {
          //continue it
          lastError.addEdge(HAS_NEXT_ERROR, currentVertex);
        }
        lastError = currentVertex;
      }
    }

    public void setCurrentVertex(Vertex currentVertex) {
      this.currentVertex = currentVertex;
    }
  }

  @Override
  public String toString() {
    return stringRepresentation;
  }

  private class BulkUploadedRow implements Row {
    private final Map<String, Object> data;
    private final ErrorHandler errorHandler;

    public BulkUploadedRow(Map<String, Object> data, ErrorHandler errorHandler) {
      this.data = data;
      this.errorHandler = errorHandler;
    }

    @Override
    public Object get(String key) {
      if (data.containsKey(key)) {
        return data.get(key);
      } else if (expressions.containsKey(key)) {
        try {
          JexlContext jexlContext = new MapContext();
          jexlContext.set("v", data);
          return expressions.get(key).evaluate(jexlContext);
        } catch (Throwable throwable) {
          LOG.info("Error during mapping", throwable);
          errorHandler.valueGenerateFailed(
            data.keySet().iterator().next(),
            String.format("Could not execute expression '%s' for this row.", expressions.get(key))
          );
          return null;
        }
      } else {
        return null;
      }
    }

    @Override
    public void handleLinkError(String childField, String parentCollection, String parentField) {
      errorHandler.linkError(data, childField, parentCollection, parentField);
    }
  }

  public static class JsonEncoder {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String stringify(Object obj) throws JsonProcessingException {
      return objectMapper.writeValueAsString(obj);
    }
  }

}
