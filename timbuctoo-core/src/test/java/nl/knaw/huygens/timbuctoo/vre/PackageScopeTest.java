package nl.knaw.huygens.timbuctoo.vre;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.DomainEntityWithIndexAnnotations;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PackageScopeTest {
  // mock
  DomainEntity inscopeEntity = new DomainEntityWithIndexAnnotations();
  DomainEntity outOfScopeEnitty = new ProjectADomainEntity();

  @Test
  public void testFilter() throws IOException {
    PackageScope instance = new PackageScope("timbuctoo.variation.model");

    List<DomainEntity> entities = Lists.newArrayList();
    entities.add(inscopeEntity);
    entities.add(outOfScopeEnitty);

    // action
    List<DomainEntity> filteredResult = instance.filter(entities);

    // verify
    assertThat(filteredResult, contains(inscopeEntity));

  }
}
