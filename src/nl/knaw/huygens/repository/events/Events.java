package nl.knaw.huygens.repository.events;

import nl.knaw.huygens.repository.model.Document;

public class Events {

  public static class IndexChangedEvent {

  }

  public static class DocumentChangeEvent<T extends Document> {
    private final Class<T> cls;
    private final String id;

    public DocumentChangeEvent(Class<T> cls, String id) {
      this.cls = cls;
      this.id = id;
    }

    public Class<T> getCls() {
      return cls;
    }

    public String getId() {
      return id;
    }
  }

  public static class DocumentAddEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentAddEvent(Class<T> cls, String id) {
      super(cls, id);
    }
  }

  public static class DocumentEditEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentEditEvent(Class<T> cls, String id) {
      super(cls, id);
    }
  }

  public static class DocumentDeleteEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentDeleteEvent(Class<T> cls, String id) {
      super(cls, id);
    }
  }

}
