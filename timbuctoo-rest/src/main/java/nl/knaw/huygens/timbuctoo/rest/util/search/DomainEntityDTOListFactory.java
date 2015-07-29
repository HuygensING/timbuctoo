package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;

import java.util.List;
import java.util.Map;

public class DomainEntityDTOListFactory {
  public List<DomainEntityDTO> createFor(Class<? extends DomainEntity> type, List<Map<String, Object>> rawIndexData) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }
}
