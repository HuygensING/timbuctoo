package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.EntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.database.dto.DirectionalRelationType;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopCreateEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopDeleteEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopGetCollection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopGetEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopUpdateEntity;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class DataAccess {

  private final GraphWrapper graphwrapper;
  private final EntityFetcher entityFetcher;
  private final Authorizer authorizer;
  private final ChangeListener listener;
  private final Vres mappings;
  private final HandleAdder handleAdder;

  public DataAccess(GraphWrapper graphwrapper, EntityFetcher entityFetcher, Authorizer authorizer,
                    ChangeListener listener, HandleAdder handleAdder) {
    this(graphwrapper, entityFetcher, authorizer, listener, null, handleAdder);
  }

  /**
   * @deprecated Use the constructor without the mappings.
   */
  @Deprecated
  public DataAccess(GraphWrapper graphwrapper, EntityFetcher entityFetcher, Authorizer authorizer,
                    ChangeListener listener, Vres mappings, HandleAdder handleAdder) {
    this.graphwrapper = graphwrapper;
    this.entityFetcher = entityFetcher;
    this.authorizer = authorizer;
    this.listener = listener;
    this.mappings = mappings;
    this.handleAdder = handleAdder;
  }

  /**
   * @deprecated Use {@link #executeAndReturn(Function)} or {@link #execute(Function)} method to ensure that the commit
   *     and rollback methods are always called
   */
  @Deprecated
  public DataAccessMethods start() {
    return new DataAccessMethods(graphwrapper, authorizer, listener, entityFetcher, mappings, handleAdder);
  }

  public <T> T executeAndReturn(Function<DataAccessMethods, TransactionStateAndResult<T>> actions) {
    DataAccessMethods db = start();

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

  public void execute(Function<DataAccessMethods, TransactionState> actions) {
    DataAccessMethods db = start();

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

}
