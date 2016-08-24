package nl.knaw.huygens.timbuctoo.database.dto;


import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface RelationCreateDescription {

  Optional<EntityRelation> getExistingRelation();

  Collection getCollection();

  String getSourceId();

  String getTargetId();

  RelationType getDescription();
}
