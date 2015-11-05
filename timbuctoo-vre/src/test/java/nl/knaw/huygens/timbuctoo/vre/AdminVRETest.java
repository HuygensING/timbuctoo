package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
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

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AdminVRETest {

  private static VRE vre;

  @BeforeClass
  public static void setupVRE() throws IOException {
    List<String> receptionNames = Lists.newArrayList();
    Repository repositoryMock = mock(Repository.class);
    RelationSearcher relationSearcher = mock(RelationSearcher.class);
    vre = new PackageVRE("Admin", "Admin VRE", "timbuctoo.model", repositoryMock, relationSearcher);
  }

  @Test
  public void testPrimitiveEntityTypes() {
    assertTrue(vre.getPrimitiveEntityTypes().contains(Location.class));
    assertTrue(vre.getPrimitiveEntityTypes().contains(Person.class));
    assertFalse(vre.getPrimitiveEntityTypes().contains(User.class));
    assertFalse(vre.getPrimitiveEntityTypes().contains(DCARPerson.class));
  }

  @Test
  public void testTypeAndIdInScope() {
    assertTrue(vre.inScope(Location.class, "id"));
    assertTrue(vre.inScope(Person.class, "id"));
    assertFalse(vre.inScope(DCARPerson.class, "id"));
  }

  @Test
  public void testInstanceInScope() {
    assertTrue(vre.inScope(new Location()));
    assertTrue(vre.inScope(new Person()));
    assertFalse(vre.inScope(new DCARPerson()));
  }

  @Test
  public void testTypeInScope() {
    assertTrue(vre.inScope(Location.class));
    assertTrue(vre.inScope(Person.class));
    assertFalse(vre.inScope(DCARPerson.class));
  }

}
