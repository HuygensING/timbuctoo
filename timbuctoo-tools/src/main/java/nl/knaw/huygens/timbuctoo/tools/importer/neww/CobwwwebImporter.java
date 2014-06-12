package nl.knaw.huygens.timbuctoo.tools.importer.neww;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.Visitor;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.XRepository;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class CobwwwebImporter extends DefaultImporter {

  public CobwwwebImporter(XRepository repository) {
    super(repository);
  }

  protected String getResource(String... parts) throws Exception {
    String url = Joiner.on("/").join(parts);
    openSource(url);
    Client client = Client.create();
    WebResource webResource = client.resource(url);
    ClientResponse response = webResource.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
    if (response.getClientResponseStatus() != ClientResponse.Status.OK) {
      throw new IOException("Failed to retrieve " + url);
    }
    return response.getEntity(String.class);
  }

  protected List<String> parseIdResource(String xml, String idElementName) {
    IdContext context = new IdContext();
    parseXml(xml, new IdVisitor(context, idElementName));
    return context.ids;
  }

  protected void parseXml(String xml, Visitor visitor) {
    nl.knaw.huygens.tei.Document.createFromXml(xml).accept(visitor);
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

}
