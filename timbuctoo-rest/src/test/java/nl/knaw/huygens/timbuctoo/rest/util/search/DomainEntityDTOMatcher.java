package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;

import java.util.Map;

public class DomainEntityDTOMatcher extends CompositeMatcher<DomainEntityDTO> {
  private DomainEntityDTOMatcher() {
  }

  public static DomainEntityDTOMatcher likeDomainEntityDTO() {
    return new DomainEntityDTOMatcher();
  }

  public DomainEntityDTOMatcher withType(Class<? extends DomainEntity> type) {
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntityDTO, String>("type", TypeNames.getExternalName(type)) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getType();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withId(final String id) {
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntityDTO, String>("id", id) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getId();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntityDTO, String>("displayName", displayName) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getDisplayName();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withRawData(Map<String, Object> dataRow) {
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntityDTO, Map<String, ? extends Object>>("rawData", dataRow) {
      @Override
      protected Map<String, ? extends Object> getItemValue(DomainEntityDTO item) {
        return item.getData();
      }
    });
    return this;
  }
}
