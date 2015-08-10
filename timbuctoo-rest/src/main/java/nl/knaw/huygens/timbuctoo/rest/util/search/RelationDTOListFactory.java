package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import java.util.List;
import java.util.Map;

public class RelationDTOListFactory extends RelationMapper {
  private final RelationDTOFactory relationDTOFactory;

  public RelationDTOListFactory(Repository repository, RelationDTOFactory relationDTOFactory) {
    super(repository);
    this.relationDTOFactory = relationDTOFactory;
  }

  public List<RelationDTO> create(VRE vre, Class<? extends DomainEntity> type, List<Map<String, Object>> rawData) throws SearchResultCreationException {
    List<RelationDTO> dtos = Lists.newArrayList();
    for (Map<String, Object> rawDataRow : rawData) {
      try {
        dtos.add(relationDTOFactory.create(vre, type, rawDataRow));
      } catch (NotInScopeException | SearchException | MappingException e) {
        throw new SearchResultCreationException(type, e);
      }
    }
    return dtos;
  }
}
