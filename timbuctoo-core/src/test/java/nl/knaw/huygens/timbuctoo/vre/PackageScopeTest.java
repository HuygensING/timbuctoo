package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo core
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
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Test;

import test.variation.model.DomainEntityWithIndexAnnotations;
import test.variation.model.projecta.ProjectADomainEntity;

import com.google.common.collect.Lists;

public class PackageScopeTest {

  @Test
  public void testFilter() throws IOException {
    PackageScope instance = new PackageScope("test.variation.model");

    DomainEntity inScope = new DomainEntityWithIndexAnnotations();
    DomainEntity outOfScope = new ProjectADomainEntity();
    List<DomainEntity> entities = Lists.newArrayList(inScope, outOfScope);

    assertThat(instance.filter(entities), contains(inScope));
  }

}
