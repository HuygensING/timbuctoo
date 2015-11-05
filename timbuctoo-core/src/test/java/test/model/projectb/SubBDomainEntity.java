package test.model.projectb;

/*
 * #%L
 * Timbuctoo core
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import test.model.BaseDomainEntity;

public class SubBDomainEntity extends BaseDomainEntity {

  private String valueb;

  public SubBDomainEntity() {}

  public SubBDomainEntity(String id) {
    setId(id);
  }

  public SubBDomainEntity(String id, String pid) {
    setId(id);
    setPid(pid);
  }

  public String getValueb() {
    return valueb;
  }

  public void setValueb(String value) {
    valueb = value;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
