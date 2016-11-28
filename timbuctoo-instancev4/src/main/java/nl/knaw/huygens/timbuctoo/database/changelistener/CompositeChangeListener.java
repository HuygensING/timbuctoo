package nl.knaw.huygens.timbuctoo.database.changelistener;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;
import java.util.Set;

public class CompositeChangeListener implements ChangeListener {

  private final Set<ChangeListener> subListeners;

  public CompositeChangeListener(ChangeListener... listeners) {
    subListeners = Sets.newHashSet(listeners);
  }

  @Override
  public void onCreate(Collection collection, Vertex vertex) {
    subListeners.forEach(l -> l.onCreate(collection, vertex));
  }

  @Override
  public void onUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {
    subListeners.forEach(l -> l.onUpdate(collection, oldVertex, newVertex));
  }
}
