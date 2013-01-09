package nl.knaw.huygens.repository.events;

import nl.knaw.huygens.repository.model.Document;

public class Events {
  public static class IndexChangedEvent {

  }

  public static class DocumentChangeEvent<T extends Document> {
    private T doc;
    private Class<T> cls;

    public DocumentChangeEvent(T doc, Class<T> cls) {
      this.doc = doc;
      this.cls = cls;
    }

    public T getDocument() {
      return doc;
    }

    public String getType() {
      return doc.getType();
    }

    public Class<T> getCls() {
      return cls;
    }
  }

  public static class DocumentAddEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentAddEvent(T doc, Class<T> cls) {
      super(doc, cls);
    }
  }

  public static class DocumentEditEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentEditEvent(T doc, Class<T> cls) {
      super(doc, cls);
    }
  }

  public static class DocumentDeleteEvent<T extends Document> extends DocumentChangeEvent<T> {
    public DocumentDeleteEvent(T doc, Class<T> cls) {
      super(doc, cls);
    }
  }
}
