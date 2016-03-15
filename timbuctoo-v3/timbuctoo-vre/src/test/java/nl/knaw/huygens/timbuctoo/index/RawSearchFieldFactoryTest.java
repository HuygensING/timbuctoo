package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo VRE
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
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Before;
import org.junit.Test;

public class RawSearchFieldFactoryTest {
  private RawSearchFieldFactory instance;

  @Before
  public void setup() {
    instance = new RawSearchFieldFactory();
  }

  @Test
  public void getRawSearchFieldReturnsTheValueOfThenAnnotationOfTheClassItIsOn() {
    String rawSearchField = instance.getRawSearchField(DerivedWithAnnotation.class);

    assertThat(rawSearchField, is(DerivedWithAnnotation.DERIVED_SEARCH_FIELD));
  }

  @Test
  public void getRawSearchFieldTheValueOfTheAnnotationOfTheSuperClassIfTheClassHasNone() {
    String rawSearchField = instance.getRawSearchField(DerivedWithAnnotationInTree.class);

    assertThat(rawSearchField, is(BaseWithAnnotation.BASE_SEARCH_FIELD));
  }

  @Test
  public void getRawSearchFieldReturnsAnEmptyStringIfNoAnnotationsAreFound() {
    String rawSearchField = instance.getRawSearchField(DerivedWithoutAnnotation.class);

    assertThat(rawSearchField, is(""));
  }

  private static class BaseWithoutAnnotation extends DomainEntity {

    @Override
    public String getIdentificationName() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }

  }

  private static class DerivedWithoutAnnotation extends BaseWithoutAnnotation {

  }

  @RawSearchField(BaseWithAnnotation.BASE_SEARCH_FIELD)
  private static class BaseWithAnnotation extends DomainEntity {

    static final String BASE_SEARCH_FIELD = "baseSearchField";

    @Override
    public String getIdentificationName() {
      throw new UnsupportedOperationException("Yet to be implemented");
    }
  }

  @RawSearchField(DerivedWithAnnotation.DERIVED_SEARCH_FIELD)
  private static class DerivedWithAnnotation extends BaseWithAnnotation {

    static final String DERIVED_SEARCH_FIELD = "derivedSearchField";

  }

  private static class DerivedWithAnnotationInTree extends BaseWithAnnotation {

  }
}
