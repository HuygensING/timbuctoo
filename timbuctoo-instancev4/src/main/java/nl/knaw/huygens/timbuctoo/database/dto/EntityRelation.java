package nl.knaw.huygens.timbuctoo.database.dto;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
public interface EntityRelation {

  boolean isAccepted();

  UUID getTimId();

  Collection getCollection();

  int getRevision();
}
