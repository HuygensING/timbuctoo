package nl.knaw.huygens.timbuctoo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.common.collect.Lists;

public class DomainEntityTest {

  @Test
  public void testVariationsCannotBeNull() {
    DomainEntity entity = new BaseDomainEntity();
    assertNotNull(entity.getVariations());

    entity.setVariations(null);
    assertNotNull(entity.getVariations());
  }

  @Test
  public void testResettingVariationsRemovesExistingItems() {
    DomainEntity entity = new BaseDomainEntity();
    assertEquals(0, entity.getVariations().size());

    entity.addVariation("a");
    assertEquals(1, entity.getVariations().size());

    entity.setVariations(null);
    assertEquals(0, entity.getVariations().size());
  }

  @Test
  public void testVariationsCannotContainDuplicates() {
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
