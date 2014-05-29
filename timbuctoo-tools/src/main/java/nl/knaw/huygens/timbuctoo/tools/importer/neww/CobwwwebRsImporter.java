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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.XRepository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Collective;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.cwrs.CWRSCollective;
import nl.knaw.huygens.timbuctoo.model.cwrs.CWRSDocument;
import nl.knaw.huygens.timbuctoo.model.cwrs.CWRSPerson;
import nl.knaw.huygens.timbuctoo.model.cwrs.CWRSRelation;
import nl.knaw.huygens.timbuctoo.model.neww.WWCollective;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.util.DisplayTokenHandler;
import nl.knaw.huygens.timbuctoo.util.Text;
import nl.knaw.huygens.timbuctoo.util.Tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Importer for Serbian COBWWWEB data.
 * Assumes the presence of New European Women Writers data,
 * because COBWWWEB records are linked to that data.
 * 
 * Place Mappings:
 * "Serbia" --> "co:srb"
 * "Hungary" --> "co:hun"
 * "Republic of Ragusa" --> "se:dubrovnik.hrv" (1358-1808)
 */
public class CobwwwebRsImporter extends CobwwwebImporter {

  private static final Logger LOG = LoggerFactory.getLogger(CobwwwebRsImporter.class);

  // Base URL for import
  private static final String URL = "http://ws-knjizenstvo.etf.rs/Knjizenstvo/Cobwwweb";

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    // Handle commandline arguments
    String directory = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/neww/";

    XRepository repository = null;
    try {
      repository = ToolsInjectionModule.createRepositoryInstance();
      new CobwwwebRsImporter(repository, directory).importAll();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (repository != null) {
        repository.close();
      }
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // -------------------------------------------------------------------

  private final Change change;
  /** References of stored primitive entities. */
  private final Map<String, Reference> references = Maps.newHashMap();
  /** Used languages. */
  private LoadingCache<String, Language> languages;
  private LocationConcordance locations;

  public CobwwwebRsImporter(XRepository repository, String inputDirName) throws Exception {
    super(repository);
    change = new Change("importer", "cwrs");
    setupLanguageCache();

    File inputDir = new File(inputDirName);
    if (inputDir.isDirectory()) {
      System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
    } else {
      System.out.printf("%n.. Not a directory: %s%n", inputDir.getAbsolutePath());
    }
    locations = new LocationConcordance(new File(inputDir, "neww-locations.txt"));
  }

  public void importAll() throws Exception {
    try {
      openImportLog("cobwwweb-rs-log.txt");
      importRelationTypes();
      setupRelationTypeDefs();
      importCollectives();
      if ("x".isEmpty()) {
      importPersons();
      importDocuments();
      importRelations();
      }
      displayStatus();
    } finally {
      displayErrorSummary();
      closeImportLog();
    }
  }

  // ---------------------------------------------------------------------------

  private void setupLanguageCache() {
    languages = CacheBuilder.newBuilder().build(new CacheLoader<String, Language>() {
      @Override
      public Language load(String code) throws IOException {
        Language language = repository.getLanguageByCode(Language.class, code);
        if (language == null) {
          throw new IOException(code);
        }
        return language;
      }
    });
  }

  private Language getLanguage(String code) {
    try {
      return languages.get(code);
    } catch (ExecutionException e) {
      LOG.error("No language with code {}", code);
      return null;
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

  // --- collectives -----------------------------------------------------------

  private void importCollectives() throws Exception {
    String xml = getResource(URL, "cooperations");
    List<String> ids = parseIdResource(xml, "cooperationId");
    log("Retrieved %d id's.%n", ids.size());

    for (String id : ids) {
      xml = getResource(id);
      CWRSCollective entity = parseCollectiveResource(xml, id);
      if (accept(entity)) {
        String storedId = createNewCollective(entity);

        handleCollectiveLocation(entity);

        indexManager.addEntity(CWRSCollective.class, storedId);
        indexManager.updateEntity(WWCollective.class, storedId);
      }
    }
    System.out.println("Names");
    nameTokens.handleSortedByText(new DisplayTokenHandler());
  }

  private CWRSCollective parseCollectiveResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    CollectiveContext context = new CollectiveContext(xml, id);
    document.accept(new CollectiveVisitor(context));
    return context.entity;
  }

  private boolean accept(CWRSCollective entity) {
    return true;
  }

  private String createNewCollective(CWRSCollective entity) {
    String storedId = addDomainEntity(CWRSCollective.class, entity, change);
    WWCollective collective = repository.getEntity(WWCollective.class, storedId);
    updateProjectDomainEntity(WWCollective.class, collective, change);
    return storedId;
  }

  private void handleCollectiveLocation(CWRSCollective entity) {
    String name = entity.tempLocation;
    String urn = locations.lookup(name);
    if (urn != null) {
      Location location = repository.findEntity(Location.class, Location.URN, urn);
      if (location != null) {
        Reference typeRef = getRelationTypeRef("hasLocation", true);
        Reference sourceRef = new Reference(Collective.class, entity.getId());
        Reference targetRef = new Reference(Location.class, location.getId());
        addRelation(CWRSRelation.class, typeRef, sourceRef, targetRef, change, "");
      }
    } else if (name != null) {
      log("Unknown location [%s]%n", name);
    }
  }

  private class CollectiveContext extends XmlContext {
    public String xml;
    public String id;
    public CWRSCollective entity = new CWRSCollective();

    public CollectiveContext(String xml, String id) {
      this.xml = xml;
      this.id = id;
    }

    public void error(String format, Object... args) {
      log("[%s] %s%n", id, String.format(format, args));
    }
  }

  private class CollectiveVisitor extends DelegatingVisitor<CollectiveContext> {
    public CollectiveVisitor(CollectiveContext context) {
      super(context);
      setDefaultElementHandler(new DefaultCollectiveHandler());
      addElementHandler(new CollectiveIdHandler(), "cooperationId");
      addElementHandler(new CollectiveTypeHandler(), "type");
      addElementHandler(new CollectiveNamesHandler(), "names");
      addElementHandler(new CollectiveLocationHandler(), "location");
      addElementHandler(new CollectiveLinkHandler(), "reference");
    }
  }

  private class DefaultCollectiveHandler extends DefaultElementHandler<CollectiveContext> {
    private final Set<String> ignoredNames = Sets.newHashSet("cooperation");

    @Override
    public Traversal enterElement(Element element, CollectiveContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name)) {
        context.error("Unexpected element: %s%nxml: %s", name, context.xml);
      }
      return Traversal.NEXT;
    }
  }

  private class CollectiveIdHandler extends CaptureHandler<CollectiveContext> {
    @Override
    public void handleContent(String text, CollectiveContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class CollectiveTypeHandler extends CaptureHandler<CollectiveContext> {

    @Override
    public void handleContent(String text, CollectiveContext context) {
      if (text.equals("Publishing House")) {
        text = "PUBLISHER";
      }
      String normalized = Collective.Type.normalize(text);
      if (normalized.equals(Collective.Type.UNKNOWN)) {
        context.error("Unknown type: %s", text);
      }
      context.entity.setType(normalized);
    }
  }

  private class CollectiveLinkHandler extends CaptureHandler<CollectiveContext> {
    // Collectives do not occur as collection in the old Women Writers database.
    // So references, if any, can be treated as simple links.

    @Override
    public void handleContent(String text, CollectiveContext context) {
      context.entity.addLink(new Link(text));
    }
  }


  private class CollectiveLocationHandler extends CaptureHandler<CollectiveContext> {

    @Override
    public void handleContent(String text, CollectiveContext context) {
      context.entity.tempLocation = text;
    }
  }

  private final Tokens nameTokens = new Tokens();

  private class CollectiveNamesHandler extends CaptureHandler<CollectiveContext> {

    @Override
    public void handleContent(String text, CollectiveContext context) {
      // TODO model name variants for collectives
      context.entity.tempNames.add(text);
      if (Strings.isNullOrEmpty(context.entity.getName()) || Text.isCyrillicText(text)) {
        context.entity.setName(text);
      }
      nameTokens.increment(text);
    }
  }

  // --- persons ---------------------------------------------------------------

  private void importPersons() throws Exception {
    String xml = getResource(URL, "persons");
    List<String> personIds = parseIdResource(xml, "personId");
    log("Retrieved %d id's.%n", personIds.size());

    for (String id : personIds) {
      xml = getResource(URL, "person", id);
      CWRSPerson entity = parsePersonResource(xml, id);
      if (accept(entity)) {
        String storedId = updateExistingPerson(entity);
        if (storedId == null) {
          storedId = createNewPerson(entity);
        }
        storeReference(id, CWRSPerson.class, storedId);

        handleLanguages(entity);

        indexManager.addEntity(CWRSPerson.class, storedId);
        indexManager.updateEntity(WWPerson.class, storedId);
      }
    }
  }

  private CWRSPerson parsePersonResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    PersonContext context = new PersonContext(xml, id);
    document.accept(new PersonVisitor(context));
    return context.person;
  }

  private boolean accept(CWRSPerson entity) {
    List<PersonName> names = entity.getNames();
    if (names.size() == 1 && names.get(0).getFullName().equalsIgnoreCase("Anonymous")) {
      log("Rejected anonymous person%n");
      // TODO register this one, in order to ignore relations
      return false;
    }
    return true;
  }

  // Retrieve existing WWPerson, add CWRSPerson variation
  private String updateExistingPerson(CWRSPerson entity) {
    String storedId = null;
    if (!Strings.isNullOrEmpty(entity.tempNewwId)) {
      WWPerson person = repository.findEntity(WWPerson.class, "tempOldId", entity.tempNewwId);
      if (person == null) {
        log("Failed to find person with old id %s%n", entity.tempNewwId);
      } else {
        storedId = person.getId();
        entity.setId(storedId);
        entity.setRev(person.getRev());
        updateProjectDomainEntity(CWRSPerson.class, entity, change);
        log("Updated person with id %s%n", storedId);
      }
    }
    return storedId;
  }

  // Save as CWRSPerson, add WWPerson variation
  private String createNewPerson(CWRSPerson entity) {
    String storedId = addDomainEntity(CWRSPerson.class, entity, change);
    WWPerson person = repository.getEntity(WWPerson.class, storedId);
    updateProjectDomainEntity(WWPerson.class, person, change);
    return storedId;
  }

  private void handleLanguages(CWRSPerson entity) {
    for (String code : entity.tempLanguageCodes) {
      Language language = getLanguage(code);
      if (language == null) {
        log("Failed to retrieve language with code %s%n", code);
      } else {
        Reference typeRef = getRelationTypeRef("hasPersonLanguage", true);
        Reference sourceRef = new Reference(Person.class, entity.getId());
        Reference targetRef = new Reference(Language.class, language.getId());
        addRelation(CWRSRelation.class, typeRef, sourceRef, targetRef, change, "");
      }
    }
  }

  private class PersonContext extends XmlContext {
    public String xml;
    public String id;
    public PersonName personName;
    public CWRSPerson person = new CWRSPerson();

    public PersonContext(String xml, String id) {
      this.xml = xml;
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
      addElementHandler(new PlaceOfBirthHandler(), "placeOfBirth");
      addElementHandler(new DateOfDeathHandler(), "dateOfDeath");
      addElementHandler(new PlaceOfDeathHandler(), "placeOfDeath");
      addElementHandler(new NameHandler(), "names");
      addElementHandler(new NameComponentHandler(), "forename", "surname");
      addElementHandler(new PersNameHandler(), "persName");
      addElementHandler(new PersonLanguagesHandler(), "languages");
      addElementHandler(new PersonLinkHandler(), "reference");
    }
  }

  private class DefaultPersonHandler extends DefaultElementHandler<PersonContext> {
    private final Set<String> ignoredNames = Sets.newHashSet("person", "names", "languages");

    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name)) {
        context.error("Unexpected element: %s%n%s", name, context.xml);
      }
      return Traversal.NEXT;
    }
  }

  private class PersonIdHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class PersonTypeHandler extends CaptureHandler<PersonContext> {
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

  private class GenderHandler extends CaptureHandler<PersonContext> {
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

  private class DateOfBirthHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setBirthDate(datable);
    }
  }

  private class DateOfDeathHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setDeathDate(datable);
    }
  }

  private class PlaceOfBirthHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.tempBirthPlace = text;
      // System.out.println("PlaceOfBirth: " + text);
    }
  }

  private class PlaceOfDeathHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.tempDeathPlace = text;
      // System.out.println("PlaceOfDeath: " + text);
    }
  }

  private class PersNameHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      List<String> words = Splitter.on(' ').splitToList(text);
      int n = words.size();
      if (n > 0) {
        for (int i = 0; i < n - 1; i++) {
          context.personName.addNameComponent(PersonNameComponent.Type.FORENAME, words.get(i));
        }
        context.personName.addNameComponent(PersonNameComponent.Type.SURNAME, words.get(n - 1));
      }
    }
  }

  private class NameHandler implements ElementHandler<PersonContext> {
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      context.personName = new PersonName();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, PersonContext context) {
      if (context.personName.getComponents().size() != 0) {
        context.person.addName(context.personName);
      }
      return Traversal.NEXT;
    }
  }

  private class NameComponentHandler implements ElementHandler<PersonContext> {
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }

    @Override
    public Traversal leaveElement(Element element, PersonContext context) {
      String text = context.closeLayer();
      if (element.hasName("forename")) {
        context.personName.addNameComponent(PersonNameComponent.Type.FORENAME, text);
      } else if (element.hasName("surname")) {
        context.personName.addNameComponent(PersonNameComponent.Type.SURNAME, text);
      } else {
        context.error("Unknown component: %s", element.getName());
      }
      return Traversal.NEXT;
    }
  }

  private class PersonLinkHandler extends CaptureHandler<PersonContext> {
    private static final String NEWW_URL = "http://neww.huygens.knaw.nl/authors/show/";

    @Override
    public void handleContent(String text, PersonContext context) {
      if (text.startsWith(NEWW_URL)) {
        log("Reference to NEWW: %s%n", text);
        context.person.tempNewwId = text.substring(NEWW_URL.length());
      } else {
        context.person.addLink(new Link(text));
      }
    }
  }

  private class PersonLanguagesHandler extends CaptureHandler<PersonContext> {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.tempLanguageCodes.add(text);
    }
  }

  // --- documents -------------------------------------------------------------

  private void importDocuments() throws Exception {
    String xml = getResource(URL, "documents");
    List<String> documentIds = parseIdResource(xml, "documentId");
    log("Retrieved %d id's.%n", documentIds.size());

    for (String id : documentIds) {
      xml = getResource(URL, "document", id);
      CWRSDocument entity = parseDocumentResource(xml, id);
      String storedId = updateExistingDocument(entity);
      if (storedId == null) {
        storedId = createNewDocument(entity);
      }
      storeReference(id, CWRSDocument.class, storedId);

      handleLanguages(entity);

      indexManager.addEntity(CWRSDocument.class, storedId);
      indexManager.updateEntity(WWDocument.class, storedId);
    }
  }

  private CWRSDocument parseDocumentResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    DocumentContext context = new DocumentContext(id);
    document.accept(new DocumentVisitor(context));
    return context.document;
  }

  // Retrieve existing WWDocument, add CWRSDocument variation
  private String updateExistingDocument(CWRSDocument entity) {
    String storedId = null;
    if (!Strings.isNullOrEmpty(entity.tempNewwId)) {
      WWDocument document = repository.findEntity(WWDocument.class, "tempOldId", entity.tempNewwId);
      if (document != null) {
        storedId = document.getId();
        entity.setId(storedId);
        entity.setRev(document.getRev());
        updateProjectDomainEntity(CWRSDocument.class, entity, change);
        log("Updated document with id %s%n", storedId);
      }
    }
    return storedId;
  }

  // Save as CWRSDocument, add WWDocument variation
  private String createNewDocument(CWRSDocument entity) {
    String storedId = addDomainEntity(CWRSDocument.class, entity, change);
    WWDocument document = repository.getEntity(WWDocument.class, storedId);
    updateProjectDomainEntity(WWDocument.class, document, change);
    return storedId;
  }

  private void handleLanguages(CWRSDocument entity) {
    for (String code : entity.tempLanguages) {
      Language language = getLanguage(code);
      if (language == null) {
        log("Failed to retrieve language with code %s%n", code);
      } else {
        Reference typeRef = getRelationTypeRef("hasWorkLanguage", true);
        Reference sourceRef = new Reference(Document.class, entity.getId());
        Reference targetRef = new Reference(Language.class, language.getId());
        addRelation(CWRSRelation.class, typeRef, sourceRef, targetRef, change, "");
      }
    }
  }

  private class DocumentContext extends XmlContext {
    public String id;
    public CWRSDocument document = new CWRSDocument();

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
      addElementHandler(new DocumentIdHandler(), "documentId"); // OK
      addElementHandler(new DocumentTypeHandler(), "type"); // OK
      addElementHandler(new DocumentTitleHandler(), "title"); // OK
      addElementHandler(new DocumentDescriptionHandler(), "description"); // OK
      addElementHandler(new DocumentDateHandler(), "date"); // OK
      addElementHandler(new DocumentLanguageHandler(), "language");
      addElementHandler(new DocumentLinkHandler(), "reference");
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
    public void handleContent(String text, DocumentContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class DocumentTypeHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      for (Document.DocumentType type : Document.DocumentType.values()) {
        if (text.equalsIgnoreCase(type.name())) {
          context.document.setDocumentType(type);
          return;
        }
      }
      context.error("Unknown document type: %s", text);
    }
  }

  private class DocumentTitleHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.setTitle(text);
    }
  }

  private class DocumentDescriptionHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.setDescription(text);
    }
  }

  private class DocumentDateHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      Datable datable = new Datable(text);
      context.document.setDate(datable);
    }
  }

  private class DocumentLanguageHandler extends CaptureHandler<DocumentContext> {
    @Override
    public void handleContent(String text, DocumentContext context) {
      context.document.tempLanguages.add(text);
    }
  }

  private class DocumentLinkHandler extends CaptureHandler<DocumentContext> {
    private static final String NEWW_URL = "http://neww.huygens.knaw.nl/works/show/";

    @Override
    public void handleContent(String text, DocumentContext context) {
      log("Reference: %s%n", text);
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

    for (String id : relationIds) {
      xml = getResource(id);
      parseRelationResource(xml, id);
    }
  }

  private void parseRelationResource(String xml, String id) {
    nl.knaw.huygens.tei.Document document = nl.knaw.huygens.tei.Document.createFromXml(xml);
    RelationContext context = new RelationContext(id);
    document.accept(new RelationVisitor(context));

    // Resolve ambiguous reception type
    if ("isWorkCommentedOnIn".equals(context.relationTypeName) && context.targetId.contains("/person/")) {
      context.relationTypeName = "isPersonCommentedOnIn";
    }

    Reference typeRef = relationTypes.get(context.relationTypeName);
    Reference sourceRef = references.get(context.sourceId);
    Reference targetRef = references.get(context.targetId);
    // suppose that type is ambiguous, line <<vomments on>> How do we resolve?
    if (typeRef != null && sourceRef != null && targetRef != null) {
      addRelation(CWRSRelation.class, typeRef, sourceRef, targetRef, change, xml);
    } else {
      log("Null reference in %s: %s --> %s%n", context.relationTypeName, context.sourceId, context.targetId);
    }
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
    public void handleContent(String text, RelationContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private class RelationLinkHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.error("Unexpected reference: %s", text);
    }
  }

  private class RelationTypeHandler extends CaptureHandler<RelationContext> {
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
      } else if (text.equalsIgnoreCase("published by")) {
        context.relationTypeName = "isPublishedBy";
      } else if (text.equalsIgnoreCase("created by")) {
        context.relationTypeName = "isCreatedBy";
      } else if (text.equalsIgnoreCase("translated by")) {
        context.relationTypeName = "<<translated by>>";
      } else if (text.equalsIgnoreCase("comments on")) {
        context.relationTypeName = "isWorkCommentedOnIn"; // ambiguous...
      } else if (text.equalsIgnoreCase("pseudonim of")) {
        context.relationTypeName = "isPseudonymOf";
      } else {
        context.error("Unexpected relation type: '%s'", text);
        System.exit(0);
      }
    }
  }

  private class RelationActiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.sourceId = text;
    }
  }

  private class RelationPassiveHandler extends CaptureHandler<RelationContext> {
    @Override
    public void handleContent(String text, RelationContext context) {
      context.targetId = text;
    }
  }

}
