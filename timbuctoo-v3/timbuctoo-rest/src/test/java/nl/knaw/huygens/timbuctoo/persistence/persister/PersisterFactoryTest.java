package nl.knaw.huygens.timbuctoo.persistence.persister;

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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PersisterFactoryTest {

  private PersisterFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PersisterFactory(mock(Repository.class), mock(PersistenceWrapper.class));
  }

  @Test
  public void forActionTypeCreatesAnAddPersisterWhenTheActionTypeIsADD() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.ADD, AddPersister.class);
  }


  @Test
  public void forActionTypeCreatesAModPersisterWhenTheActionTypeIsMOD() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.MOD, ModPersister.class);
  }

  @Test
  public void forActionTypeCreatesANoOpPersisterWhenTheActionTypeIsDEL() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.DEL, NoOpPersister.class);
  }

  @Test
  public void forActionTypeCreatesANoOpPersisterWhenTheActionTypeIsEND() throws Exception {
    forActionTypeCreatesPersisterOfType(ActionType.END, NoOpPersister.class);
  }

  private void forActionTypeCreatesPersisterOfType(ActionType actionType, Class<? extends Persister> persisterType) {
    // action
    Persister persister = instance.forActionType(actionType);

    // verify
    assertThat(persister, is(instanceOf(persisterType)));
  }
}
