package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.rml.TestRow;
import nl.knaw.huygens.timbuctoo.rml.ThrowingErrorHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import java.util.Optional;

import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RrTemplateTest {
  @Test
  public void replacesTemplatesWithEmptyStringIfValueIsEmpty() throws Exception {
    TestRow row = new TestRow(ImmutableMap.of("foo", ""), ImmutableMap.of(), new ThrowingErrorHandler());
    RrTemplate rrTemplate = new RrTemplate("start {foo} end", TermType.Literal, XSDstring);

    Optional<Node> actual = rrTemplate.generateValue(row);
    assertThat(actual, is(Optional.of(NodeFactory.createLiteral("start  end"))));
  }

  @Test
  public void returnsEmptyIfOneValueIsMissing() throws Exception {
    TestRow row = new TestRow(ImmutableMap.of("foo", ""), ImmutableMap.of(), new ThrowingErrorHandler());
    RrTemplate rrTemplate = new RrTemplate("{foo} and {bar}", TermType.Literal, XSDstring);

    Optional<Node> actual = rrTemplate.generateValue(row);
    assertThat(actual, is(Optional.empty()));
  }

  @Test
  public void iriEncodesValueIfResultTypeIsIri() throws Exception {
    TestRow row = new TestRow(
      ImmutableMap.of("foo", "some s\"tring with wéird characters"),
      ImmutableMap.of(),
      new ThrowingErrorHandler()
    );
    RrTemplate rrTemplate = new RrTemplate("http://{foo}", TermType.IRI, null);

    Optional<Node> actual = rrTemplate.generateValue(row);
    assertThat(
      actual,
      is(Optional.of(NodeFactory.createURI("http://some%20s%22tring%20with%20w%C3%A9ird%20characters")))
    );
  }

}
