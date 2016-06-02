package nl.knaw.huygens.timbuctoo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.io.IOException;

public class EdgeMatcher extends CompositeMatcher<Edge> {

  private final ObjectMapper objectMapper;

  private EdgeMatcher() {
    objectMapper = new ObjectMapper();
  }

  public static EdgeMatcher likeEdge() {
    return new EdgeMatcher();
  }

  public EdgeMatcher withLabel(String label) {
    this.addMatcher(new PropertyEqualityMatcher<Edge, String>("label", label) {
      @Override
      protected String getItemValue(Edge item) {
        return item.label();
      }
    });
    return this;
  }

  public EdgeMatcher withModifiedTimestamp(long value) {
    this.addMatcher(new PropertyEqualityMatcher<Edge, Long>("modifiedTimeStamp", value) {
      @Override
      protected Long getItemValue(Edge item) {
        try {
          Change modified = objectMapper.readValue(item.<String>value("modified"), Change.class);
          return modified.getTimeStamp();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });

    return this;
  }

  public EdgeMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<Edge, String>("id", id) {
      @Override
      protected String getItemValue(Edge item) {
        return item.value("tim_id");
      }
    });
    return this;
  }
}
