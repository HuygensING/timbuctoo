package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TinkerPopGetCollection implements DataStream<ReadEntity> {
  private final Collection collection;
  private final int start;
  private final int rows;
  private final boolean withRelations;
  private final CustomEntityProperties entityProps;
  private final CustomRelationProperties relationProps;
  private final DataStoreOperations dataStoreOperations;

  public TinkerPopGetCollection(Collection collection, int start, int rows, boolean withRelations,
                                CustomEntityProperties entityProps, CustomRelationProperties relationProps,
                                DataStoreOperations dataStoreOperations) {
    this.collection = collection;
    this.start = start;
    this.rows = rows;
    this.withRelations = withRelations;
    this.entityProps = entityProps;
    this.relationProps = relationProps;
    this.dataStoreOperations = dataStoreOperations;
  }

  @Override
  public <U> List<U> map(Function<ReadEntity, U> mapping) {
    Stream<ReadEntity> entities =
      dataStoreOperations.getCollection(this.collection, rows, start, withRelations, entityProps, relationProps);
    List<U> collect = entities.map(mapping).collect(Collectors.toList());
    dataStoreOperations.success();
    dataStoreOperations.close();
    return collect;
  }

}
