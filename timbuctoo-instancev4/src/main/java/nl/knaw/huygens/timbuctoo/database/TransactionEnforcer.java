package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopCreateEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopDeleteEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopGetCollection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopGetEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopUpdateEntity;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransactionEnforcer {

  private final Supplier<DataStoreOperations> dataStoreOperationsSupplier;

  public TransactionEnforcer(Supplier<DataStoreOperations> dataStoreOperationsSupplier) {
    this.dataStoreOperationsSupplier = dataStoreOperationsSupplier;
  }

  /**
   * @deprecated Use {@link #executeAndReturn(Function)} or {@link #execute(Function)} method to ensure that the commit
   *     and rollback methods are always called
   */
  @Deprecated
  public DataStoreOperations start() {
    return dataStoreOperationsSupplier.get();
  }

  public <T> T executeAndReturn(Function<DataStoreOperations, TransactionStateAndResult<T>> actions) {
    DataStoreOperations db = start();

    try {
      TransactionStateAndResult<T> result = actions.apply(db);
      if (result.wasCommitted()) {
        db.success();
      } else {
        db.rollback();
      }
      return result.getValue();
    } catch (RuntimeException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  public void execute(Function<DataStoreOperations, TransactionState> actions) {
    DataStoreOperations db = start();

    try {
      TransactionState result = actions.apply(db);
      if (result.wasCommitted()) {
        db.success();
      } else {
        db.rollback();
      }
    } catch (RuntimeException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  public TransactionState createEntity(Collection collection,
                                       Optional<Collection> baseCollection,
                                       CreateEntity entity) {
    return executeAndReturn(new TinkerPopCreateEntity(collection, baseCollection, entity));
  }

  public UpdateReturnMessage updateEntity(Collection collection, UpdateEntity updateEntity) {
    return executeAndReturn(new TinkerPopUpdateEntity(collection, updateEntity));
  }

  public GetMessage getEntity(Collection collection, UUID id,
                              Integer rev,
                              CustomEntityProperties entityProps,
                              CustomRelationProperties relationProps) {
    return executeAndReturn(new TinkerPopGetEntity(collection, id, rev, entityProps, relationProps));
  }

  public DeleteMessage deleteEntity(Collection collection, UUID id, Change modified) {
    return executeAndReturn(new TinkerPopDeleteEntity(collection, id, modified));
  }

  public DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                              boolean withRelations, CustomEntityProperties entityProps,
                                              CustomRelationProperties relationProps) {

    return new TinkerPopGetCollection(collection, start, rows, withRelations, entityProps, relationProps, start());
  }

  public CreateMessage createRelation(Collection collection, CreateRelation createRelation) {
    return executeAndReturn(new TinkerPopCreateRelation(collection, createRelation));
  }

  public UpdateReturnMessage updateRelation(Collection collection, UpdateRelation updateRelation) {
    return executeAndReturn(new TinkerPopUpdateRelation(collection, updateRelation));
  }
}
