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

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.junit.Before;
import org.junit.Test;
import test.model.BaseDomainEntity;
import test.model.BaseDomainEntityWithoutSubClasses;
import test.model.projecta.SubADomainEntity;
import test.model.projectb.SubBDomainEntity;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PackageScopeTest {

  public static final Class<BaseDomainEntity> BASE_TYPE = BaseDomainEntity.class;
  private PackageScope instance;

  @Before
  public void setup() throws IOException {
    instance = new PackageScope("test.model.projecta");
  }

  @Test
  public void testFilter() throws IOException {
    DomainEntity inScope = new SubADomainEntity();
    DomainEntity outOfScope = new SubBDomainEntity();
    List<DomainEntity> entities = Lists.newArrayList(inScope, outOfScope);

    assertThat(instance.filter(entities), contains(inScope));
  }


  @Test
  public void mapToScopeTypeReturnsTheTypeInScopeThatMatchesTheBaseType() throws Exception {
    // action
    Class<? extends DomainEntity> scopeType = instance.mapToScopeType(BASE_TYPE);

    // verify
    assertThat(scopeType, is(equalTo(SubADomainEntity.class)));
  }

  @Test(expected = NotInScopeException.class)
  public void mapToScopeTypeThrowsANotInScopeExceptionWhenNoMatchIsFound() throws Exception {
    instance.mapToScopeType(BaseDomainEntityWithoutSubClasses.class);
  }

}
