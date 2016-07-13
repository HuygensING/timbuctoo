package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.shaded.jackson.databind.node.JsonNodeFactory;

import java.time.Clock;
import java.util.UUID;

/**
 * FIXME: deduplicate code in TinkerpopJsonCrudService
 */
public class SystemPropertyModifier {

  private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
  private final Clock clock;

  public SystemPropertyModifier(Clock clock) {
    this.clock = clock;
  }

  public void setCreated(Element element, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    element.property("created", value);
    element.property("modified", value);
  }

  public void setModified(Element element, String userId) {
    String value = String.format("{\"timeStamp\":%s,\"userId\":%s}",
      clock.millis(),
      nodeFactory.textNode(userId)
    );
    element.property("modified", value);
  }

  public void setTimId(Element element) {
    element.property("tim_id", UUID.randomUUID().toString());
  }

  public void setRev(Element element, int rev) {
    element.property("rev", rev);
  }

  public void setIsLatest(Element element, boolean isLatest) {
    element.property("isLatest", isLatest);
  }

  public void setIsDeleted(Element element, boolean isDeleted) {
    element.property("deleted", isDeleted);
  }
}
