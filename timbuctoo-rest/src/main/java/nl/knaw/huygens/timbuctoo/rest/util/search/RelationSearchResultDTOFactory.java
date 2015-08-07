package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import java.util.List;
import java.util.Map;

public class RelationSearchResultDTOFactory extends RelationMapper{
  public RelationSearchResultDTOFactory(Repository repository) {
    super(repository);
  }

  public List<RelationDTO> create(VRE vre, Class<? extends DomainEntity> type, List<Map<String, Object>> rawData) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
