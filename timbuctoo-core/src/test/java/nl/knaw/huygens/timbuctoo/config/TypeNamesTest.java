package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.VTestSystemEntity;

import org.junit.Test;

public class TypeNamesTest {

  @Test
  public void testGetInternalName() {
    assertEquals("basedomainentity", TypeNames.getInternalName(BaseDomainEntity.class));
  }

  @Test
  public void testGetExternalName() {
    assertEquals("basedomainentitys", TypeNames.getExternalName(BaseDomainEntity.class));
  }

  @Test
  public void testGetInternalNameForAnnotation() {
    assertEquals("vtestsystementity", TypeNames.getInternalName(VTestSystemEntity.class));
  }

  @Test
  public void testGetExternalNameForAnnotation() {
    assertEquals("mysystementity", TypeNames.getExternalName(VTestSystemEntity.class));
  }

}
