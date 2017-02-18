package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
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
  private final Map<String, CompiledExpression> expressions;
  private final VariableGetter variableGetter;

  private final JoinHandler joinHandler = new HashMapBasedJoinHandler();

  public static final String HAS_NEXT_ERROR = "hasNextError";

  public BulkUploadedDataSource(String vreName, String collectionName, Map<String, String> customFields,
                                GraphWrapper graphWrapper) {
    this.vreName = vreName;
    this.collectionName = collectionName;
    this.graphWrapper = graphWrapper;
    this.errorHandler = new TimbuctooErrorHandler(graphWrapper, vreName, collectionName);
    this.expressions = new HashMap<>();
    DVMap resolver = new Resolver();
    Class[] dynamicLibs = new Class[] { VariableGetter.class };
    Library lib = new Library(null, dynamicLibs, new Class[0], resolver, null);
    variableGetter = new VariableGetter();
    customFields.forEach((key, value) -> {
      try {
        expressions.put(key, Evaluator.compile(value, lib));
      } catch (CompilationException ce) {
        LOG.error("Could not compile expression", ce);
      }
    });
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

                         return (Row) new BulkUploadedRow(valueMap, variableGetter, errorHandler);
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
    private final GraphWrapper graphWrapper;
    private final String vreName;
    private final String collectionName;
    private Vertex currentVertex;
    private Vertex lastError;


    public TimbuctooErrorHandler(GraphWrapper graphWrapper, String vreName, String collectionName) {
      this.graphWrapper = graphWrapper;
      this.vreName = vreName;
      this.collectionName = collectionName;
    }

    @Override
    public void linkError(Map<String, Object> rowData, String childField, String parentCollection,
                          String parentField) {

      Object fieldValue = rowData.get(childField);
      if (fieldValue != null) {
        Graph graph = graphWrapper.getGraph();
        currentVertex.property(ERROR_PREFIX + childField,
          String.format("'%s' does not exist in field '%s' of collection '%s'.",
          fieldValue,
          parentField,
          parentCollection
        ));
        //if the current entity is not already part of the rawEntities-with-errors chain
        if (!currentVertex.edges(Direction.IN, HAS_NEXT_ERROR).hasNext()) {
          //if there is no such chain
          if (lastError == null) {
            //start it
            Vertex collection = graph.traversal().V()
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
    }

    public void setCurrentVertex(Vertex currentVertex) {
      this.currentVertex = currentVertex;
    }
  }

  @Override
  public String toString() {
    return String.format("    BulkUploadedDatasource: %s, %s\n", this.vreName, this.collectionName);
  }

  private class BulkUploadedRow implements Row {
    private final Map<String, Object> data;
    private final VariableGetter variableGetter;
    private final ErrorHandler errorHandler;

    public BulkUploadedRow(Map<String, Object> data, VariableGetter variableGetter, ErrorHandler errorHandler) {
      this.data = data;
      this.variableGetter = variableGetter;
      this.errorHandler = errorHandler;
    }

    @Override
    public Object get(String key) {
      if (data.containsKey(key)) {
        return data.get(key);
      } else if (expressions.containsKey(key)) {
        variableGetter.setData(data);
        try {
          return expressions.get(key).evaluate(new Object[] { variableGetter });
        } catch (Throwable throwable) {
          LOG.error("Exception during evaluation of expression", throwable);
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

  private class Resolver extends DVMap {
    @Override
    public String getTypeName(String varName) {
      return "String";
    }
  }

  public class VariableGetter {
    private Map<String, Object> data = new HashMap<>();

    private VariableGetter() {
    }

    public String getStringProperty(String name) {
      return data.get(name) + "";
    }

    public void setData(Map<String, Object> data) {
      this.data = data;
    }
  }
}
