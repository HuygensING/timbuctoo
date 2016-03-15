package nl.knaw.huygens.timbuctoo;

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
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DomainEntityMatcher extends CompositeMatcher<DomainEntity>{

  private DomainEntityMatcher(){

  }

  public static DomainEntityMatcher likeDomainEntity(){
    return new DomainEntityMatcher();
  }

  public DomainEntityMatcher ofType(Class<? extends DomainEntity> type){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(DomainEntity item) {
        return item.getClass();
      }
    });

    return this;
  }

  public DomainEntityMatcher withId(String id){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, String>("id", id) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getId();
      }
    });

    return this;
  }

  public DomainEntityMatcher withRevision(int revision){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, Integer>("revision", revision) {
      @Override
      protected Integer getItemValue(DomainEntity item) {
        return item.getRev();
      }
    });

    return this;
  }

  public DomainEntityMatcher withPID(String pid){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, String>("pid", pid) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getPid();
      }
    });

    return this;
  }


}
