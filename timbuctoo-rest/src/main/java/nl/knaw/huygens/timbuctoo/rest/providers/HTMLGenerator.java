package nl.knaw.huygens.timbuctoo.rest.providers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;

/**
 * Generates human-readable HTML.
 * 
 * {@code HTMLGenerator} does not know about the proper structure of entities.
 * For example, it does not know which variations can be present.
 */
public class HTMLGenerator extends JsonGeneratorDelegate {

  public HTMLGenerator(JsonGenerator generator) {
    super(generator);
  }

  private final static String NULL_STR = "none";
  private final static String TRUE_STR = "yes";
  private final static String FALSE_STR = "no";

  private enum NestingLevel {
    ARRAY, OBJECT
  }

  private Stack<NestingLevel> levels = new Stack<NestingLevel>();

  @Override
  public void writeFieldName(String fieldName) throws IOException, JsonGenerationException {
    this.writeRaw("<tr><th>" + camelCaseUnescape(fieldName) + "</th>");
  }

  @Override
  public void writeFieldName(SerializableString fieldName) throws IOException, JsonGenerationException {
    this.writeRaw("<tr><th>" + camelCaseUnescape(fieldName.getValue()) + "</th>");
  }

  // --- Public API, write methods, structural -------------------------

  @Override
  public void writeStartArray() throws IOException, JsonGenerationException {
    if (NestingLevel.OBJECT.equals(levels.peek())) {
      writeRaw("<td>");
    } else {
      writeRaw("<table><tr><td>");
    }
    levels.push(NestingLevel.ARRAY);
  }

  @Override
  public void writeEndArray() throws IOException, JsonGenerationException {
    levels.pop();
    if (NestingLevel.OBJECT.equals(levels.peek())) {
      writeRaw("</td></tr>\n");
    } else {
      writeRaw("</td></tr></table>");
    }
  }

  @Override
  public void writeStartObject() throws IOException, JsonGenerationException {
    if (levels.isEmpty()) {
      writeRaw("<table>\n");
    } else if (levels.peek().equals(NestingLevel.OBJECT)) {
      writeRaw("<td><table>\n");
    } else if (levels.peek().equals(NestingLevel.ARRAY)) {
      writeRaw("<table>\n");
    }
    levels.push(NestingLevel.OBJECT);
  }

  @Override
  public void writeEndObject() throws IOException, JsonGenerationException {
    levels.pop();
    if (levels.isEmpty()) {
      writeRaw("</table>\n");
    } else if (levels.peek().equals(NestingLevel.OBJECT)) {
      writeRaw("</table></td></tr>\n");
    } else if (levels.peek().equals(NestingLevel.ARRAY)) {
      writeRaw("</table>\n");
    }
  }

  // --- Public API, write methods, text/String values -----------------

  @Override
  public void writeString(String text) throws IOException, JsonGenerationException {
    writeFieldValue(htmlEscape(text));
  }

  @Override
  public void writeString(char[] text, int offset, int len) throws IOException, JsonGenerationException {
    writeFieldValue(htmlEscape(new String(text, offset, len)));
  }

  @Override
  public void writeString(SerializableString text) throws IOException, JsonGenerationException {
    writeFieldValue(htmlEscape(text.getValue()));
  }

  @Override
  public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
    writeFieldValue(htmlEscape(jsonUnescape(text, offset, length)));
  }

  @Override
  public void writeUTF8String(byte[] text, int offset, int length) throws IOException, JsonGenerationException {
    writeFieldValue(htmlEscape(text, offset, length));
  }

  // --- Public API, write methods, other value types ------------------

  @Override
  public void writeNumber(int v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(long v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(BigInteger v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(double v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(float v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(BigDecimal v) throws IOException, JsonGenerationException {
    writeNumberValue(v);
  }

  @Override
  public void writeNumber(String encodedValue) throws IOException, JsonGenerationException, UnsupportedOperationException {
    throw new UnsupportedOperationException("WTF?");
  }

  @Override
  public void writeBoolean(boolean state) throws IOException, JsonGenerationException {
    writeFieldValPre();
    writeRaw(state ? TRUE_STR : FALSE_STR);
    writeFieldValPost();
  }

  @Override
  public void writeNull() throws IOException, JsonGenerationException {
    writeFieldValPre();
    writeRaw(NULL_STR);
    writeFieldValPost();
  }

  private void writeNumberValue(long v) throws JsonGenerationException, IOException {
    writeFieldValPre();
    delegate.writeNumber(v);
    writeFieldValPost();
  }

  private void writeNumberValue(double v) throws JsonGenerationException, IOException {
    writeFieldValPre();
    delegate.writeNumber(v);
    writeFieldValPost();
  }

  private void writeNumberValue(BigDecimal v) throws JsonGenerationException, IOException {
    writeFieldValPre();
    delegate.writeNumber(v);
    writeFieldValPost();
  }

  private void writeNumberValue(BigInteger v) throws JsonGenerationException, IOException {
    writeFieldValPre();
    delegate.writeNumber(v);
    writeFieldValPost();
  }

  private void writeFieldValue(String text) throws JsonGenerationException, IOException {
    writeFieldValPre();
    writeRaw(text);
    writeFieldValPost();
  }

  private void writeFieldValPre() throws JsonGenerationException, IOException {
    if (NestingLevel.OBJECT.equals(levels.peek())) {
      writeRaw("<td>");
    }
  }

  private void writeFieldValPost() throws JsonGenerationException, IOException {
    if (NestingLevel.OBJECT.equals(levels.peek())) {
      writeRaw("</td></tr>\n");
    } else {
      writeRaw(";<br>\n");
    }
  }

  private String camelCaseUnescape(String fieldName) {
    // See: http://stackoverflow.com/a/2560017/713326
    return StringUtils.capitalize(fieldName.replaceAll("[_^.-@!]", " ").trim()).replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"),
        " ");
  }

  private String htmlEscape(byte[] text, int offset, int length) {
    return htmlEscape(new String(text, offset, length));
  }

  private String htmlEscape(String string) {
    return StringEscapeUtils.escapeHtml(string);
  }

  private String jsonUnescape(byte[] text, int offset, int length) {
    return StringEscapeUtils.unescapeJavaScript(new String(text, offset, length));
  }

}
