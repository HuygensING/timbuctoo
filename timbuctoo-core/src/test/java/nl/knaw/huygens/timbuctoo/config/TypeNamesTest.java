package nl.knaw.huygens.timbuctoo.config;

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
