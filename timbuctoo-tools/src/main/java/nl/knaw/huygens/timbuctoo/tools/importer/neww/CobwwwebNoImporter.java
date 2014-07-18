package nl.knaw.huygens.timbuctoo.tools.importer.neww;

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
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.cwno.CWNODocument;
import nl.knaw.huygens.timbuctoo.model.cwno.CWNOPerson;
import nl.knaw.huygens.timbuctoo.model.cwno.CWNORelation;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.model.neww.WWRelation;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.CaptureHandler;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * Importer for Norwegian COBWWWEB data.
 * Assumes the presence of New European Women Writers data,
 * because COBWWWEB records are linked to that data.
 */
public class CobwwwebNoImporter extends CobwwwebImporter {

  private static final Logger LOG = LoggerFactory.getLogger(CobwwwebNoImporter.class);

  // Base URL for import
  private static final String URL = "https://www2.hf.uio.no/tjenester/bibliografi/Robinsonades";

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    CobwwwebNoImporter importer = null;
    try {
      Injector injector = ToolsInjectionModule.createInjector();
      Repository repository = injector.getInstance(Repository.class);
      IndexManager indexManager = injector.getInstance(IndexManager.class);

      importer = new CobwwwebNoImporter(repository, indexManager);
      importer.importAll();
    } finally {
      if (importer != null) {
        importer.close();
      }
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // -------------------------------------------------------------------

  /** References of stored primitive entities */
  private final Map<String, Reference> references = Maps.newHashMap();

  public CobwwwebNoImporter(Repository repository, IndexManager indexManager) {
    super(repository, indexManager, "cwno");
  }

  public void importAll() throws Exception {
    try {
      openImportLog("cobwwweb-no-log.txt");
      importRelationTypes();
      setupRelationTypeDefs();

      printBoxedText("Persons");
      importPersons();

      printBoxedText("Documents");
      importDocuments();

      printBoxedText("Relations");
      importRelations();

      displayStatus();
    } finally {
      displayErrorSummary();
      closeImportLog();
    }
  }

  private Reference storeReference(String key, Class<? extends DomainEntity> type, String id) {
    Reference reference = new Reference(TypeRegistry.toBaseDomainEntity(type), id);
    if (references.put(key, reference) != null) {
      log("Duplicate key '%s'%n", key);
      System.exit(-1);
    }
    return reference;
  }

  // --- persons ---------------------------------------------------------------

  private void importPersons() throws Exception {
    String xml = getResource(URL, "persons");
    List<String> personIds = parseIdResource(xml, "personId");
    log("Retrieved %d id's.%n", personIds.size());

    Progress progress = new Progress();
    for (String id : personIds) {
      progress.step();
      xml = getResource(URL, "person", id);
      CWNOPerson entity = parsePersonResource(xml, id);
      String storedId = updateExistingPerson(entity);
      if (storedId == null) {
        storedId = createNewPerson(entity);
      }
      storeReference(id, CWNOPerson.class, storedId);
      indexManager.addEntity(CWNOPerson.class, storedId);
      indexManager.updateEntity(WWPerson.class, storedId);
    }
    progress.done();
  }

  private CWNOPerson parsePersonResource(String xml, String id) {
    PersonContext context = new PersonContext(id);
    parseXml(xml, new PersonVisitor(context));
    return context.person;
  }

  // Retrieve existing WWPerson, add CWNOPerson variation
  private String updateExistingPerson(CWNOPerson entity) {
    String storedId = null;
    if (!Strings.isNullOrEmpty(entity.tempNewwId)) {
      WWPerson person = repository.findEntity(WWPerson.class, "tempOldId", entity.tempNewwId);
      if (person != null) {
        storedId = person.getId();
        entity.setId(storedId);
        entity.setRev(person.getRev());
        updateProjectDomainEntity(CWNOPerson.class, entity);
        log("Updated person with id %s%n", storedId);
      }
    }
    return storedId;
  }

  // Save as CWNOPerson, add WWPerson variation
  private String createNewPerson(CWNOPerson entity) {
    String storedId = addDomainEntity(CWNOPerson.class, entity);
    WWPerson person = repository.getEntity(WWPerson.class, storedId);
    updateProjectDomainEntity(WWPerson.class, person);
    return storedId;
  }

  private class PersonContext extends XmlContext {
    public String id;
    public CWNOPerson person = new CWNOPerson();

    public PersonContext(String id) {
      this.id = id;
    }

    public void error(String format, Object... args) {
      log("[%s] %s%n", id, String.format(format, args));
    }
  }

  private class PersonVisitor extends DelegatingVisitor<PersonContext> {
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

  private class DefaultPersonHandler extends DefaultElementHandler<PersonContext> {
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

  private class PersonIdHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class PersonTypeHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
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

  private class GenderHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
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

  private class DateOfBirthHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      Datable datable = new Datable(text);
      context.person.setBirthDate(datable);
    }
  }

  private class DateOfDeathHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      Datable datable = new Datable(text);
      context.person.setDeathDate(datable);
    }
  }

  private class NameHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      context.person.tempNames.add(text);
    }
  }

  private class PersonLinkHandler extends CaptureHandler<PersonContext> {
    private static final String NEWW_URL = "http://neww.huygens.knaw.nl/authors/show/";

    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      if (text.startsWith(NEWW_URL)) {
        log("Reference to NEWW: %s%n", text);
        context.person.tempNewwId = text.substring(NEWW_URL.length());
      } else {
        context.person.addLink(new Link(text));
      }
    }
  }

  private class PersonNotesHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(Element element, PersonContext context, String text) {
      context.person.setNotes(text);
    }
  }

  private class PersonLanguageHandler implements ElementHandler<PersonContext> {
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

  // --- documents -------------------------------------------------------------

  private void importDocuments() throws Exception {
    String xml = getResource(URL, "documents");
    List<String> documentIds = parseIdResource(xml, "documentId");
    log("Retrieved %d id's.%n", documentIds.size());

    Progress progress = new Progress();
    for (String id : documentIds) {
      progress.step();
      xml = getResource(URL, "document", id);
      CWNODocument entity = parseDocumentResource(xml, id);
      String storedId = updateExistingDocument(entity);
      if (storedId == null) {
        storedId = createNewDocument(entity);
      }
      storeReference(id, CWNODocument.class, storedId);
      indexManager.addEntity(CWNODocument.class, storedId);
      indexManager.updateEntity(WWDocument.class, storedId);
    }
    progress.done();
  }

  private CWNODocument parseDocumentResource(String xml, String id) {
    DocumentContext context = new DocumentContext(id);
    parseXml(xml, new DocumentVisitor(context));
    return context.document;
  }

  // Retrieve existing WWDocument, add CWNODocument variation
  private String updateExistingDocument(CWNODocument entity) {
    String storedId = null;
    if (!Strings.isNullOrEmpty(entity.tempNewwId)) {
      WWDocument document = repository.findEntity(WWDocument.class, "tempOldId", entity.tempNewwId);
      if (document != null) {
        storedId = document.getId();
        entity.setId(storedId);
        entity.setRev(document.getRev());
        updateProjectDomainEntity(CWNODocument.class, entity);
        log("Updated document with id %s%n", storedId);
      }
    }
    return storedId;
  }

  // Save as CWNODocument, add WWDocument variation
  private String createNewDocument(CWNODocument entity) {
    String storedId = addDomainEntity(CWNODocument.class, entity);
    WWDocument document = repository.getEntity(WWDocument.class, storedId);
    updateProjectDomainEntity(WWDocument.class, document);
    return storedId;
  }

  private class DocumentContext extends XmlContext {
    public String id;
    public CWNODocument document = new CWNODocument();

    public DocumentContext(String id) {
      this.id = id;
    }

    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private class DocumentVisitor extends DelegatingVisitor<DocumentContext> {
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

  private class DefaultDocumentHandler extends DefaultElementHandler<DocumentContext> {
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

  private class DocumentIdHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class DocumentTypeHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      if (text.equalsIgnoreCase(Document.DocumentType.WORK.name())) {
        context.document.setDocumentType(Document.DocumentType.WORK);
      } else {
        context.error("Unknown type: %s", text);
      }
    }
  }

  private class DocumentTitleHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      context.document.setTitle(text);
    }
  }

  private class DocumentDescriptionHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      context.document.setDescription(text);
    }
  }

  private class DocumentDateHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      Datable datable = new Datable(text);
      context.document.setDate(datable);
    }
  }

  private class DocumentNotesHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      context.document.setNotes(text);
    }
  }

  private class DocumentLanguageHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      context.document.tempLanguages.add(text);
    }
  }

  private class DocumentLinkHandler extends CaptureHandler<DocumentContext> {
    private static final String NEWW_URL = "http://neww.huygens.knaw.nl/works/show/";

    @Override
    public void handleContent(Element element, DocumentContext context, String text) {
      if (text.startsWith(NEWW_URL)) {
        log("Reference to NEWW: %s%n", text);
        context.document.tempNewwId = text.substring(NEWW_URL.length());
      } else {
        context.document.addLink(new Link(text));
      }
    }
  }

  // --- relations -------------------------------------------------------------

  private void importRelations() throws Exception {
    String xml = getResource(URL, "relations");
    List<String> relationIds = parseIdResource(xml, "relationId");
    log("Retrieved %d id's.%n", relationIds.size());

    Progress progress = new Progress();
    for (String id : relationIds) {
      progress.step();
      xml = getResource(URL, "relation", id);
      String storedId = parseRelationResource(xml, id);
      if (storedId != null) {
        indexManager.addEntity(CWNORelation.class, storedId);
        indexManager.updateEntity(WWRelation.class, storedId);
      }
    }

    progress.done();
  }

  private String parseRelationResource(String xml, String id) {
    RelationContext context = new RelationContext(id);
    parseXml(xml, new RelationVisitor(context));
    Reference typeRef = relationTypes.get(context.relationTypeName);
    Reference sourceRef = references.get(context.sourceId);
    Reference targetRef = references.get(context.targetId);
    String storedId = null;
    if (typeRef != null && sourceRef != null && targetRef != null) {
      storedId = addRelation(CWNORelation.class, typeRef, sourceRef, targetRef, change, xml);
    } else {
      System.err.printf("Error in %s: %s --> %s%n", context.relationTypeName, context.sourceId, context.targetId);
    }

    return storedId;
  }

  private class RelationContext extends XmlContext {
    public String id;
    public String relationTypeName = "";
    public String sourceId = "";
    public String targetId = "";

    public RelationContext(String id) {
      this.id = id;
    }

    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private class RelationVisitor extends DelegatingVisitor<RelationContext> {
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

  private class DefaultRelationHandler extends DefaultElementHandler<RelationContext> {
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

  private class RelationIdHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(Element element, RelationContext context, String text) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class RelationLinkHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(Element element, RelationContext context, String text) {
      context.error("Unexpected reference: %s", text);
    }
  }

  private class RelationTypeHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(Element element, RelationContext context, String text) {
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

  private class RelationActiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(Element element, RelationContext context, String text) {
      context.sourceId = text;
    }
  }

  private class RelationPassiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(Element element, RelationContext context, String text) {
      context.targetId = text;
    }
  }

}
