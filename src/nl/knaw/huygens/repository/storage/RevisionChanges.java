package nl.knaw.huygens.repository.storage;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public interface RevisionChanges<T extends Document> {
  public String getId();
  public List<T> getRevisions();
}
