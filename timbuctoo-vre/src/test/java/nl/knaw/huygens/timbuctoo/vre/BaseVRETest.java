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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BaseVRETest {

  private static VRE vre;

  @BeforeClass
  public static void setupVRE() throws IOException {
    List<String> receptionNames = Lists.newArrayList();
    Repository repositoryMock = mock(Repository.class);
    vre = new PackageVRE("id", "Base VRE", "timbuctoo.model.base", receptionNames, repositoryMock);
  }

  @AfterClass
  public static void cleanup() {
    vre = null;
  }

  @Test
  public void testMapPrimitiveType() {
    assertEqualTypes(vre.mapTypeName("language", true), BaseLanguage.class);
    assertEqualTypes(vre.mapTypeName("location", true), BaseLocation.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPrimitiveEntityTypes() {
    assertThat(vre.getPrimitiveEntityTypes(), containsInAnyOrder(Language.class, Location.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testEntityTypes() {
    assertThat(vre.getEntityTypes(), containsInAnyOrder(BaseLanguage.class, BaseLocation.class));
  }

  private void assertEqualTypes(Class<?> type1, Class<?> type2) {
    // Hamcrest has problems comparing generic types...
    assertThat(toString(type1), equalTo(toString(type2)));
  }

  private String toString(Class<?> type) {
    return (type != null) ? type.getName() : "null";
  }

}
