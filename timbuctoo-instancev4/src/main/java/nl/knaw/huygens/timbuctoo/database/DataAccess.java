package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.EntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.database.dto.DirectionalRelationType;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopCreateEntity;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopUpdateEntity;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.util.Optional;
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

  public DbCreateEntity createEntity(Collection collection, Optional<Collection> baseCollection, CreateEntity entity) {
    return new TinkerPopCreateEntity(collection, baseCollection, entity);
  }

  public DbUpdateEntity updateEntity(Collection collection, UpdateEntity updateEntity) {
    return new TinkerPopUpdateEntity(collection, updateEntity);
  }
}
