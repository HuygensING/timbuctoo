package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import static nl.knaw.huygens.timbuctoo.search.FacetDefinitionMatcher.matchesFacetDefinition;
import static nl.knaw.huygens.timbuctoo.search.FacetDefinitionMatcher.matchesRangeFacetDefinition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.model.ClassWithMultipleFacetTypes;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClassNoneFaceted;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClassNotAllFaceted;
import nl.knaw.huygens.timbuctoo.search.model.NonAnnotatedSubClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedSubClass;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import test.timbuctoo.index.model.ModelWithInvalidRangeFacet;

import com.google.common.collect.Lists;

public class FacetFinderTest {

  private FacetFinder instance;

  @Before
  public void setUp() {
    instance = new FacetFinder();
  }

  @Test
  public void testFindFacetDefinitionsAnnotatedClass() {
    List<FacetDefinitionMatcher> expectedFacets = Lists.newArrayList();
    expectedFacets.add(matchesFacetDefinition("dynamic_s_simple", "Simple", FacetType.LIST));

    testFindFacetDefinitions(SimpleAnnotatedClass.class, containsInAnyOrder(expectedFacets.toArray(new FacetDefinitionMatcher[0])));
  }

  @Test
  public void testFindFacetDefinitionsAnnotatedClassAndSuperClass() {
    List<FacetDefinitionMatcher> expectedFacets = Lists.newArrayList();
    expectedFacets.add(matchesFacetDefinition("dynamic_s_simple", "Simple", FacetType.LIST));
    expectedFacets.add(matchesFacetDefinition("dynamic_s_prop", "Property", FacetType.LIST));

    testFindFacetDefinitions(SimpleAnnotatedSubClass.class, containsInAnyOrder(expectedFacets.toArray(new FacetDefinitionMatcher[0])));
  }

  @Test
  public void testFindFacetDefinitionsAnnotatedSuperClass() {
    testFindFacetDefinitions(NonAnnotatedSubClass.class, contains(matchesFacetDefinition("dynamic_s_simple", "Simple", FacetType.LIST)));
  }

  @Test
  public void testFindFacetDefinitionsComplexAnnotatedClassAllFaceted() {
    List<FacetDefinitionMatcher> expectedFacets = Lists.newArrayList();
    expectedFacets.add(matchesFacetDefinition("dynamic_t_complex1", "Complex1", FacetType.LIST));
    expectedFacets.add(matchesFacetDefinition("dynamic_t_complex2", "Complex2", FacetType.LIST));

    testFindFacetDefinitions(ComplexAnnotatedClass.class, containsInAnyOrder(expectedFacets.toArray(new FacetDefinitionMatcher[0])));
  }

  @Test
  public void testFindFacetDefinitionsComplexAnnotatedClassSomeFaceted() {
    testFindFacetDefinitions(ComplexAnnotatedClassNotAllFaceted.class, contains(matchesFacetDefinition("dynamic_t_complex1", "Complex1", FacetType.LIST)));
  }

  @Test
  public void testFindFacetDefinitionsClassWithMultipleFacetTypes() {
    List<FacetDefinitionMatcher> expectedFacets = Lists.newArrayList();
    expectedFacets.add(matchesFacetDefinition("dynamic_s_list", "", FacetType.LIST));
    expectedFacets.add(matchesFacetDefinition("dynamic_b_boolean", "", FacetType.BOOLEAN));
    expectedFacets.add(matchesRangeFacetDefinition("dynamic_d_range", "", FacetType.RANGE, "dynamic_d_range_low", "dynamic_d_range_high"));
    expectedFacets.add(matchesFacetDefinition("dynamic_p_period", "", FacetType.PERIOD));

    testFindFacetDefinitions(ClassWithMultipleFacetTypes.class, containsInAnyOrder(expectedFacets.toArray(new FacetDefinitionMatcher[0])));
  }

  private void testFindFacetDefinitions(Class<? extends DomainEntity> type, Matcher<Iterable<? extends FacetDefinition>> matcher) {
    List<FacetDefinition> actualFacets = instance.findFacetDefinitions(type);

    assertThat(actualFacets, matcher);

  }

  @Test
  public void testFindFacetDefinitionsComplexAnnotatedClassNonFaceted() {
    List<FacetDefinition> actualFacets = instance.findFacetDefinitions(ComplexAnnotatedClassNoneFaceted.class);
    assertThat(actualFacets, empty());
  }

  @Test(expected = InvalidRangeFacetException.class)
  public void testFindFacetDefinitionsInvalidRangeFacet() {
    instance.findFacetDefinitions(ModelWithInvalidRangeFacet.class);
  }
}
