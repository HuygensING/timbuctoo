package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ErrorObjectTest {
  private static final String EXAMPLE_ERROR =
      "[2019-01-30T10:17:09.848Z] ; file: unknown_prefix1.ttl; method: ImportManager.processLogs; message: Processing" +
          " log failed; error: Namespace prefix 'wrong_in_1' used but not defined [line 58]\n  55:   tim:description " +
          "\"Law\"^^xsd:string ;\n  56:   tim:original_id \"EDU0000930\"^^xsd:string ;\n  57:   a clusius:Education ;" +
          "\n  58:   wrong_in_1:isEducationOf clusius:Persons_PE00011341 .\n";

  @Test
  public void hasDateStamp() {
    final ErrorObject data = ErrorObject.parse(EXAMPLE_ERROR);

    assertThat(data.getDateStamp(), is("[2019-01-30T10:17:09.848Z]"));
  }

  @Test
  public void hasFile() {
    final ErrorObject data = ErrorObject.parse(EXAMPLE_ERROR);

    assertThat(data.getFile(), is("unknown_prefix1.ttl"));
  }

  @Test
  public void hasMethod() {
    final ErrorObject data = ErrorObject.parse(EXAMPLE_ERROR);

    assertThat(data.getMethod(), is("ImportManager.processLogs"));
  }

  @Test
  public void hasMessage() {
    final ErrorObject data = ErrorObject.parse(EXAMPLE_ERROR);

    assertThat(data.getMessage(), is("Processing log failed"));
  }

  @Test
  public void hasError() {
    final ErrorObject data = ErrorObject.parse(EXAMPLE_ERROR);

    assertThat(data.getError(),
        is("Namespace prefix 'wrong_in_1' used but not defined [line 58]\n  55:   tim:description " +
            "\"Law\"^^xsd:string ;\n  56:   tim:original_id \"EDU0000930\"^^xsd:string ;\n  57:   a clusius:Education" +
            " ;\n  58:   wrong_in_1:isEducationOf clusius:Persons_PE00011341 ."));
  }

}
