package nl.knaw.huygens.timbuctoo.tools.importer.neww;

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.XRepository;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;


public class CobwwwebImporter extends DefaultImporter {

  public CobwwwebImporter(XRepository repository) {
    super(repository);
  }

  protected String getResource(String... parts) throws Exception {
    String url = Joiner.on("/").join(parts);
    log("-- %s%n", url);
    ClientResource resource = new ClientResource(url);
    return resource.get(MediaType.APPLICATION_XML).getText();
  }

  protected List<String> parseIdResource(String xml, String idElementName) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    IdContext context = new IdContext();
    document.accept(new IdVisitor(context, idElementName));
    return context.ids;
  }

  private class IdContext extends XmlContext {
    public final List<String> ids = Lists.newArrayList();

    public void addId(String id) {
      // somewhat inefficent, but we want to preserve ordering
      if (ids.contains(id)) {
        log("## Duplicate entry %s%n", id);
      } else {
        ids.add(id);
      }
    }
  }

  private class IdVisitor extends DelegatingVisitor<IdContext> {
    public IdVisitor(IdContext context, String idElementName) {
      super(context);
      addElementHandler(new IdHandler(), idElementName);
    }
  }

  private class IdHandler extends DefaultElementHandler<IdContext> {
    @Override
    public Traversal enterElement(Element element, IdContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, IdContext context) {
      String id = context.closeLayer().trim();
      context.addId(id);
      return Traversal.NEXT;
    }
  }

  // ---------------------------------------------------------------------------

  /**
   * TEI element handler that captures and filters the content of the element.
   */
  protected static abstract class CaptureHandler<T extends XmlContext> implements ElementHandler<T> {

    @Override
    public Traversal enterElement(Element element, T context) {
      context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, T context) {
      String text = context.closeLayer().trim();
      if (!text.isEmpty()) {
        handleContent(filterField(text), context);
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

    protected abstract void handleContent(String text, T context);

  }

}
