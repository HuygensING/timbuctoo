package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrRefObjectMap;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class RrTriplesMap {
  private final List<Tuple<RrRefObjectMap, String>> subscriptions = new ArrayList<>();
  private final RrSubjectMap subjectMap;
  private final List<RrPredicateObjectMap> predicateObjectMaps;
  private final DataSource dataSource;
  private final String uri;

  public RrTriplesMap(RrSubjectMap subjectMap, DataSource dataSource, String uri) {
    this.subjectMap = subjectMap;
    this.predicateObjectMaps = new ArrayList<>();
    this.dataSource = dataSource;
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public void addPredicateObjectMap(RrPredicateObjectMap objectMap) {
    predicateObjectMaps.add(objectMap);
  }

  public void subscribeToSubjectsWith(RrRefObjectMap subscriber, String fieldName) {
    subscriptions.add(Tuple.tuple(subscriber, fieldName));
  }

  Stream<Triple> getItems(ErrorHandler defaultErrorHandler) {
    return stream(dataSource.getRows(defaultErrorHandler))
      .flatMap(row -> {
        Node subject = subjectMap.generateValue(row);

        for (Tuple<RrRefObjectMap, String> subscription : subscriptions) {
          subscription.getLeft().onNewSubject(row.get(subscription.getRight()), subject);
        }

        return predicateObjectMaps.stream()
          .flatMap(predicateObjectMap -> predicateObjectMap.generateValue(subject, row));
      });
  }

  public DataSource getDataSource() {
    return dataSource;
  }
}
