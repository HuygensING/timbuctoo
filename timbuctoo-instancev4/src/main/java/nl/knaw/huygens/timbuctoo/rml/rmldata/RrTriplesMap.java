package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class RrTriplesMap {
  private final List<Tuple<RrRefObjectMap, String>> subscriptions = new ArrayList<>();
  private RrSubjectMap subjectMap;
  private List<RrPredicateObjectMap> predicateObjectMaps = new ArrayList<>();
  private RrLogicalSource logicalSource;
  private DataSource dataSource;
  private Node_URI uri;

  public RrTriplesMap() {
  }

  public static Builder rrTriplesMap() {
    return new Builder();
  }

  void addPredicateObjectMap(RrPredicateObjectMap map) {
    this.predicateObjectMaps.add(map);
  }

  public Node_URI getUri() {
    return uri;
  }

  public void subscribeToSubjectsWith(RrRefObjectMap subscriber, String parent) {
    subscriptions.add(Tuple.tuple(subscriber, parent));
  }

  //FIXME iwillJoinOnYou should make sure that the tripleMap gets ordered

  public Iterator<Triple> getItems() {
    return stream(dataSource.getItems())
      .flatMap(stringObjectMap -> {
        Node subject = subjectMap.getTermMap().generateValue(stringObjectMap);
        for (Tuple<RrRefObjectMap, String> subscription : subscriptions) {
          subscription.getLeft().newSubject(stringObjectMap.get(subscription.getRight()), subject);
        }

        return predicateObjectMaps.stream()
                                  .map(
                                    predicateObjectMap -> predicateObjectMap.generateValue(subject, stringObjectMap));
      }).iterator();
  }

  public static class Builder {
    private final RrTriplesMap instance;
    private RrLogicalSource.Builder logicalSourceBuilder;
    private RrSubjectMap.Builder subjectMapBuilder;
    private List<RrPredicateObjectMap.Builder> predicateObjectMapBuilders = new ArrayList<>();

    Builder() {
      this.instance = new RrTriplesMap();
    }

    public Builder withUri(Node_URI uri) {
      this.instance.uri = uri;
      return this;
    }

    public Builder withLogicalSource(RrLogicalSource.Builder subBuilder) {
      this.logicalSourceBuilder = subBuilder;
      return this;
    }

    public RrLogicalSource.Builder withLogicalSource() {
      this.logicalSourceBuilder = new RrLogicalSource.Builder();
      return logicalSourceBuilder;
    }

    public Builder withSubjectMap(RrSubjectMap.Builder subBuilder) {
      this.subjectMapBuilder = subBuilder;
      return this;
    }

    public RrSubjectMap.Builder withSubjectMap() {
      this.subjectMapBuilder = new RrSubjectMap.Builder();
      return this.subjectMapBuilder;
    }

    public Builder withPredicateObjectMap(RrPredicateObjectMap.Builder subBuilder) {
      this.predicateObjectMapBuilders.add(subBuilder);
      return this;
    }

    public RrPredicateObjectMap.Builder withPredicateObjectMap() {
      final RrPredicateObjectMap.Builder subBuilder = new RrPredicateObjectMap.Builder();
      this.predicateObjectMapBuilders.add(subBuilder);
      return subBuilder;
    }

    RrTriplesMap build(Function<RrLogicalSource, DataSource> dataSourceFactory) {

      instance.logicalSource = logicalSourceBuilder.build();
      instance.dataSource = dataSourceFactory.apply(instance.logicalSource);

      instance.subjectMap = subjectMapBuilder.build(this::withPredicateObjectMap);
      for (RrPredicateObjectMap.Builder subBuilder : this.predicateObjectMapBuilders) {
        instance.addPredicateObjectMap(subBuilder.build(this.instance, this.instance.dataSource));
      }
      return instance;
    }

    void fixupTripleMapLinks(TripleMapGetter getter) {
      for (RrPredicateObjectMap.Builder subBuilder : this.predicateObjectMapBuilders) {
        subBuilder.fixupTripleMaps(getter);
      }
    }

  }
}
