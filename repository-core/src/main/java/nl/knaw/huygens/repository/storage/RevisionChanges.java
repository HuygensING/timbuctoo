package nl.knaw.huygens.repository.storage;

import java.util.List;

import nl.knaw.huygens.repository.model.Entity;

public interface RevisionChanges<T extends Entity> {

  public String getId();

  public List<T> getRevisions();

}
