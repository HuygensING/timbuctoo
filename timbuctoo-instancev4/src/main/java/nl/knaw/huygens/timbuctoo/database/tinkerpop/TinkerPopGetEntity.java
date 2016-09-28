package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.GetMessage;
import nl.knaw.huygens.timbuctoo.database.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.util.UUID;
import java.util.function.Function;

public class TinkerPopGetEntity implements
  Function<DataAccessMethods, TransactionStateAndResult<GetMessage>> {
  private final Collection collection;
  private final UUID id;
  private final Integer rev;
  private final CustomEntityProperties entityProps;
  private final CustomRelationProperties relationProps;

  public TinkerPopGetEntity(Collection collection, UUID id, Integer rev, CustomEntityProperties entityProps,
                            CustomRelationProperties relationProps) {
    this.collection = collection;
    this.id = id;
    this.rev = rev;
    this.entityProps = entityProps;
    this.relationProps = relationProps;
  }

  @Override
  public TransactionStateAndResult<GetMessage> apply(DataAccessMethods dataAccessMethods) {
    try {
      ReadEntity entity = dataAccessMethods.getEntity(id, rev, collection, entityProps, relationProps);
      return TransactionStateAndResult.commitAndReturn(GetMessage.success(entity));
    } catch (NotFoundException e) {
      return TransactionStateAndResult.rollbackAndReturn(GetMessage.notFound());
    }
  }
}
