package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TinkerPopGetCollection implements
  Function<DataAccessMethods, TransactionStateAndResult<Stream<ReadEntity>>> {
  private final Collection collection;
  private final int start;
  private final int rows;
  private final boolean withRelations;
  private final CustomEntityProperties entityProps;
  private final CustomRelationProperties relationProps;

  public TinkerPopGetCollection(Collection collection, int start, int rows, boolean withRelations,
                                CustomEntityProperties entityProps,
                                CustomRelationProperties relationProps) {
    this.collection = collection;
    this.start = start;
    this.rows = rows;
    this.withRelations = withRelations;
    this.entityProps = entityProps;
    this.relationProps = relationProps;
  }

  @Override
  public TransactionStateAndResult<Stream<ReadEntity>> apply(DataAccessMethods dataAccessMethods) {
    Stream<ReadEntity> entityStream =
      dataAccessMethods.getCollection(this.collection, start, rows, withRelations, entityProps, relationProps);
    // make sure the entities are read before the transaction closes
    // TODO find a better way to stream the entities from the database
    List<ReadEntity> entities = entityStream.collect(toList());
    return TransactionStateAndResult.commitAndReturn(entities.stream());
  }
}
