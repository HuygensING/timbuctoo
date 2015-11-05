package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class SearchResultCreationException extends Exception {
  public SearchResultCreationException(Class<? extends DomainEntity> type, Exception cause) {
    super(createMessage(type), cause);
  }

  private static String createMessage(Class<? extends DomainEntity> type) {
    return String.format("Something went wrong while creating a search result for %s", type);
  }
}
