package nl.knaw.huygens.timbuctoo.index;

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
