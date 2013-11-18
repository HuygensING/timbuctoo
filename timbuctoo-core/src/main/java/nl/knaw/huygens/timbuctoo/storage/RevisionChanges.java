package nl.knaw.huygens.timbuctoo.storage;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

public interface RevisionChanges<T extends Entity> {

  String getId();

  List<T> getRevisions();

}
