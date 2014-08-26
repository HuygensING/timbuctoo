package nl.knaw.huygens.timbuctoo.storage;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class VREAuthorizationFileCollectionTest extends FileCollectionTest<VREAuthorization> {
  private static final String USER_ID = "USER000000000001";
  private static final String VRE_ID = "vreId";
  private static final String USER_ID2 = "USER000000000002";
  private static final String USER_ID1 = "USER000000000003";
  private VREAuthorizationFileCollection instance;

  @Before
  public void setUp() {
    instance = new VREAuthorizationFileCollection();
  }

  @Test
  public void addShouldGiveAuthorizationAndReturnTheId() {
    VREAuthorization authorization = createVREAuthorizationWithDefaultVREId(USER_ID);

    String expectedId = "VREA000000000001";

    verifyAddReturnsAnIdAndAddsItToTheEntity(authorization, expectedId);
  }

  @Test
  public void addShouldCreateAnIdHigherThanTheHighest() {
    VREAuthorization authorization1 = createVREAuthorizationWithDefaultVREId(USER_ID);
    VREAuthorization authorization2 = createVREAuthorizationWithDefaultVREId(USER_ID1);
    VREAuthorization authorization3 = createVREAuthorizationWithDefaultVREId(USER_ID2);

    String expectedId = "VREA000000000003";

    verifyAddIncrementsTheId(authorization1, authorization2, authorization3, expectedId);
  }

  @Test
  public void addAddsTheAuthorizationToItsCollection() {
    VREAuthorization authorization = createVREAuthorizationWithDefaultVREId(USER_ID);

    verifyAddAddsTheEntityToItsCollection(authorization);
  }

  // TODO: Add tests for adding without user id or VRE id.

  @Test
  public void addReturnsTheExistingIdIfTheUserAllreadyHasAnAuthroizationForTheVRE() {
    VREAuthorization authorization1 = createVREAuthorizationWithDefaultVREId(USER_ID);
    VREAuthorization authorization2 = createVREAuthorizationWithDefaultVREId(USER_ID);

    String expectedId = "VREA000000000001";

    instance.add(authorization1);

    String actualId = instance.add(authorization2);

    assertThat(actualId, is(equalTo(expectedId)));
  }

  @Test
  public void findSearchesTheAuthorizationWithUserAndVREId() {
    VREAuthorization authorization = setupInstanceWithAuthorization(USER_ID);

    VREAuthorization exampleVREAuthorization = createVREAuthorizationWithDefaultVREId(USER_ID);
    VREAuthorization exampleVREAuthorization1 = createVREAuthorizationWithDefaultVREId(USER_ID1);

    assertThat(instance.findItem(exampleVREAuthorization), is(equalTo(authorization)));
    assertThat(instance.findItem(exampleVREAuthorization1), is(nullValue(VREAuthorization.class)));
  }

  @Test
  public void findSearchesDoesNotFindAnythingWithoutUserId() {
    setupInstanceWithAuthorization(USER_ID);

    VREAuthorization exampleAuthorization = createVREAuthorizationWithDefaultVREId(null);

    assertThat(instance.findItem(exampleAuthorization), is(nullValue(VREAuthorization.class)));
  }

  private VREAuthorization setupInstanceWithAuthorization(String userId) {
    VREAuthorization authorization = createVREAuthorizationWithDefaultVREId(userId);
    instance.add(authorization);

    return authorization;
  }

  @Test
  public void findSearchesDoesNotFindAnythingWithoutAuthorizationId() {
    setupInstanceWithAuthorization(USER_ID);

    VREAuthorization exampleAuthorization = new VREAuthorization(null, USER_ID);

    assertThat(instance.findItem(exampleAuthorization), is(nullValue(VREAuthorization.class)));
  }

  @Test
  public void getReturnsNullIfTheIdIsNull() {
    setupInstanceWithAuthorization(USER_ID);
    String nullId = null;

    VREAuthorization vreAuthorization = instance.get(nullId);

    assertThat(vreAuthorization, is(nullValue(VREAuthorization.class)));
  }

  @Test
  public void getAllReturnsAllTheKnowEntities() {
    VREAuthorization authorization1 = createVREAuthorizationWithDefaultVREId(USER_ID);
    VREAuthorization authorization2 = createVREAuthorizationWithDefaultVREId(USER_ID1);
    VREAuthorization authorization3 = createVREAuthorizationWithDefaultVREId(USER_ID2);

    instance.add(authorization1);
    instance.add(authorization2);
    instance.add(authorization3);

    verifyGetAllReturnsAllTheKnownEntities(containsInAnyOrder(authorization1, authorization2, authorization3));
  }

  @Test
  public void getAlldReturnsAnEmptyStorageIteratorWhenNoAuthorizationsAreKnown() {

    // action
    StorageIterator<VREAuthorization> authorizations = instance.getAll();

    // verify
    assertThat(authorizations, is(notNullValue()));
    assertThat(authorizations.getAll(), is(empty()));
  }

  @Test
  public void updateSearchesWithTheCombinationOfVREIdAndUserId() {
    // setup
    VREAuthorization original = createVREAuthorizationWithDefaultVREId(USER_ID);
    original.setRoles(Lists.newArrayList("role1"));

    String id = instance.add(original);

    VREAuthorization itemToUpdate = createVREAuthorizationWithDefaultVREId(USER_ID);
    String role2 = "role2";
    itemToUpdate.setRoles(Lists.newArrayList(role2));

    // action
    instance.updateItem(itemToUpdate);

    // verify
    VREAuthorization updated = instance.get(id);

    assertThat(updated, is(notNullValue(VREAuthorization.class)));
    assertThat(updated.getRoles(), contains(role2));
  }

  // TODO find a better way to test this.
  @Test
  public void updateDoesNothingIfTheAuthorizationCannotBeFound() {
    // setup
    VREAuthorization original = createVREAuthorizationWithDefaultVREId(USER_ID);
    String role1 = "role1";
    original.setRoles(Lists.newArrayList(role1));

    String id = instance.add(original);

    VREAuthorization itemToUpdate = createVREAuthorizationWithDefaultVREId(USER_ID1);
    itemToUpdate.setRoles(Lists.newArrayList("role2"));

    // action
    instance.updateItem(itemToUpdate);

    // verify
    VREAuthorization updated = instance.get(id);

    assertThat(updated, is(notNullValue(VREAuthorization.class)));
    assertThat(updated.getRoles(), contains(role1));
  }

  @Test
  public void deleteDeterminesTheAuthorizationByVREIdAndUserId() {
    // setup
    VREAuthorization original = createVREAuthorizationWithDefaultVREId(USER_ID);
    String role1 = "role1";
    original.setRoles(Lists.newArrayList(role1));

    String id = instance.add(original);

    VREAuthorization itemToDelete = createVREAuthorizationWithDefaultVREId(USER_ID);

    // action
    instance.deleteItem(itemToDelete);

    // verify
    assertThat(instance.get(id), is(nullValue(VREAuthorization.class)));
  }

  @Ignore
  @Test
  public void deleteDoesNothingIfAuthorizationCannotBeFound() {
    fail("Yet to be implemented");
  }

  @Test
  public void initializeWithAListOfAuthorizationsMakesThemRetrievable() {
    VREAuthorization authorization = createVREAuthorizationWithDefaultVREId(USER_ID);
    String id = "id";
    authorization.setId(id);
    List<VREAuthorization> authorizations = Lists.newArrayList(authorization);

    VREAuthorizationFileCollection collection = new VREAuthorizationFileCollection(authorizations);

    assertThat(collection.get(id), is(sameInstance(authorization)));
    assertThat(collection.findItem(authorization), is(sameInstance(authorization)));
  }

  @Test
  public void asArrayContainsAllTheItemsOfTheCollections() {
    // setup
    VREAuthorization authorization1 = createVREAuthorizationWithDefaultVREId(USER_ID);
    VREAuthorization authorization2 = createVREAuthorizationWithDefaultVREId(USER_ID1);
    VREAuthorization authorization3 = createVREAuthorizationWithDefaultVREId(USER_ID2);

    instance.add(authorization1);
    instance.add(authorization2);
    instance.add(authorization3);

    // action
    VREAuthorization[] authorizations = instance.asArray();

    // verify
    assertThat(authorizations.length, is(equalTo(3)));
  }

  private VREAuthorization createVREAuthorizationWithDefaultVREId(String userId) {
    return new VREAuthorization(VRE_ID, userId);
  }

  @Override
  protected FileCollection<VREAuthorization> getInstance() {
    return instance;
  }
}
