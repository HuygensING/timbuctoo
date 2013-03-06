package nl.knaw.huygens.repository.events;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public class Events {
  public static class IndexChangedEvent {

  }

  public static class DocumentChangeEvent<T extends Document> {
    private List<T> docs;
    private Class<T> cls;

    public DocumentChangeEvent(Class<T> cls, List<T> docs) {
      this.docs = docs;
      this.cls = cls;
    }

    public List<T> getDocuments() {
      return docs;
    }

    public Class<T> getCls() {
      return cls;
    }
  }

  public static class DocumentAddEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentAddEvent(Class<T> cls, List<T> docs) {
      super(cls, docs);
    }
  }

  public static class DocumentEditEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentEditEvent(Class<T> cls, List<T> docs) {
      super(cls, docs);
    }
  }

  public static class DocumentDeleteEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentDeleteEvent(Class<T> cls, List<T> docs) {
      super(cls, docs);
    }
  }
}
