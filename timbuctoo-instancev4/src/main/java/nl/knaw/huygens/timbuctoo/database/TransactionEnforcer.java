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
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopUpdateEntity;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransactionEnforcer {

  private final Supplier<DataStoreOperations> dataStoreOperationsSupplier;
  private final TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory;

  public TransactionEnforcer(Supplier<DataStoreOperations> dataStoreOperationsSupplier,
                             TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory) {
    this.dataStoreOperationsSupplier = dataStoreOperationsSupplier;
    this.timbuctooActionsFactory = timbuctooActionsFactory;
  }

  public <T> T oldExecuteAndReturn(Function<DataStoreOperations, TransactionStateAndResult<T>> actions) {
    DataStoreOperations db = dataStoreOperationsSupplier.get();

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

  public <T> T executeAndReturn(Function<TimbuctooActions, TransactionStateAndResult<T>> action) {
    DataStoreOperations db = dataStoreOperationsSupplier.get();

    TimbuctooActions timbuctooActions = timbuctooActionsFactory.create(this, db);
    try {
      TransactionStateAndResult<T> result = action.apply(timbuctooActions);
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
    DataStoreOperations db = dataStoreOperationsSupplier.get();

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
    return oldExecuteAndReturn(new TinkerPopCreateEntity(collection, baseCollection, entity));
  }

  public UpdateReturnMessage updateEntity(Collection collection, UpdateEntity updateEntity) {
    return oldExecuteAndReturn(new TinkerPopUpdateEntity(collection, updateEntity));
  }

  public DeleteMessage deleteEntity(Collection collection, UUID id, Change modified) {
    return oldExecuteAndReturn(new TinkerPopDeleteEntity(collection, id, modified));
  }

  public DataStream<ReadEntity> getCollection(Collection collection, int start, int rows,
                                              boolean withRelations, CustomEntityProperties entityProps,
                                              CustomRelationProperties relationProps) {

    return new TinkerPopGetCollection(
      collection,
      start,
      rows,
      withRelations,
      entityProps,
      relationProps,
      dataStoreOperationsSupplier.get());
  }

  public CreateMessage createRelation(Collection collection, CreateRelation createRelation) {
    return oldExecuteAndReturn(new TinkerPopCreateRelation(collection, createRelation));
  }

  public UpdateReturnMessage updateRelation(Collection collection, UpdateRelation updateRelation) {
    return oldExecuteAndReturn(new TinkerPopUpdateRelation(collection, updateRelation));
  }
}
