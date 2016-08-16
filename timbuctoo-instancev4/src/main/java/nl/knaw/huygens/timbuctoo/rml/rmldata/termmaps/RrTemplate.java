package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

import nl.knaw.huygens.timbuctoo.rml.Row;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RrTemplate implements RrTermMap {
  private final String template;
  private final TermType termType;
  private final Pattern pattern;

  public RrTemplate(String template, TermType termType) {
    this.template = template;
    this.termType = termType;
    //regex can be tested by going to https://regex101.com/r/fV1zJ1/1
    //It has been tested with
    // http://jan/{} <- should not match
    // http://jan/{foo}
    // http://jan/{fo\}o}
    // http://jan/{fo\}o\\}
    // http://jan/{fo\}o\\\}}
    // http://jan/{fo\}o\\\\}} <- should mark before-last curly as the end of the group
    // http://jan/{foo}/{bar}
    // http://jan/{foo}/{bar}?really=\{as} <- should not match as
    // http://jan/{foo}/{bar}?really=\\{as} <- should match as

    //Explanation:
    //  (?<!\\)     Negative Lookbehind: will make the regex fizzle if preceded by \ (\\ to escape it)
    //  (?:\\\\)*   (?:) is a non-matching group (we group, but the result does not end up in the matches)
    //              (?:\\\\)* matches any even number (or zero) of backslashes
    //  \{( ... )\} Here the real matching starts with the literal { immediately after it we open the matching group (we
    //              want whatever's inside the curly's)
    //  (?: ... )+  We're now going to repeatedly (at least once) encounter any of three options
    //
    //  [^\\\}]     a character that is not { or \
    //  (?:(?<!\\)(?:\\\\)+) an even number of slashes not preceded by a slash
    //  \\\}        the exact combination \}
    //
    // nothing matches an odd number of slashes so \\} is matched in two steps: first \\ is matched as "an even number
    // of slashes" then the } is matched as the end of the regex. On the other hand \\\} is has the first two slashes
    // matched by the even number of slashes matcher and the remaining \} matched by the "exact combination \}" matcher
    pattern = Pattern.compile(
      "(?<!\\\\)(?:\\\\\\\\)*\\{((?:[^\\\\\\}]|(?:(?<!\\\\)(?:\\\\\\\\)+)|(?:\\\\\\})+)+)\\}"
    );
  }

  @Override
  public Node generateValue(Row input) {
    Matcher regexMatcher = pattern.matcher(template);
    StringBuffer resultString = new StringBuffer();
    while (regexMatcher.find()) {
      regexMatcher.appendReplacement(resultString, "" + input.get(regexMatcher.group(1)) );
    }
    regexMatcher.appendTail(resultString);

    switch (termType) {
      case IRI:
        return NodeFactory.createURI(resultString.toString());
      case BlankNode:
        return NodeFactory.createBlankNode(resultString.toString());
      case Literal:
        return NodeFactory.createLiteral(resultString.toString());
      default:
        throw new UnsupportedOperationException("Not all items in the Enumerable where handled");
    }

  }

  @Override
  public String toString() {
    Matcher regexMatcher = pattern.matcher(template);
    StringBuffer resultString = new StringBuffer();
    while (regexMatcher.find()) {
      regexMatcher.appendReplacement(resultString, "«" + regexMatcher.group(1) + "»" );
    }
    regexMatcher.appendTail(resultString);

    return String.format("      Template: %s (%s)\n",
      resultString,
      this.termType
    );
  }

}
