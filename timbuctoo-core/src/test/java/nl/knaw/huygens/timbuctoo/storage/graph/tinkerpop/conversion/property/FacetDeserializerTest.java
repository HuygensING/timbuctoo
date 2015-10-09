package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.facetedsearch.model.FacetOption;
import nl.knaw.huygens.facetedsearch.model.RangeFacet;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FacetDeserializerTest {

  public static final String TITLE = "title";
  public static final String NAME = "name";
  public static final String OPTION_NAME1 = "optionName";
  public static final int COUNT1 = 100;
  public static final String OPTION_NAME2 = "optionName2";
  public static final int COUNT2 = 102;
  public static final int LOWER_LIMIT = 899;
  public static final int UPPER_LIMIT = 1099;
  public static final DeserializationContext NULL_CONTEXT = null;
  private FacetDeserializer instance;
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    instance = new FacetDeserializer();
  }

  @Test
  public void deserializeDeserializesAFacetToADefaultFacet() throws Exception {
    // setup
    DefaultFacet defaultFacet = new DefaultFacet(NAME, TITLE);
    defaultFacet.addOption(new FacetOption(OPTION_NAME1, COUNT1));
    defaultFacet.addOption(new FacetOption(OPTION_NAME2, COUNT2));

    JsonParser parser = getFacetAsJsonParser(defaultFacet);

    // action
    Facet deserializedFacet = instance.deserialize(parser, NULL_CONTEXT);


    assertThat(deserializedFacet, is(instanceOf(DefaultFacet.class)));
    assertThat((DefaultFacet) deserializedFacet, likeDefaultFacet()//
      .withName(NAME) //
      .withTitle(TITLE)
      .withOptions(likeFacetOption().withName(OPTION_NAME1).withCount(COUNT1),
        likeFacetOption().withName(OPTION_NAME2).withCount(COUNT2)));
  }

  private JsonParser getFacetAsJsonParser(Facet facet) throws IOException {
    String value = OBJECT_MAPPER.writeValueAsString(facet);
    return OBJECT_MAPPER.getFactory().createParser(value);
  }


  @Test
  public void deserializeDeserializesAFacetWithTheTypeRANGEToARangeFacet() throws Exception {
    // setup
    RangeFacet rangeFacet = new RangeFacet(NAME, TITLE, LOWER_LIMIT, UPPER_LIMIT);
    JsonParser parser = getFacetAsJsonParser(rangeFacet);

    // action
    Facet deserializedFacet = instance.deserialize(parser, NULL_CONTEXT);

    assertThat(deserializedFacet, is(instanceOf(RangeFacet.class)));
    assertThat((RangeFacet) deserializedFacet, likeRangeFacet() //
      .withName(NAME) //
      .withTitle(TITLE) //
      .withLowerLimit(LOWER_LIMIT) //
      .withUpperLimit(UPPER_LIMIT));

  }

  private static DefaultFacetMatcher likeDefaultFacet() {
    return DefaultFacetMatcher.likeDefaultFacet();
  }

  private static class DefaultFacetMatcher extends FacetMatcher<DefaultFacet, DefaultFacetMatcher> {
    private DefaultFacetMatcher() {

    }

    private static DefaultFacetMatcher likeDefaultFacet() {
      return new DefaultFacetMatcher();
    }

    private Matcher<? super DefaultFacet> withOptions(FacetOptionMatcher... facetOptionMatchers) {
      this.addMatcher(new PropertyMatcher<DefaultFacet, Iterable<? extends FacetOption>>("options", containsInAnyOrder(facetOptionMatchers)) {
        @Override
        protected Iterable<FacetOption> getItemValue(DefaultFacet item) {
          return item.getOptions();
        }
      });
      return this;
    }

  }

  private static FacetOptionMatcher likeFacetOption() {
    return FacetOptionMatcher.likeFacetOption();
  }

  private static class FacetOptionMatcher extends CompositeMatcher<FacetOption> {
    private FacetOptionMatcher() {

    }

    private static FacetOptionMatcher likeFacetOption() {
      return new FacetOptionMatcher();
    }

    public FacetOptionMatcher withName(String name) {
      this.addMatcher(new PropertyEqualityMatcher<FacetOption, String>("name", name) {
        @Override
        protected String getItemValue(FacetOption item) {
          return item.getName();
        }
      });
      return this;
    }

    public FacetOptionMatcher withCount(long count) {
      this.addMatcher(new PropertyEqualityMatcher<FacetOption, Long>("count", count) {
        @Override
        protected Long getItemValue(FacetOption item) {
          return item.getCount();
        }
      });
      return this;
    }


  }

  private static RangeFacetMatcher likeRangeFacet() {
    return RangeFacetMatcher.likeRangeFacet();
  }

  private static class RangeFacetMatcher extends FacetMatcher<RangeFacet, RangeFacetMatcher> {
    private RangeFacetMatcher() {

    }

    public static RangeFacetMatcher likeRangeFacet() {
      return new RangeFacetMatcher();
    }

    public RangeFacetMatcher withLowerLimit(long lowerLimit) {
      this.addMatcher(new PropertyEqualityMatcher<RangeFacet, Long>("lowerLimit", lowerLimit) {

        @Override
        protected Long getItemValue(RangeFacet item) {
          return item.getOptions().get(0).getLowerLimit();
        }
      });
      return this;
    }

    public RangeFacetMatcher withUpperLimit(long upperLimit) {
      this.addMatcher(new PropertyEqualityMatcher<RangeFacet, Long>("upperLimit", upperLimit) {

        @Override
        protected Long getItemValue(RangeFacet item) {
          return item.getOptions().get(0).getUpperLimit();
        }
      });
      return this;
    }
  }


  private static abstract class FacetMatcher<T extends Facet, M extends FacetMatcher<T, M>> extends CompositeMatcher<T> {

    public M withName(String name) {
      this.addMatcher(new PropertyEqualityMatcher<T, String>("name", name) {
        @Override
        protected String getItemValue(T item) {
          return item.getName();
        }
      });

      return (M) this;
    }

    public M withTitle(String title) {
      this.addMatcher(new PropertyEqualityMatcher<T, String>("title", title) {
        @Override
        protected String getItemValue(T item) {
          return item.getTitle();
        }
      });

      return (M) this;
    }
  }


}
