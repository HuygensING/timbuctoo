package nl.knaw.huygens.timbuctoo.persistence;

/*
 * #%L
 * Timbuctoo services
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.persistence.PersistenceManager;
import nl.knaw.huygens.timbuctoo.config.Paths;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.TestSystemEntity;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersistenceWrapperTest {

  private static final String URL_WITHOUT_ENDING_SLASH = "http://test.nl";
  private static final String DEFAULT_ID = "1234";
  private static final String URL_WITH_ENDING_SLASH = "http://test.nl/";
  private static final Class<ProjectADomainEntity> DEFAULT_DOMAIN_TYPE = ProjectADomainEntity.class;
  private static final Class<TestSystemEntity> DEFAULT_SYSTEM_TYPE = TestSystemEntity.class;
  public static final String HANDLE_PREFIX = "http://hdl.handle.net/11240/";
  public static final String PID = "pid";
  private PersistenceManager persistenceManager;

  @Before
  public void setUp() {
    persistenceManager = mock(PersistenceManager.class);
  }

  private PersistenceWrapper createInstance(String url) {
    return new PersistenceWrapper(url, persistenceManager);
  }

  @Test
  public void testPersistDomainEntitySuccess() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstanceWithoutEndingSlash();
    persistenceWrapper.persistObject(DEFAULT_DOMAIN_TYPE, DEFAULT_ID);
    verifyADomainEntityIsPersisted("basedomainentities/1234");
  }

  private PersistenceWrapper createInstanceWithoutEndingSlash() {
    return createInstance(URL_WITHOUT_ENDING_SLASH);
  }

  @Test
  public void testPersistSystemEntitySuccess() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstanceWithoutEndingSlash();
    persistenceWrapper.persistObject(DEFAULT_SYSTEM_TYPE, DEFAULT_ID);
    verify(persistenceManager).persistURL(createURL(Paths.SYSTEM_PREFIX, "testsystementities/1234"));
  }

  @Test
  public void testPersistDomainEntityWithRevision() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstanceWithoutEndingSlash();
    persistenceWrapper.persistObject(DEFAULT_DOMAIN_TYPE, DEFAULT_ID, 12);
    verifyADomainEntityIsPersisted("basedomainentities/1234?rev=12");
  }

  @Test
  public void testPersistObjectSuccesUrlEndOnSlash() throws PersistenceException {
    PersistenceWrapper persistenceWrapper = createInstance(URL_WITH_ENDING_SLASH);
    persistenceWrapper.persistObject(DEFAULT_DOMAIN_TYPE, DEFAULT_ID);
    verifyADomainEntityIsPersisted("basedomainentities/1234");
  }

  @Test(expected = PersistenceException.class)
  public void testPersistObjectException() throws PersistenceException {
    when(persistenceManager.persistURL(anyString())).thenThrow(new PersistenceException("error"));
    PersistenceWrapper persistenceWrapper = createInstance(URL_WITH_ENDING_SLASH);
    persistenceWrapper.persistObject(DEFAULT_DOMAIN_TYPE, DEFAULT_ID);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewPersistentWrapperBaseUrlIsEmpty() throws PersistenceException {
    new PersistenceWrapper("", persistenceManager);
  }

  @Test
  public void updatePIDUpdatesThePIDOfTheEntity() throws PersistenceException {
        PersistenceWrapper persistenceWrapper = createInstance(URL_WITH_ENDING_SLASH);
    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.setPid(String.format(HANDLE_PREFIX + "%s", PID));
    entity.setId(DEFAULT_ID);
    entity.setRev(10);

    // action
    persistenceWrapper.updatePID(entity);

    // verify
    String expectedUrl = createURL(Paths.DOMAIN_PREFIX, String.format("basedomainentities/1234?rev=10"));
    verify(persistenceManager).modifyURLForPersistentId(PID, expectedUrl);
  }


  private void verifyADomainEntityIsPersisted(String entityPath) throws PersistenceException {
    verify(persistenceManager).persistURL(createURL(Paths.DOMAIN_PREFIX, entityPath));
  }

  private String createURL(String... parts) {
    List<String> urlParts = Lists.newArrayList(URL_WITHOUT_ENDING_SLASH);
    urlParts.addAll(Arrays.asList(parts));

    return Joiner.on("/").join(urlParts);
  }

}
