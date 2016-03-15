package nl.knaw.huygens.timbuctoo.storage.graph;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Test;

import test.model.projecta.SubADomainEntity;

public class EntityInstantiatorTest {
  private static final Class<SubADomainEntity> ENTITY_TYPE = SubADomainEntity.class;

  @Test
  public void createInstanceForReturnAndInstanceForTheType() throws Exception {
    // setup
    EntityInstantiator instance = new EntityInstantiator();

    // action
    SubADomainEntity entity = instance.createInstanceOf(ENTITY_TYPE);

    // verify
    assertThat(entity, is(notNullValue()));
  }

  @Test(expected = InstantiationException.class)
  public void createInstanceThrowsAnInstationExceptionWhenCreateDoes() throws Exception {
    EntityInstantiator instance = new EntityInstantiator() {
      @Override
      protected <T extends Entity> T create(Class<T> type) throws InstantiationException, IllegalAccessException {
        throw new InstantiationException();
      }
    };

    instance.createInstanceOf(ENTITY_TYPE);

  }

  @Test(expected = InstantiationException.class)
  public void createInstanceThrowsAnInstationExceptionWhenCreateThrowsAnIllegalAccessException() throws Exception {
    EntityInstantiator instance = new EntityInstantiator() {
      @Override
      protected <T extends Entity> T create(Class<T> type) throws InstantiationException, IllegalAccessException {
        throw new IllegalAccessException();
      }
    };

    instance.createInstanceOf(ENTITY_TYPE);

  }
}
