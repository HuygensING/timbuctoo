package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * This is a helper class for the LogEntryFactory to find a previous version of an Edge. When no previous version is
 * found the version will be estimated, with the information available from the current Edge.
 */
class EdgeRetriever {

  public static final Logger LOG = LoggerFactory.getLogger(EdgeRetriever.class);

  public Edge getPreviousVersion(Edge edge) {
    Integer rev = edge.<Integer>value("rev");
    String id = edge.value("tim_id");

    Optional<Edge> prev = Optional.empty();
    for (Iterator<Edge> edges = edge.outVertex().edges(Direction.OUT, edge.label()); edges.hasNext(); ) {
      Edge next = edges.next();
      if (next.<Integer>value("rev") == (rev - 1)) {
        prev = Optional.of(next);
      }
    }

    return prev.isPresent() ? prev.get() : estimatePreviousVersion(edge);
  }

  private Edge estimatePreviousVersion(final Edge edge) {
    LOG.info("Estimate missing edge with id {} and rev {}", edge.value("tim_id"), edge.<Integer>value("rev") - 1);
    return new EstimatedEdge(edge);
  }

  private static class EstimatedEdge implements Edge {
    private final Edge edge;

    public EstimatedEdge(Edge edge) {
      this.edge = edge;
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction) {
      return edge.vertices(direction);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
      Iterator<Property<V>> properties = edge.properties(propertyKeys);
      List<Property<V>> newProperties = Lists.newArrayList();

      properties.forEachRemaining(prop -> {
        if (prop.key().contains("accepted")) {
          newProperties.add(new AcceptedTrueProperty<>(prop));
        } else if (Objects.equals(prop.key(), "rev")) {
          newProperties.add(new PreviousRevProperty(prop));
        } else if (Objects.equals(prop.key(), "modified")) {
          newProperties.add(edge.property("created"));
        } else {
          newProperties.add(prop);
        }
      });

      return newProperties.iterator();

    }

    @Override
    public Object id() {
      return null;
    }

    @Override
    public String label() {
      return edge.label();
    }

    @Override
    public Graph graph() {
      return edge.graph();
    }

    @Override
    public <V> Property<V> property(String key, V value) {
      return null;
    }

    @Override
    public void remove() {

    }

    private static class AcceptedTrueProperty<V> implements Property<V> {
      private final Property<V> prop;

      public AcceptedTrueProperty(Property<V> prop) {
        this.prop = prop;
      }

      @Override
      public String key() {
        return prop.key();
      }

      @Override
      @SuppressWarnings("unchecked") // accepted properties must always contain  a boolean value
      public V value() throws NoSuchElementException {
        return (V) Boolean.TRUE;
      }

      @Override
      public boolean isPresent() {
        return prop.isPresent();
      }

      @Override
      public Element element() {
        return prop.element();
      }

      @Override
      public void remove() {

      }
    }

    private class PreviousRevProperty<V> implements Property<V> {
      private final Property<V> prop;

      public PreviousRevProperty(Property<V> prop) {
        this.prop = prop;
      }

      @Override
      public String key() {
        return prop.key();
      }

      @Override
      @SuppressWarnings("unchecked") // rev properties must always be an Integer
      public V value() throws NoSuchElementException {
        Integer value = ((Integer) prop.value()) - 1;
        return (V) value;
      }

      @Override
      public boolean isPresent() {
        return prop.isPresent();
      }

      @Override
      public Element element() {
        return prop.element();
      }

      @Override
      public void remove() {

      }
    }
  }
}

