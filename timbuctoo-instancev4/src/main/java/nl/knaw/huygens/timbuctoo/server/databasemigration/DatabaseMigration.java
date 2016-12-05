package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Optional;

public interface DatabaseMigration {
  void execute(TinkerPopGraphManager graphWrapper) throws IOException;

  class DeafListener implements ChangeListener {

    @Override
    public void onCreate(Collection collection, Vertex vertex) {

    }

    @Override
    public void onPropertyUpdate(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }

    @Override
    public void onRemoveFromCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }

    @Override
    public void onAddToCollection(Collection collection, Optional<Vertex> oldVertex, Vertex newVertex) {

    }
  }
}
