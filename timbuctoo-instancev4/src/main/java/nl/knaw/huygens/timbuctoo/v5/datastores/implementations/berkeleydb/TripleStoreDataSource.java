package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.RowFactory;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
import static org.slf4j.LoggerFactory.getLogger;

public class TripleStoreDataSource implements DataSource {
  private final RowFactory rowFactory;
  private final TripleStore tripleStore;
  private final CollectionIndex collectionIndex;
  private final String collectionUri;
  private final JoinHandler joinHandler;
  private String stringRepresentation;
  private static final Logger LOG = getLogger(TripleStoreDataSource.class);

  public TripleStoreDataSource(TripleStore tripleStore, CollectionIndex collectionIndex, String dataSetId,
                               String collectionUri, RowFactory rowFactory) {
    this.tripleStore = tripleStore;
    this.collectionIndex = collectionIndex;
    this.collectionUri = collectionUri;
    StringBuffer result = new StringBuffer("    BdbBulkuploadDataSource: ");
    result
      .append(dataSetId).append(", ")
      .append(collectionUri).append("\n");
    this.rowFactory = rowFactory;
    this.joinHandler = this.rowFactory.getJoinHandler();
    stringRepresentation = result.toString();
  }

  @Override
  public Stream<Row> getRows(ErrorHandler defaultErrorHandler) {
    //First, get the properties for this collection
    //i.e. get the collection

    try (Stream<Quad> propsSubjects = tripleStore.getQuads(collectionUri, RdfConstants.OF_COLLECTION + "_inverse")) {
      Map<String, String> props = propsSubjects
        .map(propsOfCollection -> {
          String propSubject = propsOfCollection.getObject();
          return tuple(propSubject, tripleStore.getFirst(propSubject, RdfConstants.RDFS_LABEL));
        })
        .filter(data -> data.getRight().isPresent())
        .map(data -> tuple(data.getLeft(), data.getRight().get().getObject()))
        .collect(toMap(Tuple::getLeft, Tuple::getRight));
      try (Stream<String> subjects = collectionIndex.getSubjects(collectionUri)) {
        return subjects.map(subject -> {
          try (Stream<Quad> quads = tripleStore.getQuads(subject)) {
            Map<String, String> result = new HashMap<>();
            quads.forEach(quad -> {
              String propName = props.get(quad.getPredicate());
              if (propName == null) {
                LOG.error("Stored raw property has no name: " + subject);
              } else {
                result.put(propName, quad.getObject());
              }
            });
            return rowFactory.makeRow(result, defaultErrorHandler);
          }
        });
      }
    }

  }

  @Override
  public void willBeJoinedOn(String fieldName, String referenceJoinValue, String uri, String outputFieldName) {
    joinHandler.willBeJoinedOn(fieldName, referenceJoinValue, uri, outputFieldName);
  }

  @Override
  public String toString() {
    return stringRepresentation;
  }

}
