package nl.knaw.huygens.repository.storage;

import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public interface Storage {
    public <T extends Document> T getItem(String id, Class<T> cls);
    public <T extends Document> List<T> getAllByType(Class<T> cls);
    public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls);
    public <T extends Document> void deleteItem(String id, Class<T> cls);
    public <T extends Document> List<T> getAllForQuery(String query, Class<T> cls);
    public List<? extends Document> getAllUntypedForQuery(String query);
    public <T extends Document> List<T> getAllRevisionsOfType(String id, Class<T> cls);
    public List<? extends Document> getAllRevisions(String id, Class<? extends Document> baseCls);
    public void destroy();
}
