package nl.knaw.huygens.timbuctoo.config;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.base.BaseLanguage;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntityMapperTest {

  private static EntityMappers mappers;

  @BeforeClass
  public static void setup() throws ModelException {
    TypeRegistry registry = TypeRegistry.getInstance().init("timbuctoo.model.*");
    mappers = new EntityMappers(registry.getDomainEntityTypes());
  }

  @AfterClass
  public static void teardown() {
    mappers = null;
  }

  @Test
  public void testGetEntityMapper() {
    assertNull(mappers.getEntityMapper(DomainEntity.class));
    assertNotNull(mappers.getEntityMapper(Language.class));
    assertNotNull(mappers.getEntityMapper(BaseLanguage.class));
  }

  @Test
  public void testPrimitiveDomainEntityPackage() {
    EntityMapper mapper = mappers.getEntityMapper(Language.class);
    assertEquals(Language.class, mapper.map(Language.class));
    assertEquals(BaseLanguage.class, mapper.map(BaseLanguage.class));
    assertEquals(Location.class, mapper.map(Location.class));
    assertEquals(BaseLocation.class, mapper.map(BaseLocation.class));
  }

  @Test
  public void testProjectDomainEntityPackage() {
    EntityMapper mapper = mappers.getEntityMapper(BaseLanguage.class);
    assertEquals(BaseLanguage.class, mapper.map(Language.class));
    assertEquals(BaseLanguage.class, mapper.map(BaseLanguage.class));
    assertEquals(BaseLocation.class, mapper.map(Location.class));
    assertEquals(BaseLocation.class, mapper.map(BaseLocation.class));
  }

}
