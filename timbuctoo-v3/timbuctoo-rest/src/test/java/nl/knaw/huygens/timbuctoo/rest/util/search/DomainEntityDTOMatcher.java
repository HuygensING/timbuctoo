package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
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
    this.addMatcher(new PropertyEqualityMatcher<DomainEntityDTO, String>("type", TypeNames.getExternalName(type)) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getType();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withId(final String id) {
    this.addMatcher(new PropertyEqualityMatcher<DomainEntityDTO, String>("id", id) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getId();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withDisplayName(String displayName) {
    this.addMatcher(new PropertyEqualityMatcher<DomainEntityDTO, String>("displayName", displayName) {
      @Override
      protected String getItemValue(DomainEntityDTO item) {
        return item.getDisplayName();
      }
    });
    return this;
  }

  public DomainEntityDTOMatcher withRawData(Map<String, Object> dataRow) {
    this.addMatcher(new PropertyEqualityMatcher<DomainEntityDTO, Map<String, ? extends Object>>("rawData", dataRow) {
      @Override
      protected Map<String, ? extends Object> getItemValue(DomainEntityDTO item) {
        return item.getData();
      }
    });
    return this;
  }
}
