package nl.knaw.huygens.concordion.extensions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.ValueMatcherException;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class JsonSpecComparatorTest {

  private JsonSpecComparator.ArraySpecMatcher arrayMatcher;
  private JsonSpecComparator instance;
  public static final String PREFIX = "prefix";
  private JSONCompareResult jsonCompareResult;
  private RegularExpressionValueMatcher<Object> regexMatcher;

  @Before
  public void setUp() throws Exception {
    arrayMatcher = mock(JsonSpecComparator.ArraySpecMatcher.class);
    regexMatcher = mock(RegularExpressionValueMatcher.class);
    instance = new JsonSpecComparator(arrayMatcher, regexMatcher);
    jsonCompareResult = mock(JSONCompareResult.class);
  }

  @Test
  public void compareValuesWhenTheExpectedValueIsAnArrayTheValueIsComparedByTheArrayMatcher() throws Exception {
    // setup
    when(arrayMatcher.equal(any(), any())).thenReturn(true);

    JSONArray expectedValue = new JSONArray();
    expectedValue.put(8);
    JSONArray actualValue = new JSONArray();
    actualValue.put(5);

    // action
    JSONCompareResult jsonCompareResult = mock(JSONCompareResult.class);
    String prefix = "prefix";
    instance.compareValues(prefix, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(arrayMatcher).equal(prefix, actualValue, expectedValue, jsonCompareResult);
    verifyZeroInteractions(jsonCompareResult);
  }

  @Test
  public void compareValuesDoesNothingWhenTheReturnOfArraySpecMatcherEqualReturnsFalse() throws Exception {
    // setup
    when(arrayMatcher.equal(any(), any())).thenReturn(false);

    JSONArray expectedValue = new JSONArray();
    expectedValue.put(8);
    JSONArray actualValue = new JSONArray();
    actualValue.put(5);

    // action
    JSONCompareResult jsonCompareResult = mock(JSONCompareResult.class);
    String prefix = "prefix";
    instance.compareValues(prefix, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(arrayMatcher).equal(prefix, actualValue, expectedValue, jsonCompareResult);
    verifyZeroInteractions(jsonCompareResult);
  }

  @Test
  public void compareValuesAddsAFailureToTheResultWhenArraySpecMatcherThrowsAValueMatcherException() throws Exception {
    // setup
    when(arrayMatcher.equal(any(), any())).thenThrow(new ValueMatcherException("", "", ""));

    JSONArray expectedValue = new JSONArray();
    expectedValue.put(8);
    JSONArray actualValue = new JSONArray();
    actualValue.put(5);

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(arrayMatcher).equal(PREFIX, actualValue, expectedValue, jsonCompareResult);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void compareValuesThrowsAnIllegalArgumentExceptionIfTheExpectedValueIsNotAnArrayOrAString() throws Exception {
    // setup
    int expectedValue = 12;
    String test = "test";

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(expectedValue + " is not a supported expectation");

    // action
    instance.compareValues(PREFIX, expectedValue, test, jsonCompareResult);
  }

  @Test
  public void compareValuesDoesNotThrowAnIllegalArgumentExceptionWhenTheTypeIsAJsonObject() throws Exception {
    // setup
    JSONObject expectedValue = new JSONObject();
    JSONObject actualValue = new JSONObject();

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);
  }

  @Test
  public void compareValuesCallsFailOnJsonCompareResultWhenTheExpectedValueSpecifiesANumberAndTheActualValueIsNotANumber() throws Exception {
    // setup
    String expectedValue = "?Number";
    String actualValue = "12";

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(jsonCompareResult).fail(PREFIX, "a number", actualValue);
  }

  @Test
  public void compareValuesDoesNotCallFailOnJsonCompareResultWhenTheExpectedValueSpecifiesANumberAndTheActualValueIsANumber() throws Exception {
    // setup
    String expectedValue = "?Number";
    Number actualValue = 12;

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verifyZeroInteractions(jsonCompareResult);
  }

  @Test
  public void compareValuesCallsFailOnJsonCompareResultWhenTheExpectedValueSpecifiesABooleanAndTheActualValueIsNotABoolean() throws Exception {
    // setup
    String expectedValue = "?Boolean";
    String actualValue = "true";

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(jsonCompareResult).fail(PREFIX, "a boolean", actualValue);
  }

  @Test
  public void compareValuesDoesNotCallFailOnJsonCompareResultWhenTheExpectedValueSpecifiesABooleanAndTheActualValueIsABoolean() throws Exception {
    // setup
    String expectedValue = "?Boolean";
    boolean actualValue = true;

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verifyZeroInteractions(jsonCompareResult);
  }

  @Test
  public void compareValuesCallsFailOnJsonCompareResultWhenTheExpectedValueSpecifiesAnUnsupportedDataType() throws Exception {
    // setup
    String expectedValue = "?Foo";
    boolean actualValue = true;

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(jsonCompareResult).fail(argThat(is(PREFIX)), any(ValueMatcherException.class));
  }

  @Test
  public void compareValuesCallsRegexMatcherEqualWhenTheExpectedValueDoesNotSpecifyADataType() throws Exception {
    // setup
    String expectedValue = ".*";
    Object actualValue = 123;

    when(regexMatcher.equal(any(), any())).thenReturn(true);
    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(regexMatcher).equal("" + actualValue, expectedValue);
    verifyZeroInteractions(jsonCompareResult);
  }

  @Test
  public void compareValuesCallsFailOnJsonCompareResultWhenRegexMatcherEqualsReturnsFalse() throws Exception {
    // setup
    String expectedValue = ".*";
    Object actualValue = 123;

    when(regexMatcher.equal(any(), any())).thenReturn(false);

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(regexMatcher).equal("" + actualValue, expectedValue);
    verify(jsonCompareResult).fail(PREFIX, expectedValue, actualValue);
  }

  @Test
  public void compareValuesCallsFailOnJsonCompareResultWhenRegexMatcherThrowsValueMatcherException() throws Exception {
    // setup
    String expectedValue = ".*";
    Object actualValue = 123;

    ValueMatcherException valueMatcherException = new ValueMatcherException("", "", "");
    when(regexMatcher.equal(any(), any())).thenThrow(valueMatcherException);

    // action
    instance.compareValues(PREFIX, expectedValue, actualValue, jsonCompareResult);

    // verify
    verify(regexMatcher).equal("" + actualValue, expectedValue);
    verify(jsonCompareResult).fail(PREFIX, valueMatcherException);
  }

}
