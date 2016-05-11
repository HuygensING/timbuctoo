package nl.knaw.huygens.timbuctoo.crud.changelistener;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.crud.ChangeListener;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;
import java.util.Set;

public class CompositeChangeListener implements ChangeListener {

  private final Set<ChangeListener> subListeners;

  public CompositeChangeListener(ChangeListener... listeners) {
    subListeners = Sets.newHashSet(listeners);
  }

  @Override
  public void onCreate(Vertex vertex) {
    subListeners.forEach(l -> l.onCreate(vertex));
  }

  @Override
  public void onUpdate(Optional<Vertex> oldVertex, Vertex newVertex) {
    subListeners.forEach(l -> l.onUpdate(oldVertex, newVertex));
  }
}
