package nl.knaw.huygens.timbuctoo.model;

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

import org.junit.Test;

import com.google.common.collect.Lists;

public class DomainEntityTest {

  @Test
  public void getVariationsCannotBeNull() {
    DomainEntity entity = new BaseDomainEntity();
    assertNotNull(entity.getVariations());

    entity.setVariations(null);
    assertNotNull(entity.getVariations());
  }

  @Test
  public void setVariationsRemovesExistingItems() {
    DomainEntity entity = new BaseDomainEntity();
    assertEquals(0, entity.getVariations().size());

    entity.addVariation("a");
    assertEquals(1, entity.getVariations().size());

    entity.setVariations(null);
    assertEquals(0, entity.getVariations().size());
  }

  @Test
  public void setVariationsEliminatesDuplicates() {
    DomainEntity entity = new BaseDomainEntity();
    entity.setVariations(Lists.newArrayList("a", "b", "a", "b", "b"));
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());
  }

  @Test
  public void addVariationEliminatesDuplicates() {
    DomainEntity entity = new BaseDomainEntity();
    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a"), entity.getVariations());

    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a"), entity.getVariations());

    entity.addVariation("b");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());

    entity.addVariation("a");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());

    entity.addVariation("b");
    assertEquals(Lists.newArrayList("a", "b"), entity.getVariations());
  }

  private static class BaseDomainEntity extends DomainEntity {
    @Override
    public String getDisplayName() {
      return null;
    }
  }

}
