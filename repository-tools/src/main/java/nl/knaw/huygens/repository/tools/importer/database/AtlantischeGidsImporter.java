package nl.knaw.huygens.repository.tools.importer.database;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.IndexManager;
import nl.knaw.huygens.repository.index.IndexService;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.model.DocumentRef;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.Reference;
import nl.knaw.huygens.repository.model.Relation;
import nl.knaw.huygens.repository.model.RelationType;
import nl.knaw.huygens.repository.model.atlg.ATLGArchive;
import nl.knaw.huygens.repository.model.atlg.ATLGArchiver;
import nl.knaw.huygens.repository.model.atlg.ATLGKeyword;
import nl.knaw.huygens.repository.model.atlg.ATLGLegislation;
import nl.knaw.huygens.repository.model.atlg.ATLGPerson;
import nl.knaw.huygens.repository.model.atlg.XRelated;
import nl.knaw.huygens.repository.model.util.PersonName;
import nl.knaw.huygens.repository.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.repository.storage.RelationManager;
import nl.knaw.huygens.repository.storage.RelationManager.RelationBuilder;
import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.repository.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.repository.tools.util.EncodingFixer;
import nl.knaw.huygens.repository.tools.util.Files;
import nl.knaw.huygens.repository.tools.util.Token;
import nl.knaw.huygens.repository.tools.util.TokenHandler;
import nl.knaw.huygens.repository.tools.util.Tokens;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Imports data of the "Atlantische Gids" project.
 * 
 * Usage:
 *  java  -cp [specs]  nl.knaw.huygens.repository.importer.database.AtlantischeGidsImporter  importDirName  configFileName
 * 
 * The commandline arguments are optional.
 * 
 * Note that the Mongo database and the Solr index are built from scratch, any existing data is deleted.
 * Future versions of this importer must use a more subtle approach.
 */
public class AtlantischeGidsImporter extends DefaultImporter {

  public static void main(String[] args) throws Exception {

    // Handle commandline argfuments
    String importDirName = (args.length > 0) ? args[0] : "../../AtlantischeGids/work/";
    System.out.println("Import directory: " + importDirName);
    String configFileName = (args.length > 1) ? args[1] : "config.xml";
    System.out.println("Configuration file: " + configFileName);

    Configuration config = new Configuration(configFileName);
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    Broker broker = null;
    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      broker = injector.getInstance(Broker.class);
      broker.start();

      storageManager = injector.getInstance(StorageManager.class);
      storageManager.clear();

      indexManager = injector.getInstance(IndexManager.class);
      indexManager.deleteAllDocuments();

      IndexService service = injector.getInstance(IndexService.class);
      Thread thread = new Thread(service);
      thread.start();

      long start = System.currentTimeMillis();

      DocTypeRegistry registry = injector.getInstance(DocTypeRegistry.class);
      RelationManager relationManager = new RelationManager(registry, storageManager);
      new AtlantischeGidsImporter(registry, relationManager, storageManager, importDirName).importAll();

      storageManager.ensureIndices();

      // Signal we're done
      BulkImporter.sendEndOfDataMessage(broker);

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

      BulkImporter.waitForCompletion(thread, 5 * 60 * 1000);

      time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n", time);

    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.close();
      }
      if (broker != null) {
        broker.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  private static final String[] JSON_EXTENSION = { "json" };

  private final ObjectMapper objectMapper;
  private final RelationManager relationManager;
  private final File inputDir;

  private final Graph graph = new Graph();
  private final Map<DocumentRef, DocumentRef> docRefMap = Maps.newHashMap();
  private final Map<String, DocumentRef> keywordDocRefMap = Maps.newHashMap();
  private final Map<String, Reference> keywordRefMap = Maps.newHashMap();
  private final Map<String, DocumentRef> personDocRefMap = Maps.newHashMap();
  private final Map<String, Reference> personRefMap = Maps.newHashMap();

  private Reference isCreatorRef;
  private Reference hasKeywordRef;
  private Reference hasPersonRef;
  private Reference hasPlaceRef;

  public AtlantischeGidsImporter(DocTypeRegistry registry, RelationManager relationManager, StorageManager storageManager, String inputDirName) {
    super(registry, storageManager);
    objectMapper = new ObjectMapper();
    this.relationManager = relationManager;
    inputDir = new File(inputDirName);
    System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
  }

  private <T> T readJsonValue(File file, Class<T> valueType) throws Exception {
    String text = Files.readTextFromFile(file);
    // For Atlantische Gids it seems OK to map "Ã " --> "à "
    String converted = EncodingFixer.convert2(text).replaceAll("Ã ", "à ");
    if (!converted.equals(text)) {
      int n = text.length() - converted.length();
      handleError("Fixed %d character encoding error%s in '%s'", n, (n == 1) ? "" : "s", file.getName());
    }
    return objectMapper.readValue(converted, valueType);
  }

  private Graph convertGraph(Graph graph) {
    Graph result = new Graph();
    for (Vertex vertex : graph.getVertices()) {
      DocumentRef srce = docRefMap.get(vertex.getDocumentRef());
      if (srce == null) {
        throw new RuntimeException("Unable to resolve srce node " + vertex.getDocumentRef());
      }
      for (Edge edge : vertex.getEdges()) {
        DocumentRef dest = docRefMap.get(edge.getDest());
        if (dest == null) {
          throw new RuntimeException("Unable to resolve dest node " + edge.getDest());
        }
        result.addEdge(srce, dest, edge.getTypes());
      }
    }
    return result;
  }

  public void importAll() throws Exception {
    System.out.printf("%n.. Relation types%n");
    importRelationTypes();
    System.out.printf("Number of entries = 4%n");

    System.out.printf("%n.. Keywords%n");
    importKeywords();
    System.out.printf("Number of entries = %d%n", keywordRefMap.size());

    System.out.printf("%n.. Persons%n");
    importPersons();
    System.out.printf("Number of entries = %d%n", personRefMap.size());

    System.out.printf("%n.. Legislation%n");
    int n = importLegislation();
    System.out.printf("Number of entries = %d%n", n);

    System.out.printf("%n.. Archives -- pass 1%n");
    List<String> archiveIds = importArchiefMats();
    System.out.printf("Number of entries = %d%n", archiveIds.size());

    System.out.printf("%n.. Archivers -- pass 1%n");
    List<String> archiverIds = importCreators();
    System.out.printf("Number of entries = %d%n", archiverIds.size());

    Graph newGraph = convertGraph(graph);

    System.out.printf("%n.. Archives -- pass 2%n");
    resolveArchiveRefs(archiveIds, newGraph);

    System.out.printf("%n.. Archivers -- pass 2%n");
    resolveArchiverRefs(archiverIds);

    displayErrorSummary();
  }

  // --- relations -----------------------------------------------------

  private void importRelationTypes() {
    RelationType type = new RelationType("is_creator_of", ATLGArchiver.class, ATLGArchive.class);
    addDocument(RelationType.class, type, true);
    isCreatorRef = new Reference(RelationType.class, type.getId());

    type = new RelationType("has_keyword", DomainDocument.class, ATLGKeyword.class);
    addDocument(RelationType.class, type, true);
    hasKeywordRef = new Reference(RelationType.class, type.getId());

    type = new RelationType("has_person", DomainDocument.class, ATLGPerson.class);
    addDocument(RelationType.class, type, true);
    hasPersonRef = new Reference(RelationType.class, type.getId());

    type = new RelationType("has_place", DomainDocument.class, ATLGKeyword.class);
    addDocument(RelationType.class, type, true);
    hasPlaceRef = new Reference(RelationType.class, type.getId());
  }

  private void addRelations(Reference sourceRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        addRelation(sourceRef, relTypeRef, map.get(key));
      }
    }
  }

  private void addRelation(Reference sourceRef, Reference relTypeRef, Reference targetRef) {
    RelationBuilder builder = relationManager.getBuilder();
    Relation relation = builder.source(sourceRef).type(relTypeRef).target(targetRef).build();
    if (relation != null) {
      addDocument(Relation.class, relation, true);
    }
  }

  // --- Keywords ------------------------------------------------------

  private static final String KEYWORD_DIR = "keywords";
  private static final String KEYWORD_FILE = "keywords.json";

  private void importKeywords() throws Exception {
    File file = new File(new File(inputDir, KEYWORD_DIR), KEYWORD_FILE);
    for (XKeyword xkeyword : readJsonValue(file, XKeyword[].class)) {
      String jsonId = xkeyword._id;
      if (keywordRefMap.containsKey(jsonId)) {
        System.err.printf("## [%s] Duplicate keyword id %s%n", KEYWORD_FILE, jsonId);
      } else {
        ATLGKeyword keyword = convert(xkeyword);
        String storedId = addDocument(ATLGKeyword.class, keyword, true);
        keywordRefMap.put(jsonId, new Reference(ATLGKeyword.class, storedId));
        keywordDocRefMap.put(jsonId, newDocumentRef(ATLGKeyword.class, keyword));
      }
    }
  }

  private ATLGKeyword convert(XKeyword xkeyword) {
    ATLGKeyword keyword = new ATLGKeyword();

    String type = xkeyword.type;
    keyword.setType(type);

    if (type == null) {
      System.err.println("Missing type");
    } else if (type.equals("subject")) {
      keyword.setValue(xkeyword.onderwerp);
    } else if (type.equals("geography")) {
      keyword.setValue(xkeyword.regionaam);
    } else {
      System.err.println("Unknown type" + type);
      keyword.setValue("?");
    }

    if (xkeyword.label != null) {
      keyword.setLabel(xkeyword.label);
    }

    return keyword;
  }

  // --- Persons -------------------------------------------------------

  private static final String PERSON_DIR = "keywords";
  private static final String PERSON_FILE = "persons.json";

  private void importPersons() throws Exception {
    File file = new File(new File(inputDir, PERSON_DIR), PERSON_FILE);
    for (XPerson xperson : readJsonValue(file, XPerson[].class)) {
      String jsonId = xperson._id;
      if (personRefMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate person id %s", PERSON_FILE, jsonId);
      } else {
        ATLGPerson person = convert(xperson);
        String storedId = addDocument(ATLGPerson.class, person, true);
        personRefMap.put(jsonId, new Reference(ATLGPerson.class, storedId));
        personDocRefMap.put(jsonId, newDocumentRef(ATLGPerson.class, person));
      }
    }
  }

  private ATLGPerson convert(XPerson xperson) {
    ATLGPerson person = new ATLGPerson();

    PersonName name = new PersonName();
    if (xperson.voorl != null) {
      name.addNameComponent(Type.FORENAME, xperson.voorl);
    }
    if (xperson.tussenv != null) {
      name.addNameComponent(Type.NAME_LINK, xperson.tussenv);
    }
    if (xperson.achternaam != null) {
      name.addNameComponent(Type.SURNAME, xperson.achternaam);
    }
    if (xperson.toevoeging != null) {
      name.addNameComponent(Type.ADD_NAME, xperson.toevoeging);
    }
    person.setName(name);

    if (xperson.label != null) {
      String value = StringUtils.join(xperson.label, "; ");
      person.setLabel(value);
    }

    if (xperson.verwijzing != null) {
      String value = StringUtils.join(xperson.verwijzing, "; ");
      person.setReference(value);
    }

    return person;
  }

  // --- Legislation ---------------------------------------------------

  private static final String WETGEVING_DIR = "wetgeving";

  public int importLegislation() throws Exception {
    Set<String> refs = Sets.newHashSet();
    File directory = new File(inputDir, WETGEVING_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (WetgevingEntry entry : readJsonValue(file, WetgevingEntry[].class)) {
        Wetgeving wetgeving = entry.wetgeving;
        String jsonId = wetgeving._id;
        if (refs.contains(jsonId)) {
          handleError("[%s] Duplicate wetgeving id %s", file.getName(), jsonId);
        } else {
          ATLGLegislation legislation = convert(wetgeving);
          String storedId = addDocument(ATLGLegislation.class, legislation, true);
          Reference legislationRef = new Reference(ATLGLegislation.class, storedId);
          addLegislationRelations(legislationRef, wetgeving);
          refs.add(jsonId);
        }
      }
    }
    return refs.size();
  }

  private ATLGLegislation convert(Wetgeving wetgeving) {
    ATLGLegislation legislation = new ATLGLegislation();
    legislation.setOrigFilename(wetgeving.orig_filename);
    legislation.setReference(wetgeving.reference);
    legislation.setPages(wetgeving.pages);
    legislation.setTitleNld(wetgeving.titel);
    legislation.setTitleEng(wetgeving.titel_eng);
    if (wetgeving.dates != null) {
      legislation.setDate1(wetgeving.dates.date1);
      legislation.setDate2(wetgeving.dates.date2);
    }
    // delete
    if (wetgeving.geography != null) {
      for (String keyword : wetgeving.geography) {
        legislation.addPlaceKeyword(keywordDocRefMap.get(keyword));
      }
    }
    // delete
    if (wetgeving.keywords != null) {
      for (String keyword : wetgeving.keywords) {
        legislation.addGroupKeyword(keywordDocRefMap.get(keyword));
      }
    }
    // delete
    if (wetgeving.keywords_extra != null) {
      for (String keyword : wetgeving.keywords_extra) {
        legislation.addOtherKeyword(keywordDocRefMap.get(keyword));
      }
    }
    // delete
    if (wetgeving.persons != null) {
      for (String keyword : wetgeving.persons) {
        legislation.addPerson(personDocRefMap.get(keyword));
      }
    }
    legislation.setContents(wetgeving.contents);
    if (wetgeving.see_also != null) {
      for (XSeeAlso item : wetgeving.see_also) {
        legislation.addSeeAlso(item.toString());
      }
    }
    if (wetgeving.other_publication != null) {
      for (String publication : wetgeving.other_publication) {
        legislation.addOtherPublication(publication);
      }
    }
    legislation.setOriginalArchivalSource(wetgeving.original_archival_source);
    legislation.setLinkArchivalDBase(wetgeving.link_archival_dbase);
    legislation.setRemarks(wetgeving.remarks);
    legislation.setScan(wetgeving.scan);
    legislation.setPartsToScan(wetgeving.partstoscan);
    legislation.setMadeBy(wetgeving.made_by);
    legislation.setReminders(wetgeving.Aantekeningen);
    return legislation;
  }

  private void addLegislationRelations(Reference reference, Wetgeving wetgeving) {
    addRelations(reference, hasPlaceRef, keywordRefMap, wetgeving.geography);
    addRelations(reference, hasKeywordRef, keywordRefMap, wetgeving.keywords);
    addRelations(reference, hasKeywordRef, keywordRefMap, wetgeving.keywords_extra);
    addRelations(reference, hasPersonRef, personRefMap, wetgeving.persons);
  }

  // --- Archives ------------------------------------------------------

  private static final String ARCHIEFMAT_DIR = "archiefmat";

  public List<String> importArchiefMats() throws Exception {
    List<String> ids = Lists.newArrayList();
    Tokens tokens = new Tokens();
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : readJsonValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat object = entry.archiefmat;
        String id = object._id;
        DocumentRef key = newDocumentRef(ATLGArchive.class, id);
        if (docRefMap.containsKey(key)) {
          handleError("[%s] Duplicate entry %s", file.getName(), key);
        } else {
          ATLGArchive archive = convert(object);
          addDocument(ATLGArchive.class, archive, false);
          docRefMap.put(key, newDocumentRef(ATLGArchive.class, archive));
          ids.add(archive.getId());
          tokens.increment(archive.getIndexedRefCode());
        }
      }
    }
    // displayRefCodes(tokens);
    return ids;
  }

  void displayRefCodes(Tokens tokens) {
    System.out.printf("%nGenerated reference codes (frequency and name)%n");
    tokens.handleSortedByText(new TokenHandler() {
      @Override
      public boolean handle(Token token) {
        System.out.printf("%04d :  %s%n", token.getCount(), token.getText());
        return true;
      }
    });
  }

  private ATLGArchive convert(ArchiefMat archiefmat) {
    ATLGArchive archive = new ATLGArchive();
    archive.setOrigFilename(archiefmat.orig_filename);
    if (archiefmat.countries != null) {
      for (String country : archiefmat.countries) {
        archive.addCountry(country);
      }
    }
    archive.setRefCodeArchive(archiefmat.rf_archive);
    archive.setRefCode(archiefmat.ref_code);
    archive.setSubCode(archiefmat.code_subfonds);
    archive.setSeries(archiefmat.series);
    archive.setItemNo(archiefmat.itemno);
    archive.setTitleNld(archiefmat.titel);
    archive.setTitleEng(archiefmat.titel_eng);
    if (archiefmat.dates != null) {
      archive.setBeginDate(archiefmat.dates.begin_date);
      archive.setEndDate(archiefmat.dates.end_date);
    }
    archive.setPeriodDescription(archiefmat.period_description);
    archive.setExtent(archiefmat.extent);
    archive.setFindingAid(archiefmat.finding_aid);
    if (archiefmat.creators != null) {
      for (String creator : archiefmat.creators) {
        // This field is implied by creator records
        handleError("[%s] Ignoring creator '%s'", archiefmat.titel_eng, creator);
      }
    }
    archive.setScope(archiefmat.scope);
    if (archiefmat.geography != null) {
      for (String keyword : archiefmat.geography) {
        archive.addPlaceKeyword(keywordDocRefMap.get(keyword));
      }
    }
    if (archiefmat.keywords != null) {
      for (String keyword : archiefmat.keywords) {
        archive.addSubjectKeyword(keywordDocRefMap.get(keyword));
      }
    }
    if (archiefmat.persons != null) {
      for (String keyword : archiefmat.persons) {
        archive.addPerson(personDocRefMap.get(keyword));
      }
    }
    archive.setNotes(archiefmat.notes);
    archive.setMadeBy(archiefmat.made_by);
    archive.setReminders(archiefmat.Aantekeningen);
    if (archiefmat.related != null) {
      for (XRelated item : archiefmat.related) {
        if ("overhead_title".equals(item.type)) {
          for (String id : item.ids) {
            archive.addOverheadArchive(newDocumentRef(ATLGArchive.class, id));
          }
        } else if ("underlying_levels_titels".equals(item.type)) {
          for (String id : item.ids) {
            archive.addUnderlyingArchive(newDocumentRef(ATLGArchive.class, id));
          }
        } else if ("unit".equals(item.type)) {
          for (String id : item.ids) {
            archive.addRelatedUnitArchive(newDocumentRef(ATLGArchive.class, id));
          }
        } else {
          handleError("Ignoring field 'related' with type '%s'", item.type);
        }
      }

      // Ignored fields
      if (archiefmat.relation != null) {
        handleError("Ignoring field 'relation':'%s'", archiefmat.relation);
      }
      if (archiefmat.em != null) {
        handleError("Ignoring field 'em':'%s'", archiefmat.em);
      }
    }

    return archive;
  }

  private void resolveArchiveRefs(List<String> archiveIds, Graph newGraph) throws Exception {
    for (String archiveId : archiveIds) {
      ATLGArchive archive = getDocument(ATLGArchive.class, archiveId);
      String filename = archive.getOrigFilename();
      List<DocumentRef> oldRefs = archive.getOverheadArchives();
      if (oldRefs != null && oldRefs.size() != 0) {
        archive.setOverheadArchives(resolveRefs(filename, "overhead archive", oldRefs));
      }
      oldRefs = archive.getUnderlyingArchives();
      if (oldRefs != null && oldRefs.size() != 0) {
        archive.setUnderlyingArchives(resolveRefs(filename, "underlying archive", oldRefs));
      }
      oldRefs = archive.getRelatedUnitArchives();
      if (oldRefs != null && oldRefs.size() != 0) {
        archive.setRelatedUnitArchives(resolveRefs(filename, "related unit archive", oldRefs));
      }

      // boolean first = true;
      Vertex vertex = newGraph.getVertex(newDocumentRef(ATLGArchive.class, archive));
      for (Edge edge : vertex.getEdges()) {
        if (edge.containsType("created_by")) {
          // if (first) {
          //    System.out.println(archive.getDisplayName());
          //    first = false;
          // }
          // System.out.println("  by: " + edge.getDest().getDisplayName());
          archive.addCreator(edge.getDest());
        }
      }

      modDocument(ATLGArchive.class, archive);
    }
  }

  /**
   * Resolve the id's of the document references.
   * Duplicates are removed, but the order of the references is preserved.
   */
  private List<DocumentRef> resolveRefs(String filename, String type, List<DocumentRef> oldRefs) {
    List<DocumentRef> newRefs = Lists.newArrayList();
    for (DocumentRef oldRef : oldRefs) {
      DocumentRef newRef = docRefMap.get(oldRef);
      if (newRef == null) {
        handleError("[%s] No %s for id %s", filename, type, oldRef.getId());
      } else if (newRefs.contains(newRef)) {
        handleError("[%s] %s has duplicate reference to '%s'", filename, type, newRef.getDisplayName());
      } else {
        newRefs.add(newRef);
      }
    }
    return newRefs;
  }

  // --- Archivers -----------------------------------------------------

  private static final String CREATORS_DIR = "creators";

  public List<String> importCreators() throws Exception {
    List<String> ids = Lists.newArrayList();
    File directory = new File(inputDir, CREATORS_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      CreatorEntry[] entries = readJsonValue(file, CreatorEntry[].class);
      for (CreatorEntry entry : entries) {
        Creator creator = entry.creator;
        String id = creator._id;
        DocumentRef key = newDocumentRef(ATLGArchiver.class, id);
        if (docRefMap.containsKey(key)) {
          handleError("[%s] Duplicate entry %s", file.getName(), key);
        } else {
          ATLGArchiver archiver = convert(creator);
          addDocument(ATLGArchiver.class, archiver, false);
          docRefMap.put(key, newDocumentRef(ATLGArchiver.class, archiver));
          // this looks awfully smart: this creator has created a number of archives
          // what remains is to create a new graph with references converted
          // then a simple lookup allows one to set the relarions in the objects
          for (DocumentRef ref : archiver.getRelatedArchives()) {
            graph.addEdge(key, ref, "created");
            graph.addEdge(ref, key, "created_by");
          }
          ids.add(archiver.getId());
        }
      }
    }
    return ids;
  }

  private ATLGArchiver convert(Creator creator) {
    ATLGArchiver archiver = new ATLGArchiver();
    archiver.setOrigFilename(creator.orig_filename);
    archiver.setNameNld(creator.name);
    archiver.setNameEng(creator.name_english);
    if (creator.dates != null) {
      archiver.setBeginDate(creator.dates.begin_date);
      archiver.setEndDate(creator.dates.end_date);
    }
    archiver.setPeriodDescription(creator.period_description);
    archiver.setHistory(creator.his_func);
    if (creator.related_archives != null) {
      handleError("Ignoring field 'related_archives'");
    }
    if (creator.related_creators != null) {
      handleError("Ignoring field 'related_creators'");
      // for (String id : creator.related_creators) {
      //   archiver.addRelatedArchiver(new DocumentRef(ATLGArchiver.class, id));
      // }
    }
    if (creator.geography != null) {
      for (String keyword : creator.geography) {
        archiver.addPlaceKeyword(keywordDocRefMap.get(keyword));
      }
    }
    if (creator.keywords != null) {
      for (String keyword : creator.keywords) {
        archiver.addSubjectKeyword(keywordDocRefMap.get(keyword));
      }
    }
    if (creator.persons != null) {
      for (String keyword : creator.persons) {
        archiver.addPerson(personDocRefMap.get(keyword));
      }
    }
    archiver.setNotes(creator.notes);
    archiver.setLiterature(creator.literatuur);
    archiver.setMadeBy(creator.made_by);
    archiver.setReminders(creator.Aantekeningen);
    if (creator.related != null) {
      for (XRelated item : creator.related) {
        if ("archive".equals(item.type)) {
          for (String id : item.ids) {
            archiver.addRelatedArchive(newDocumentRef(ATLGArchive.class, id));
          }
        } else if ("creator".equals(item.type)) {
          for (String id : item.ids) {
            archiver.addRelatedArchiver(newDocumentRef(ATLGArchiver.class, id));
          }
        } else {
          handleError("Ignoring field 'related' with type '%s'", item.type);
        }
      }
    }
    if (creator.types != null) {
      for (String type : creator.types) {
        archiver.addType(type);
      }
    }
    return archiver;
  }

  private void resolveArchiverRefs(List<String> archiverIds) throws Exception {
    for (String id : archiverIds) {
      ATLGArchiver archiver = getDocument(ATLGArchiver.class, id);
      Reference sourceRef = new Reference(ATLGArchiver.class, id);
      String filename = archiver.getOrigFilename();
      List<DocumentRef> oldRefs = archiver.getRelatedArchives();
      if (oldRefs != null && oldRefs.size() != 0) {
        List<DocumentRef> newRefs = resolveRefs(filename, "related archives", oldRefs);
        archiver.setRelatedArchives(newRefs);
        for (DocumentRef archiveRef : newRefs) {
          Reference targetRef = new Reference(ATLGArchive.class, archiveRef.getId());
          Relation relation = new Relation(sourceRef, isCreatorRef, targetRef);
          addDocument(Relation.class, relation, true);
        }
      }
      oldRefs = archiver.getRelatedArchivers();
      if (oldRefs != null && oldRefs.size() != 0) {
        archiver.setRelatedArchivers(resolveRefs(filename, "related archivers", oldRefs));
      }
      modDocument(ATLGArchiver.class, archiver);
    }
  }

  // -------------------------------------------------------------------
  // --- Data model defined in ING Forms -------------------------------
  // -------------------------------------------------------------------

  public static class XKeyword {
    /** ### Assigned id (admin) */
    public String _id;

    public String type;

    public String label;

    public String onderwerp;

    public String regionaam;

    @Override
    public String toString() {
      return String.format("%-5s %-10s %-40s %-30s %s", _id, type, onderwerp, regionaam, label);
    }
  }

  // -------------------------------------------------------------------

  public static class XPerson {
    /** ### Assigned id (admin) */
    public String _id;

    public String type;

    public String voorl;

    public String tussenv;

    public String achternaam;

    public String toevoeging;

    public String[] verwijzing;

    public String[] label;
  }

  // -------------------------------------------------------------------

  // TODO remove markup - various items contain paragraph tags
  public static class Wetgeving {
    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Reference" */
    public String reference;

    /** "Pages" */
    public String pages;

    /** "Short title" */
    public String titel;

    /** "English title" */
    public String titel_eng;

    // TODO Provide more meaningful names
    /** "Date" and "Date 2" */
    public XDates dates;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) Group classification" */
    public String[] keywords;

    /** "Keyword(s) other subject" */
    public String[] keywords_extra;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Summary of contents" */
    public String contents;

    /** "See also" */
    public XSeeAlso[] see_also;

    /** "Earlier/later publications" */
    public String[] other_publication;

    /** "Original archival source" */
    public String original_archival_source;

    /** "Link archival database" */
    public String link_archival_dbase;

    /** "Remarks" */
    public String remarks;

    /** "Scan" */
    public String scan;

    /** "Parts to scan" */
    public String partstoscan;

    /** "Record made by-" */
    public String made_by;

    /** "Reminders" */
    public String Aantekeningen;

    // TODO Check: never used
    /** "Binnenkomende relaties" */
    public XRelated[] related;
  }

  public static class WetgevingEntry {
    public Wetgeving wetgeving;
  }

  // -------------------------------------------------------------------

  public static class ArchiefMat {
    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Ref. code country" ??? */
    public String[] countries;

    /** "Ref. code repository" ??? */
    public String rf_archive;

    /** "Reference code" */
    public String ref_code;

    /** "Code or indication of sub-fonds" */
    public String code_subfonds;

    /** "Indication of series, Nos." */
    public String series;

    /** "Item, No." */
    public String itemno;

    /** "Title" */
    public String titel;

    /** "English title" */
    public String titel_eng;

    /** "Begin date" and "End date" */
    public XPeriod dates;

    /** "Period description" */
    public String period_description;

    /** "Extent" */
    public String extent;

    /** "Additional finding aid" */
    public String finding_aid;

    /** "Name(s) of Creator(s)" */
    public String[] creators;

    /** "Scope and content" */
    public String scope;

    /** "Title(s) related underlying level(s) of description" ??? */
    public String relation;

    /** "Other related units of description" ??? */
    public String em;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) subject" */
    public String[] keywords;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Remarks" ??? */
    public String notes;

    /** "Record made by-" */
    public String made_by;

    /** "Reminders" ??? */
    public String Aantekeningen;

    /** "Binnenkomende relaties" ??? */
    public XRelated[] related;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(_id);
      if (related != null) {
        for (XRelated entry : related) {
          builder.append(" ").append(entry.type);
        }
      }
      return builder.toString();
    }
  }

  public static class ArchiefMatEntry {
    public ArchiefMat archiefmat;
  }

  // -------------------------------------------------------------------

  public static class Creator {
    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Name" */
    public String name;

    /** "English name" */
    public String name_english;

    /** ### "Begin date" and "End date" */
    public XPeriod dates;

    /** "Period description" */
    public String period_description;

    /** "History/functions/occupations/activities" */
    public String his_func;

    /** "Title(s) related archive(s)" */
    public String[] related_archives;

    /** "Title(s) related creator(s)" */
    public String[] related_creators;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) subject" */
    public String[] keywords;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Remarks" */
    public String notes;

    /** "Literature" */
    public String literatuur;

    /** "Record made by-" */
    public String made_by;

    /** "Reminders" */
    public String Aantekeningen;

    /** "Binnenkomende relaties" */
    public XRelated[] related;

    /** ??? ("person", "family") */
    public String[] types;
  }

  public static class CreatorEntry {
    public Creator creator;
  }

  // -------------------------------------------------------------------

  private static class XDates {
    public String date1;
    public String date2;
  }

  private static class XPeriod {
    public String begin_date;
    public String end_date;
  }

  private static class XSeeAlso {
    public String ref_id;
    public String text_line;

    @Override
    public String toString() {
      return (ref_id == null) ? text_line : String.format("%s: %s", ref_id, text_line);
    }
  }

  // --- property graphs - primitive -----------------------------------

  public static class Graph {
    private final Map<DocumentRef, Vertex> vertices = Maps.newHashMap();

    public Collection<Vertex> getVertices() {
      return vertices.values();
    }

    private Vertex getVertex(DocumentRef ref) {
      Vertex vertex = vertices.get(ref);
      if (vertex == null) {
        vertex = new Vertex(ref);
        vertices.put(ref, vertex);
      }
      return vertex;
    }

    public void addEdge(DocumentRef srce, DocumentRef dest, String type) {
      getVertex(srce).getEdgeTo(dest).addType(type);
    }

    public void addEdge(DocumentRef srce, DocumentRef dest, Set<String> types) {
      getVertex(srce).getEdgeTo(dest).setTypes(types);
    }
  }

  public static class Vertex {
    private final Map<DocumentRef, Edge> adjacencies = Maps.newHashMap();
    private final DocumentRef ref;

    public Vertex(DocumentRef ref) {
      this.ref = ref;
    }

    public DocumentRef getDocumentRef() {
      return ref;
    }

    public Collection<Edge> getEdges() {
      return adjacencies.values();
    }

    public Edge getEdgeTo(DocumentRef dest) {
      Edge edge = adjacencies.get(dest);
      if (edge == null) {
        edge = new Edge(dest);
        adjacencies.put(dest, edge);
      }
      return edge;
    }
  }

  public static class Edge {
    private Set<String> types = Sets.newHashSet();
    private final DocumentRef dest;

    public Edge(DocumentRef dest) {
      this.dest = dest;
    }

    public DocumentRef getDest() {
      return dest;
    }

    public Set<String> getTypes() {
      return types;
    }

    public boolean containsType(String type) {
      return types.contains(type);
    }

    public void setTypes(Set<String> types) {
      this.types = types;
    }

    public void addType(String type) {
      types.add(type);
    }
  }

}
