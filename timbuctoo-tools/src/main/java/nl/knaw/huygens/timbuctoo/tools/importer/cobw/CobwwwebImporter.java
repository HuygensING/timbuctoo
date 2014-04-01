package nl.knaw.huygens.timbuctoo.tools.importer.cobw;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.cobw.COBWDocument;
import nl.knaw.huygens.timbuctoo.model.cobw.COBWPerson;
import nl.knaw.huygens.timbuctoo.model.cobw.COBWRelation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Importer for Norwegian data.
 */
public class CobwwwebImporter extends DefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(CobwwwebImporter.class);

  public static void main(String[] args) throws Exception {

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = null;

    try {
      long start = System.currentTimeMillis();

      storageManager = injector.getInstance(StorageManager.class);

      TypeRegistry registry = injector.getInstance(TypeRegistry.class);
      RelationManager relationManager = new RelationManager(registry, storageManager);

      CobwwwebImporter importer = new CobwwwebImporter(registry, storageManager, relationManager, null);
      importer.importAll();

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

    } catch (Exception e) {
      // for debugging
      e.printStackTrace();
    } finally {
      // Close resources
      if (storageManager != null) {
        storageManager.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  // Base URL for import
  private static final String URL = "https://www2.hf.uio.no/tjenester/bibliografi/Robinsonades";

  private final Change change;
  private final RelationManager relationManager;
  /** Reference to relation types. */
  private final Map<String, Reference> relationTypes = Maps.newHashMap();
  /** References of stored primitive entities */
  private final Map<String, Reference> references = Maps.newHashMap();

  public CobwwwebImporter(TypeRegistry registry, StorageManager storageManager, RelationManager relationManager, IndexManager indexManager) {
    super(registry, storageManager, indexManager);
    change = new Change("importer", "neww");
    this.relationManager = relationManager;
  }

  public void importAll() throws Exception {
    setup(relationManager);
    setupRelationDefs();

    String xml = "";

    if ("".isEmpty()) {
      xml = getResource(URL, "persons");
      List<String> personIds = parseIdResource(xml, "personId");
      System.out.printf("Retrieved %d id's.%n", personIds.size());

      for (String id : personIds) {
        xml = getResource(URL, "person", id);
        COBWPerson entity = parsePersonResource(xml, id);
        String storedId = addDomainEntity(COBWPerson.class, entity, change);
        storeReference(id, COBWPerson.class, storedId);
      }
    }

    if ("".isEmpty()) {
      xml = getResource(URL, "documents");
      List<String> documentIds = parseIdResource(xml, "documentId");
      System.out.printf("Retrieved %d id's.%n", documentIds.size());

      for (String id : documentIds) {
        xml = getResource(URL, "document", id);
        COBWDocument entity = parseDocumentResource(xml, id);
        String storedId = addDomainEntity(COBWDocument.class, entity, change);
        storeReference(id, COBWDocument.class, storedId);
      }
    }

    if ("".isEmpty()) {
      xml = getResource(URL, "relations");
      List<String> relationIds = parseIdResource(xml, "relationId");
      System.out.printf("Retrieved %d id's.%n", relationIds.size());

      for (String id : relationIds) {
        xml = getResource(URL, "relation", id);
        parseRelationResource(xml, id);
      }
    }
  }

  private void setupRelationDefs() {
    StorageIterator<RelationType> iterator = storageManager.getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      relationTypes.put(type.getRegularName(), new Reference(RelationType.class, type.getId()));
    }
  }

  private String getResource(String... parts) throws Exception {
    String url = Joiner.on("/").join(parts);
    System.out.printf("URL: %s%n", url);
    ClientResource resource = new ClientResource(url);
    Representation representation = resource.get(MediaType.APPLICATION_XML);
    return representation.getText();
  }

  private Reference storeReference(String key, Class<? extends DomainEntity> type, String id) {
    Reference reference = newDomainEntityReference(type, id);
    if (references.put(key, reference) != null) {
      LOG.error("Duplicate key '{}'", key);
      System.exit(-1);
    }
    return reference;
  }

  /**
   * TEI element handler that captures and filters the content of the element.
   */
  private static abstract class CaptureHandler<T extends XmlContext> implements ElementHandler<T> {
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

  // ---------------------------------------------------------------------------

  private List<String> parseIdResource(String xml, String idElementName) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    IdContext context = new IdContext();
    document.accept(new IdVisitor(context, idElementName));
    return context.ids;
  }

  private static class IdContext extends XmlContext {
    public final List<String> ids = Lists.newArrayList();

    public void addId(String id) {
      // somewhat inefficent, but we want to preserve ordering
      if (ids.contains(id)) {
        System.err.printf("## Duplicate entry %s%n", id);
      } else {
        ids.add(id);
      }
    }
  }

  private static class IdVisitor extends DelegatingVisitor<IdContext> {
    public IdVisitor(IdContext context, String idElementName) {
      super(context);
      addElementHandler(new IdHandler(), idElementName);
    }
  }

  private static class IdHandler extends DefaultElementHandler<IdContext> {
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

  private COBWPerson parsePersonResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    PersonContext context = new PersonContext(id);
    document.accept(new PersonVisitor(context));
    return context.person;
  }

  private static class PersonContext extends XmlContext {
    public PersonContext(String id) {
      this.id = id;
    }

    public String id;
    public COBWPerson person = new COBWPerson();

    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private static class PersonVisitor extends DelegatingVisitor<PersonContext> {
    public PersonVisitor(PersonContext context) {
      super(context);
      setDefaultElementHandler(new DefaultPersonHandler());
      addElementHandler(new PersonIdHandler(), "personId");
      addElementHandler(new PersonTypeHandler(), "type");
      addElementHandler(new GenderHandler(), "gender");
      addElementHandler(new DateOfBirthHandler(), "dateOfBirth");
      addElementHandler(new DateOfDeathHandler(), "dateOfDeath");
      addElementHandler(new NameHandler(), "name");
      addElementHandler(new PersonLanguageHandler(), "language");
      addElementHandler(new PersonLinkHandler(), "Reference");
      addElementHandler(new PersonNotesHandler(), "notes");
    }
  }

  private static class DefaultPersonHandler extends DefaultElementHandler<PersonContext> {
    private final Set<String> ignoredNames = Sets.newHashSet("person", "names", "languages");

    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name)) {
        context.error("Unexpected element: %s", name);
      }
      return Traversal.NEXT;
    }
  }

  private static class PersonIdHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private static class PersonTypeHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (text.equalsIgnoreCase(Person.Type.ARCHETYPE)) {
        context.person.addType(Person.Type.ARCHETYPE);
      } else if (text.equalsIgnoreCase(Person.Type.AUTHOR)) {
        context.person.addType(Person.Type.AUTHOR);
      } else if (text.equalsIgnoreCase(Person.Type.PSEUDONYM)) {
        context.person.addType(Person.Type.PSEUDONYM);
      } else {
        context.error("Unknown type: %s", text);
      }
    }
  }

  private static class GenderHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (text.equals("1")) {
        context.person.setGender(Person.Gender.MALE);
      } else if (text.equals("2")) {
        context.person.setGender(Person.Gender.FEMALE);
      } else if (text.equals("9")) {
        context.person.setGender(Person.Gender.NOT_APPLICABLE);
      } else {
        context.person.setGender(Person.Gender.UNKNOWN);
      }
    }
  }

  private static class DateOfBirthHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setBirthDate(datable);
    }
  }

  private static class DateOfDeathHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setDeathDate(datable);
    }
  }

  private static class NameHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.addTempName(text);
    }
  }

  private static class PersonLinkHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      Link link = new Link(text, "NEWW");
      context.person.addLink(link);
    }
  }

  private static class PersonNotesHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.setNotes(text);
    }
  }

  private static class PersonLanguageHandler implements ElementHandler<PersonContext> {
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, PersonContext context) {
      String text = context.closeLayer().trim();
      if (!text.isEmpty()) {
        if (!element.hasParentWithName("languages")) {
          context.error("Unexpected value in element 'language': %s", text);
        } else if (context.person.getNationalities().contains(text)) {
          context.error("Duplicate value in element 'languages/language': %s", text);
        } else {
          context.person.addNationality(text);
        }
      }
      return Traversal.NEXT;
    }
  }

  // ---------------------------------------------------------------------------

  private COBWDocument parseDocumentResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    DocumentContext context = new DocumentContext(id);
    document.accept(new DocumentVisitor(context));
    return context.document;
  }

  private static class DocumentContext extends XmlContext {
    public DocumentContext(String id) {
      this.id = id;
    }

    public String id;
    public COBWDocument document = new COBWDocument();

    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private static class DocumentVisitor extends DelegatingVisitor<DocumentContext> {
    public DocumentVisitor(DocumentContext context) {
      super(context);
      setDefaultElementHandler(new DefaultDocumentHandler());
      addElementHandler(new DocumentIdHandler(), "documentId");
      addElementHandler(new DocumentTypeHandler(), "type");
      addElementHandler(new DocumentTitleHandler(), "title");
      addElementHandler(new DocumentDescriptionHandler(), "description");
      addElementHandler(new DocumentDateHandler(), "date");
      addElementHandler(new DocumentLanguageHandler(), "language");
      addElementHandler(new DocumentLinkHandler(), "Reference");
      addElementHandler(new DocumentNotesHandler(), "notes");
    }
  }

  private static class DefaultDocumentHandler extends DefaultElementHandler<DocumentContext> {
    private final Set<String> ignoredNames = Sets.newHashSet("document", "creators", "languages");

    @Override
    public Traversal enterElement(Element element, DocumentContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name)) {
        context.error("Unexpected element: %s", name);
      }
      return Traversal.NEXT;
    }
  }

  private static class DocumentIdHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private static class DocumentTypeHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      if (text.equalsIgnoreCase(Document.DocumentType.WORK.name())) {
        context.document.setDocumentType(Document.DocumentType.WORK);
      } else {
        context.error("Unknown type: %s", text);
      }
    }
  }

  private static class DocumentTitleHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.setTitle(text);
    }
  }

  private static class DocumentDescriptionHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.setDescription(text);
    }
  }

  private static class DocumentDateHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      Datable datable = new Datable(text);
      context.document.setDate(datable);
    }
  }

  private static class DocumentNotesHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.setNotes(text);
    }
  }

  private static class DocumentLanguageHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.addTempLanguage(text);
    }
  }

  private static class DocumentLinkHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      Link link = new Link(text, "NEWW");
      context.document.addLink(link);
    }
  }

  // ---------------------------------------------------------------------------

  private void parseRelationResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    RelationContext context = new RelationContext(id);
    document.accept(new RelationVisitor(context));
    Reference typeRef = relationTypes.get(context.relationTypeName);
    Reference sourceRef = references.get(context.sourceId);
    Reference targetRef = references.get(context.targetId);
    if (typeRef != null && sourceRef != null && targetRef != null) {
      relationManager.storeRelation(COBWRelation.class, sourceRef, typeRef, targetRef, change);
    } else {
      System.err.printf("Error in %s: %s --> %s%n", context.relationTypeName, context.sourceId, context.targetId);
    }
  }

  private static class RelationContext extends XmlContext {
    public RelationContext(String id) {
      this.id = id;
    }

    public String id;
    public String relationTypeName = "";
    public String sourceId = "";
    public String targetId = "";

    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private static class RelationVisitor extends DelegatingVisitor<RelationContext> {
    public RelationVisitor(RelationContext context) {
      super(context);
      setDefaultElementHandler(new DefaultRelationHandler());
      addElementHandler(new RelationIdHandler(), "relationId");
      addElementHandler(new RelationLinkHandler(), "Reference");
      addElementHandler(new RelationTypeHandler(), "type");
      addElementHandler(new RelationActiveHandler(), "active");
      addElementHandler(new RelationPassiveHandler(), "passive");
    }
  }

  private static class DefaultRelationHandler extends DefaultElementHandler<RelationContext> {
    private final Set<String> ignoredNames = Sets.newHashSet("relation");

    @Override
    public Traversal enterElement(Element element, RelationContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name)) {
        context.error("Unexpected element: %s", name);
      }
      return Traversal.NEXT;
    }
  }

  private static class RelationIdHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private static class RelationLinkHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.error("Unexpected reference: %s", text);
    }
  }

  private static class RelationTypeHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      if (text.equalsIgnoreCase("translation of")) {
        context.relationTypeName = "hasTranslation";
      } else if (text.equalsIgnoreCase("edition of")) {
        context.relationTypeName = "hasEdition";
      } else if (text.equalsIgnoreCase("written by")) {
        context.relationTypeName = "isCreatedBy";
      } else if (text.equalsIgnoreCase("pseudonym")) {
        context.relationTypeName = "isPseudonymOf";
      } else {
        context.error("Unexpected relation type: '%s'", text);
        System.exit(0);
      }
    }
  }

  private static class RelationActiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.sourceId = text;
    }
  }

  private static class RelationPassiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.targetId = text;
    }
  }

}
