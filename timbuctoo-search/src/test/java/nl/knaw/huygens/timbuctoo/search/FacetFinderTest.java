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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.timbuctoo.facet.FacetType;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClassNoneFaceted;
import nl.knaw.huygens.timbuctoo.search.model.ComplexAnnotatedClassNotAllFaceted;
import nl.knaw.huygens.timbuctoo.search.model.NonAnnotatedSubClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedClass;
import nl.knaw.huygens.timbuctoo.search.model.SimpleAnnotatedSubClass;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class FacetFinderTest {

  private FacetFinder instance;

  @Before
  public void setUp() {
    instance = new FacetFinder();
  }

  @Test
  public void testFindFacetsAnnotatedClass() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();
    expectedFacets.put("dynamic_s_simple", createFacetInfo("dynamic_s_simple", FacetType.LIST, "Simple"));

    testFindFacets(expectedFacets, SimpleAnnotatedClass.class);
  }

  @Test
  public void testFindFacetsComplexAnnotatedClassAllFaceted() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();
    expectedFacets.put("dynamic_t_complex1", createFacetInfo("dynamic_t_complex1", FacetType.LIST, "Complex1"));
    expectedFacets.put("dynamic_t_complex2", createFacetInfo("dynamic_t_complex2", FacetType.LIST, "Complex2"));

    testFindFacets(expectedFacets, ComplexAnnotatedClass.class);
  }

  @Test
  public void testFindFacetsComplexAnnotatedClassNotAllFaceted() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();
    expectedFacets.put("dynamic_t_complex1", createFacetInfo("dynamic_t_complex1", FacetType.LIST, "Complex1"));

    testFindFacets(expectedFacets, ComplexAnnotatedClassNotAllFaceted.class);
  }

  @Test
  public void testFindFacetsComplexAnnotatedClassNoneFaceted() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();

    testFindFacets(expectedFacets, ComplexAnnotatedClassNoneFaceted.class);
  }

  @Test
  public void testFindFacetsAnnotatedClassAndSuperClass() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();
    expectedFacets.put("dynamic_s_simple", createFacetInfo("dynamic_s_simple", FacetType.LIST, "Simple"));
    expectedFacets.put("dynamic_s_prop", createFacetInfo("dynamic_s_prop", FacetType.LIST, "Property"));

    testFindFacets(expectedFacets, SimpleAnnotatedSubClass.class);
  }

  @Test
  public void testFindFacetsAnnotatedSuperClass() {
    Map<String, FacetInfo> expectedFacets = Maps.newHashMap();
    expectedFacets.put("dynamic_s_simple", createFacetInfo("dynamic_s_simple", FacetType.LIST, "Simple"));

    testFindFacets(expectedFacets, NonAnnotatedSubClass.class);
  }

  private void testFindFacets(Map<String, FacetInfo> expectedFacets, Class<? extends Entity> type) {
    Map<String, FacetInfo> actualFacets = instance.findFacets(type);

    verifyKeys(expectedFacets, actualFacets);
    verifyFacets(expectedFacets, actualFacets);
  }

  private void verifyFacets(Map<String, FacetInfo> expectedFacets, Map<String, FacetInfo> actualFacets) {
    Set<String> keySet = expectedFacets.keySet();
    for (String key : keySet) {
      verifyFacetInfos(expectedFacets.get(key), actualFacets.get(key));
    }

  }

  private FacetInfo createFacetInfo(String name, FacetType facetType, String title) {
    return new FacetInfo().setName(name).setType(facetType).setTitle(title);
  }

  private void verifyKeys(Map<String, FacetInfo> expectedFacets, Map<String, FacetInfo> actualFacets) {
    assertTrue(expectedFacets.keySet().containsAll(actualFacets.keySet()));
    assertTrue(actualFacets.keySet().containsAll(expectedFacets.keySet()));
  }

  private void verifyFacetInfos(FacetInfo facetInfo1, FacetInfo facetInfo2) {
    assertEquals("Facet names not equal", facetInfo1.getName(), facetInfo2.getName());
    assertEquals("Facet titles not equal", facetInfo1.getTitle(), facetInfo2.getTitle());
    assertEquals("Facet types not equal", facetInfo1.getType(), facetInfo2.getType());
  }

}
