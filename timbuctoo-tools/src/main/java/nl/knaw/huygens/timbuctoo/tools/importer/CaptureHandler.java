package nl.knaw.huygens.timbuctoo.tools.importer;

import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;

/**
 * TEI element handler that captures and filters the content of the element.
 */
public abstract class CaptureHandler<T extends XmlContext> implements ElementHandler<T> {

  @Override
  public Traversal enterElement(Element element, T context) {
    context.openLayer();
    return Traversal.NEXT;
  }

  @Override
  public Traversal leaveElement(Element element, T context) {
    String text = context.closeLayer().trim();
    if (!text.isEmpty()) {
      handleContent(element, context, filterField(text));
    }
    return Traversal.NEXT;
  }

  private String filterField(String text) {
    if (text.contains("\\")) {
      text = text.replaceAll("\\\\r", " ");
      text = text.replaceAll("\\\\n", " ");
    }
    text = text.replaceAll("[\\s\\u00A0]+", " ");
    return text.trim();
  }

  protected abstract void handleContent(Element element, T context, String text);

}
