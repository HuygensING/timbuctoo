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
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Relation;
import org.junit.Before;
import org.junit.Test;
import test.model.BaseDomainEntity;
import test.model.BaseDomainEntityWithoutSubClasses;
import test.model.projecta.SubADomainEntity;
import test.model.projectb.ProjectBPerson;
import test.model.projectb.SubBDomainEntity;
import test.model.projectb.SubBRelation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PackageScopeTest {

  public static final Class<BaseDomainEntity> BASE_TYPE = BaseDomainEntity.class;
  public static final Class<ProjectBPerson> IN_SCOPE_TYPE = ProjectBPerson.class;
  public static final Class<SubADomainEntity> OUT_OF_SCOPE_TYPE = SubADomainEntity.class;
  public static final String ANY_ID = "anyId";
  private PackageScope instance;
  public static final DomainEntity IN_SCOPE = new SubBDomainEntity();
  public static final DomainEntity OUT_OF_SCOPE_1 = new SubADomainEntity();
  public static final DomainEntity OUT_OF_SCOPE_2 = new BaseDomainEntity();

  @Before
  public void setup() throws IOException {
    instance = new PackageScope("test.model.projectb");
  }

  @Test
  public void testFilter() throws IOException {
    List<DomainEntity> entities = Lists.newArrayList(IN_SCOPE, OUT_OF_SCOPE_1, OUT_OF_SCOPE_2);

    assertThat(instance.filter(entities), contains(IN_SCOPE));
  }

  @Test
  public void getEntityTypesReturnsAllTheTypesInScope() {
    // action
    Set<Class<? extends DomainEntity>> entityTypes = instance.getEntityTypes();

    assertThat(entityTypes, containsInAnyOrder(SubBRelation.class, SubBDomainEntity.class, ProjectBPerson.class));
  }

  @Test
  public void getPrimitiveEntityTypesReturnsThePrimitivesOfTheTypesInScope() {
    // action
    Set<Class<? extends DomainEntity>> primitiveTypes = instance.getPrimitiveEntityTypes();

    // verify
    assertThat(primitiveTypes, containsInAnyOrder(Relation.class, BaseDomainEntity.class, Person.class));
  }

  @Test
  public void inScopeReturnsTrueIfTheTypeOfTheEntityIsInScope(){
    assertThat(instance.inScope(IN_SCOPE), is(true));
  }

  @Test
  public void inScopeReturnsFalseIfTheTypeOfTheEntityIsInScope(){
    assertThat(instance.inScope(OUT_OF_SCOPE_1), is(false));
  }

  @Test
  public void inScopeReturnsTrueIfTheTypeIsInScope(){
    assertThat(instance.inScope(IN_SCOPE_TYPE), is(true));
  }

  @Test
  public void inScopeReturnsFalseIfTheTypeIsInScope(){
    assertThat(instance.inScope(OUT_OF_SCOPE_TYPE), is(false));
  }

  @Test
  public void inScopeReturnsTrueIfTheTypeIsInScopeAndIgnoresTheId(){
    assertThat(instance.inScope(IN_SCOPE_TYPE, ANY_ID), is(true));
  }

  @Test
  public void inScopeReturnsFalseIfTheTypeIsInScopeAndIgnoresTheId(){
    assertThat(instance.inScope(OUT_OF_SCOPE_TYPE, ANY_ID), is(false));
  }

  @Test
  public void mapToScopeTypeReturnsTheTypeInScopeThatMatchesTheBaseType() throws Exception {
    // action
    Class<? extends DomainEntity> scopeType = instance.mapToScopeType(BASE_TYPE);

    // verify
    assertThat(scopeType, is(equalTo(SubBDomainEntity.class)));
  }

  @Test(expected = NotInScopeException.class)
  public void mapToScopeTypeThrowsANotInScopeExceptionWhenNoMatchIsFound() throws Exception {
    instance.mapToScopeType(BaseDomainEntityWithoutSubClasses.class);
  }

}
