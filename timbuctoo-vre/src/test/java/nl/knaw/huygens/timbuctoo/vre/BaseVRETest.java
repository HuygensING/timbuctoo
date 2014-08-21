package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;

import org.junit.BeforeClass;
import org.junit.Test;

public class BaseVRETest {

  private static VRE vre;

  @BeforeClass
  public static void setupVRE() throws IOException {
    vre = new BaseVRE();
  }

  @Test
  public void testMapPrimitiveTypeName() {
    assertThat(vre.mapPrimitiveTypeName("language"), is(equalTo("baselanguage")));
    assertThat(vre.mapPrimitiveTypeName("location"), is(equalTo("baselocation")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPrimitiveEntityTypes() {
    assertThat(vre.getPrimitiveEntityTypes(), contains(Language.class, Location.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testEntityTypes() {
    assertThat(vre.getEntityTypes(), contains(BaseLanguage.class, BaseLocation.class));
  }

}
