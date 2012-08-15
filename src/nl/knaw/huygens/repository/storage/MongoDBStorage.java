package nl.knaw.huygens.repository.storage;

import java.net.UnknownHostException;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDBStorage implements Storage {
    private Mongo mongo;
    private DB db;
    public MongoDBStorage(String host, int port, String dbName) throws UnknownHostException, MongoException {
        this(host, port, dbName, null, null);
    }
    
    public MongoDBStorage(String host, int port, String dbName, String user, String password) throws UnknownHostException, MongoException {
        mongo = new Mongo(host, port);
        db = mongo.getDB(dbName);
        if (user != null && password != null) {
            db.authenticate(user, password.toCharArray());
        }
    }

    @Override
    public <T extends Document> T getItem(String id, Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Document> List<T> getAllByType(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T extends Document> void deleteItem(String id, Class<T> cls) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T extends Document> List<T> getAllForQuery(String query, Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Document> getAllUntypedForQuery(String query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Document> List<T> getAllRevisionsOfType(String id, Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Document> getAllRevisions(String id, Class<? extends Document> baseCls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

}
