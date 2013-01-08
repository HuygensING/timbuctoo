package nl.knaw.huygens.repository.storage.mongo;

import java.util.List;

public interface RevisionChanges {

  public interface Rev {
    public Object fromNext();
    public Object fromPrev();
  }
  public String getId();
  public List<Rev> getRevisions();
  public int getLastRev();
  public Object getOriginal();
}
