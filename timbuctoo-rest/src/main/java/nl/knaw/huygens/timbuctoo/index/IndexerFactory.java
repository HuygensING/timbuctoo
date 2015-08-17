package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.messages.ActionType;

public class IndexerFactory {

  public Indexer create(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddIndexer();
      case MOD:
        return new UpdateIndexer();
      case DEL:
        return new DeleteIndexer();
      default:
        throw new IllegalArgumentException(String.format("[%s] is not supported by the IndexFactory.", actionType));
    }
  }

}
