package nl.knaw.huygens.timbuctoo.contractdiff.httpdiff;

import com.google.common.collect.LinkedListMultimap;
import org.junit.jupiter.api.Test;

import static nl.knaw.huygens.timbuctoo.contractdiff.httpdiff.ExpectedHeadersAreEqualValidator.validate;

public class ExpectedHeadersAreEqualValidatorTest {

  private LinkedListMultimap<String, String> multimap(String... contents) {
    LinkedListMultimap<String, String> result = LinkedListMultimap.create(contents.length / 2);
    for (int i = 0; i < contents.length; i += 2) {
      result.put(contents[i], contents[i + 1]);
    }
    return result;
  }

  @Test
  public void sameHeader() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", "value"),
        multimap("header1", "value")
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void differingHeader() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", "value"),
        multimap("header1", "other")
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void duplicateHeaderThatMatches() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", "value"),
        multimap(
            "header1", "value",
            "header1", "othervalue"
        )
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void duplicateHeaderThatDoesNotMatch() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", "value"),
        multimap(
            "header1", "other",
            "header1", "othervalue"
        )
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void superFluousHeader() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", "value"),
        multimap(
            "header1", "value",
            "header2", "othervalue"
        )
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void emptyHeader() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", ""),
        multimap(
            "header1", "value"
        )
    );

    System.out.print(result.asConsole());
  }

  @Test
  public void missingHeader() {
    HttpHeadersDiffResult result = validate(
        multimap("header1", ""),
        multimap()
    );

    System.out.print(result.asConsole());
  }

}
