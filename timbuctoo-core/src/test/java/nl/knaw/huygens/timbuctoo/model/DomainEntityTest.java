package nl.knaw.huygens.timbuctoo.model;

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
