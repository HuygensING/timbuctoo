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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Collective;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.Document.DocumentType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.neww.WWCollective;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWKeyword;
import nl.knaw.huygens.timbuctoo.model.neww.WWLanguage;
import nl.knaw.huygens.timbuctoo.model.neww.WWLocation;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.model.neww.WWRelation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Imports data of the "New European Women Writers" project.
 * 
 * Usage:
 *  java  -cp  [specs]  ${package-name}.WomenWritersImporter  [importDirName]
 */
public class WomenWritersImporter extends WomenWritersDefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(WomenWritersImporter.class);

  public static void main(String[] args) throws Exception {

    // Handle commandline arguments
    String directory = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/neww/";

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      long start = System.currentTimeMillis();

      storageManager = injector.getInstance(StorageManager.class);
      indexManager = injector.getInstance(IndexManager.class);

      TypeRegistry registry = injector.getInstance(TypeRegistry.class);
      RelationManager relationManager = new RelationManager(storageManager);
      WomenWritersImporter importer = new WomenWritersImporter(registry, storageManager, relationManager, indexManager, directory);

      importer.importAll();

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

    } catch (Exception e) {
      // for debugging
      e.printStackTrace();
    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  /** References of stored primitive entities */
  private final Map<String, Reference> references = Maps.newHashMap();
  /** Keys of invalid primitive entities */
  private final Set<String> invalids = Sets.newHashSet();
  /** For deserializing JSON */
  private final ObjectMapper objectMapper;
  private final RelationManager relationManager;
  private final File inputDir;

  public WomenWritersImporter(TypeRegistry registry, StorageManager storageManager, RelationManager relationManager, IndexManager indexManager, String inputDirName) {
    super(registry, storageManager, indexManager);
    objectMapper = new ObjectMapper();
    this.relationManager = relationManager;
    inputDir = new File(inputDirName);
    if (inputDir.isDirectory()) {
      System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
    } else {
      System.out.printf("%n.. Not a directory: %s%n", inputDir.getAbsolutePath());
    }
  }

  public void importAll() throws Exception {
    printBoxedText("Initialization");

    removeNonPersistentEntities(WWCollective.class);
    removeNonPersistentEntities(WWDocument.class);
    removeNonPersistentEntities(WWKeyword.class);
    removeNonPersistentEntities(WWPerson.class);
    removeNonPersistentEntities(WWRelation.class);

    importRelationTypes();
    setupRelationDefs();

    printBoxedText("Import");

    System.out.println(".. Languages");
    System.out.printf("Number = %6d%n%n", importLanguages());

    System.out.println(".. Locations");
    System.out.printf("Number = %6d%n%n", importLocations());

    System.out.println(".. Keywords");
    System.out.printf("Number = %6d%n%n", importKeywords());

    System.out.println(".. Collectives");
    System.out.printf("Number = %6d%n%n", importCollectives());

    System.out.println(".. Documents");
    System.out.printf("Number = %6d%n%n", importDocuments());

    System.out.println(".. Persons");
    System.out.printf("Number = %6d%n%n", importPersons());

    System.out.printf("nUnknown    %5d%n", nUnknown);
    System.out.printf("nArchetype  %5d%n", nArchetype);
    System.out.printf("nAuthor     %5d%n", nAuthor);
    System.out.printf("nPseudonym  %5d%n", nPseudonym);
    System.out.printf("nDuplicates %5d%n", nDuplicates);

    System.out.println(".. Relations");
    importRelations();
    System.out.printf("Number of missing relation types = %6d%n", missingRelationTypes);
    System.out.printf("Number of unstored relations     = %6d%n", unstoredRelations);
    System.out.printf("Number of duplicate relations    = %6d%n", relationManager.getDuplicateRelationCount());

    printBoxedText("Indexing");

    indexEntities(WWCollective.class);
    indexEntities(WWDocument.class);
    indexEntities(WWKeyword.class);
    indexEntities(WWLanguage.class);
    indexEntities(WWPerson.class);
    indexEntities(WWLocation.class);

    displayStatus();
    displayErrorSummary();
  }

  // --- Support ---------------------------------------------------------------

  /** Returns key for a reference map. */
  private String newKey(String name, String id) {
    return name + ":" + id;
  }

  private Reference storeReference(String key, Class<? extends DomainEntity> type, String id) {
    Reference reference = newDomainEntityReference(type, id);
    references.put(key, reference);
    return reference;
  }

  private LineIterator getLineIterator(String filename) throws IOException {
    File file = new File(inputDir, filename);
    return FileUtils.lineIterator(file, "UTF-8");
  }

  private String preprocessJson(String line) {
    line = StringUtils.stripToEmpty(line);
    if (line.startsWith("{")) {
      line = line.replaceAll("\"_id\"", "\"tempid\"");
      line = line.replaceAll("ObjectId\\(\\s*(\\S+)\\s*\\)", "$1");
      return line;
    } else {
      System.out.println("## Skipping line: " + line);
      return "";
    }
  }

  private void verifyEmptyField(String line, String key, String value) {
    if (!Strings.isNullOrEmpty(value)) {
      System.out.println("Unexpected value for: " + key);
      System.out.println(line);
    }
  }

  private String verifyNonEmptyField(String line, String key, String value) {
    if (Strings.isNullOrEmpty(value)) {
      handleError("Missing '%s' in: %s", key, line);
    }
    return value;
  }

  // --- Collectives -----------------------------------------------------------

  private int importCollectives() throws Exception {
    int initialSize = references.size();
    LineIterator iterator = getLineIterator("collectives.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleCollective(line);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
    return references.size() - initialSize;
  }

  private void handleCollective(String json) throws Exception {
    XCollective object = objectMapper.readValue(json, XCollective.class);
    String key = newKey("Collective", object.tempid);
    if (references.containsKey(key)) {
      handleError("Duplicate id %s", key);
    } else {
      WWCollective converted = convert(json, object);
      if (converted == null) {
        invalids.add(key);
      } else {
        String storedId = addDomainEntity(WWCollective.class, converted);
        storeReference(key, WWCollective.class, storedId);
      }
    }
  }

  private WWCollective convert(String line, XCollective object) {
    WWCollective converted = new WWCollective();

    String name = verifyNonEmptyField(line, "name", filterField(object.name));
    if (name == null) {
      return null;
    }
    converted.setName(name);

    String type = verifyNonEmptyField(line, "type", filterField(object.type));
    converted.tempType = type;
    if (type == null || type.equals("membership")) {
      converted.setType(Collective.Type.UNKNOWN);
    } else {
      converted.setType(type);
    }

    converted.tempLocationPlacename = filterField(object.location_placename);
    converted.tempOrigin = filterField(object.origin);

    String shortName = converted.tempShortName = filterField(object.short_name);
    if (shortName != null && shortName.matches("[A-Z]{2,6}")) {
      converted.setAcronym(shortName);
    }

    String notes = filterField(object.notes);
    if (notes != null && !notes.equals(shortName)) {
      converted.setNotes(filterNotesField(object.notes));
    }

    String url = filterField(object.url);
    if (url != null) {
      converted.addLink(new Link(url, url));
    }

    return converted;
  }

  public static class XCollective {
    public String tempid;
    public String email;
    public String location_id; // needed for relation, ignore
    public String location_placename; // store temporarily
    public String name; // 4 entries without a name, but they occur in relations
    public String notes; // used. how do we handle whitespace?
    public int old_id; // ignore
    public String origin; // seems to be country. isn't this implied by location?
    public String original_field; // ignore
    public String original_table; // ignore
    public String short_name;
    public String telephone;
    public String type; // 'library', 'membership'
    public String url;
  }

  // --- Documents -------------------------------------------------------------

  private PublisherNormalizer publisherNormalizer;

  private int importDocuments() throws Exception {
    int initialSize = references.size();
    documentTypeMap = createDocumentTypeMap();
    File file = new File(inputDir, "publishers.txt");
    publisherNormalizer = new PublisherNormalizer(file);

    LineIterator iterator = getLineIterator("documents.json");
    String line = "";
    try {
      while (iterator.hasNext()) {
        line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleDocument(preprocessDocumentJson(line));
        }
      }
    } catch (JsonMappingException e) {
      System.out.println(line);
      throw e;
    } finally {
      LineIterator.closeQuietly(iterator);
      publisherNormalizer = null;
    }
    return references.size() - initialSize;
  }

  private Map<String, DocumentType> documentTypeMap;

  private Map<String, DocumentType> createDocumentTypeMap() {
    Map<String, DocumentType> map = Maps.newHashMap();
    map.put("Article", DocumentType.ARTICLE);
    map.put("Catalogue", DocumentType.CATALOGUE);
    map.put("List", DocumentType.UNKNOWN);
    map.put("Picture", DocumentType.PICTURE);
    map.put("Publicity", DocumentType.PUBLICITY);
    map.put("TBD", DocumentType.UNKNOWN);
    map.put("To Be Done", DocumentType.UNKNOWN);
    map.put("To be done", DocumentType.UNKNOWN);
    map.put("Work", DocumentType.WORK);
    map.put("work", DocumentType.WORK);
    return map;
  }

  private String preprocessDocumentJson(String text) {
    text = text.replaceAll("\"libraries\" : \"\"", "\"libraries\" : null");
    text = text.replaceAll("\"prints\" : \"\"", "\"prints\" : null");
    text = text.replaceAll("\"source\" : \"\"", "\"source\" : null");
    text = text.replaceAll("\"subject\" : \"\"", "\"subject\" : null");
    text = text.replaceAll("\"subject\" : \\[\\]", "\"subject\" : null");
    text = text.replaceAll("\"topoi\" : \"\"", "\"topoi\" : null");
    text = text.replaceAll("\"topoi\" : \"\"", "\"topoi\" : null");
    text = text.replaceAll("\"type\" : \"\"", "\"type\" : \"TBD\"");
    text = text.replaceAll("\"url\" : \"\", \"url_title\" : \"\"", "\"urls\" : null");
    text = text.replaceAll("\"urls\" : \"\"", "\"urls\" : null");
    return text;
  }

  private void handleDocument(String json) throws Exception {
    XDocument object = objectMapper.readValue(json, XDocument.class);
    String key = newKey("Document", object.tempid);
    if (references.containsKey(key)) {
      handleError("Duplicate key %s", key);
    } else {
      WWDocument converted = convert(json, object);
      if (converted != null) {
        String storedId = addDomainEntity(WWDocument.class, converted);
        Reference reference = storeReference(key, WWDocument.class, storedId);
        handlePublisher(extractPrints(object), converted.getDate(), reference);
      }
    }
  }

  private WWDocument convert(String line, XDocument object) {
    WWDocument converted = new WWDocument();
    converted.setResourceType(Document.ResourceType.TEXT);

    String type = filterField(object.type);
    converted.setDocumentType(toDocumentType(type));

    String title = filterField(object.title);
    converted.setTitle(title != null ? title : "[Untitled]");

    converted.setDescription(filterField(object.description));

    String date = filterField(object.date);
    if (date != null) {
      try {
        converted.setDate(new Datable(date));
      } catch (RuntimeException e) {
        handleError("Illegal 'date' in %s", line);
      }
    }

    converted.tempOrigin = filterField(object.origin);
    converted.setReference(filterField(object.reference));

    // the keywords are not normalized: identical topoi occur as different items
    if (object.topoi != null && object.topoi.length != 0) {
      for (String[] topos : object.topoi) {
        if (topos[0] != null) {
          converted.addTopos(topos[0]);
        }
      }
    }

    StringBuilder notesBuilder = new StringBuilder();
    appendTo(notesBuilder, filterNotesField(object.notes), "");

    List<XPrint> prints = extractPrints(object);
    for (XPrint print : prints) {
      appendTo(notesBuilder, "* Print", NEWLINE);
      appendTo(notesBuilder, print.edition, NEWLINE + "Edition: ");
      appendTo(notesBuilder, print.publisher, NEWLINE + "Publisher: ");
      appendTo(notesBuilder, print.location, NEWLINE + "Location: ");
      appendTo(notesBuilder, print.year, NEWLINE + "Year: ");
    }
    if (selectFirstEdition(prints, date) != null) {
      converted.setEdition("1");
    }

    if (object.source != null) {
      appendTo(notesBuilder, "* Source", NEWLINE);
      appendTo(notesBuilder, filterField(object.source.type), NEWLINE + "Type: ");
      appendTo(notesBuilder, filterField(object.source.full_name), NEWLINE + "Full Name: ");
      appendTo(notesBuilder, filterField(object.source.short_name), NEWLINE + "Short Name: ");
      appendTo(notesBuilder, filterField(object.source.notes), NEWLINE + "Notes: ");
    }

    converted.setNotes(notesBuilder.toString());

    if (object.urls != null) {
      for (Map.Entry<String, String> entry : object.urls.entrySet()) {
        String label = filterField(entry.getKey());
        String url = filterField(entry.getValue());
        converted.addLink(new Link(url, label));
      }
    }

    converted.tempCreator = filterField(object.creator);
    converted.tempLanguage = filterField(object.language);

    if ("work".equalsIgnoreCase(type) && "works".equals(object.original_table) && object.old_id != 0) {
      converted.tempOldId = Integer.toString(object.old_id);
    }

    return converted.isValid() ? converted : null;
  }

  public DocumentType toDocumentType(String type) {
    DocumentType documentType = (type != null) ? documentTypeMap.get(type) : null;
    return (documentType != null) ? documentType : DocumentType.UNKNOWN ;
  }

  private List<XPrint> extractPrints(XDocument object) {
    List<XPrint> prints = Lists.newArrayList();
    if (object.prints != null && object.prints.size() != 0) {
      // order by key
      Map<String, XPrint> temp = Maps.newTreeMap();
      temp.putAll(object.prints);
      for (Map.Entry<String, XPrint> entry : temp.entrySet()) {
        XPrint filtered = new XPrint();
        filtered.edition = filterField(entry.getValue().edition);
        filtered.publisher = filterField(entry.getValue().publisher);
        filtered.location = filterField(entry.getValue().location);
        filtered.year = filterField(entry.getValue().year);
        prints.add(filtered);
      }
    }
    return prints;
  }

  private static final String IS_PUBLISHED_BY = "isPublishedBy";

  private void handlePublisher(List<XPrint> prints, Datable datable, Reference documentRef) throws ValidationException {
    String date = (datable != null) ? datable.toString() : null;
    XPrint first = selectFirstEdition(prints, date);
    if (first != null) {
      String name = first.publisher;
      if (name != null && !name.isEmpty()) {
        name = publisherNormalizer.normalize(name);
        if (name != null && !name.isEmpty()) {
          String key = newKey("Publisher", name);
          Reference publisherRef = references.get(key);
          if (publisherRef == null) {
            WWCollective collective = new WWCollective();
            collective.setType(Collective.Type.PUBLISHER);
            collective.setName(name);
            collective.tempLocationPlacename = first.location;
            String storedId = addDomainEntity(WWCollective.class, collective);
            publisherRef = storeReference(key, WWCollective.class, storedId);
          }
          Reference relationRef = relationTypes.get(IS_PUBLISHED_BY);
          storeRelation(WWRelation.class, documentRef, relationRef, publisherRef, change, "");
        }
      }
    }
  }

  private XPrint selectFirstEdition(List<XPrint> prints, String year) {
    XPrint selected = null;
    if (prints != null && !prints.isEmpty() && year != null) {
      int best = 0;
      for (XPrint print : prints) {
        int score = (edition(print) == 1) ? 1 : 0;
        if (year.equals(print.year)) {
          score++;
        }
        if (score == best) {
          // not unique
          selected = null;
        } else if (score > best) {
          selected = print;
          best = score;
        }
      }
    }
    return selected;
  }

  private int edition(XPrint print) {
    int n = 0;
    String text = print.edition;
    if (text != null) {
      for (char ch : text.toCharArray()) {
        if (Character.isDigit(ch)) {
          n = 10 * n + (ch - '0');
        } else
          break;
      }
    }
    return n;
  }

  public static class XDocument {
    public String tempid;
    public String creator; // ignore
    public String creator_id; // object id of creator - must occur in relations
    public String date; // convert to datable
    public String description; // text
    public String language; // ignore
    public String language_id; // object id of language - must occur in relations
    public String[] libraries; // list of library id's - must occur in relations
    public String notes; // text
    public int old_id; // record number in NEWW database
    public String origin; // item of a list of countries. BUT origin of what?
    public String original_table; // table in NEWW database
    public Map<String, XPrint> prints; // printed editions
    public String reference; // text, sparse, unstructured
    public XSource source;
    public String[][] subject; // a list of subject keywords (text, id) - must occur in relations
    public String title; // text
    public String[][] topoi; // a list of topoi keywords (text, id) - must occur in relations
    public String type; // 'Article', 'Catalogue', 'List', 'Picture', 'Publicity', 'TBD', 'Work'
    public Map<String, String> urls; // convert to Link
    public String url;
    public String url_title; // convert to Link
  }

  public static class XSource {
    public String notes;
    public String type;
    public String full_name;
    public String short_name;

    @Override
    public String toString() {
      return String.format("%s | %s | %s | %s%n", notes, type, full_name, short_name);
    }
  }

  public static class XPrint {
    public String edition;
    public String publisher;
    public String location;
    public String year;
  }

  // --- Keywords --------------------------------------------------------------

  // Used for handling multiple occurrences of key values
  private final Map<String, String> keywordValueIdMap = Maps.newHashMap();
  private final Set<String> toposIds = Sets.newHashSet();

  private int importKeywords() throws Exception {
    int initialSize = references.size();
    LineIterator iterator = getLineIterator("keywords.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleKeyword(line);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
    return references.size() - initialSize;
  }

  private void handleKeyword(String json) throws Exception {
    XKeyword object = objectMapper.readValue(json, XKeyword.class);
    String key = newKey("Keyword", object.tempid);
    if (references.containsKey(key)) {
      handleError("Duplicate key %s", key);
    } else {
      WWKeyword converted = convert(json, object);
      if (converted != null) {
        String value = converted.getValue();
        String storedId = keywordValueIdMap.get(value);
        if (storedId == null) {
          storedId = addDomainEntity(WWKeyword.class, converted);
          keywordValueIdMap.put(value, storedId);
        }
        storeReference(key, WWKeyword.class, storedId);
      }
    }
  }

  private WWKeyword convert(String line, XKeyword object) {
    WWKeyword converted = new WWKeyword();

    converted.setType(filterField(object.type));
    verifyNonEmptyField(line, "type", converted.getType());

    converted.setValue(filterField(object.keyword));
    verifyNonEmptyField(line, "keyword", converted.getValue());

    // We only store keywords of type "genre"
    // Relations with keywords are of type "hasGenre"
    if ("topos".equals(converted.getType())) {
      invalids.add(newKey("Keyword", object.tempid));
      toposIds.add(object.tempid);
      return null;
    } else if (!"genre".equals(converted.getType())) {
      handleError("Unexpected type", converted.getType());
      converted.setValue(null);
    }

    return converted.isValid() ? converted : null;
  }

  public static class XKeyword {
    public String tempid;
    public String keyword;
    public int old_id; // ignore
    public String original_table; // ignore
    public String type;
  }

  // --- Languages -------------------------------------------------------------

  // The imported data contains ISO639-2 codes and names that are not mapped.
  // The simplest strategy is to implement our own mapping from name to code.

  /**
   * Creates a mapping from language names to ISO639-3 language codes.
   */
  private Map<String, String> createNameCodeMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("Albanian", "sqi");
    map.put("Arabic", "ara");
    map.put("Armenian", "hye");
    map.put("Atticism", "ell");
    map.put("Basque", "eus");
    map.put("Breton", "bre");
    map.put("Bulgarian", "bul");
    map.put("Catalan", "cat");
    map.put("Chinese", "zho");
    map.put("Croatian", "hrv");
    map.put("Czech", "ces");
    map.put("Danish", "dan");
    map.put("Dutch", "nld");
    map.put("English", "eng");
    map.put("Esperanto", "epo");
    map.put("Estonian", "est");
    map.put("Finnish", "fin");
    map.put("French", "fra");
    map.put("Frisian", "fry");
    map.put("Galician", "glg");
    map.put("German", "deu");
    map.put("Greek", "ell");
    map.put("Hebrew", "hbo");
    map.put("Hungarian", "hun");
    map.put("Icelandic", "isl");
    map.put("Irish Gaelic", "gle");
    map.put("Italian", "ita");
    map.put("Japanese", "jpn");
    map.put("Latin", "lat");
    map.put("Lithuanian", "lit");
    map.put("Norwegian", "nno");
    map.put("Occitan", "oci");
    map.put("Ottoman Turkish", "ota");
    map.put("Persian", "fas");
    map.put("Polish", "pol");
    map.put("Portuguese", "por");
    map.put("Romanian", "ron");
    map.put("Russian", "rus");
    map.put("Serbian", "srp");
    map.put("Slavo-Serbian", "srp");
    map.put("Slovak", "slk");
    map.put("Slovenian", "slv");
    map.put("Sorbian language", "srp");
    map.put("Spanish", "spa");
    map.put("Swedish", "swe");
    map.put("Turkish", "tur");
    map.put("Ukrainian", "ukr");
    map.put("Uzbek", "uzb");
    return map;
  }

  private String mapName(Map<String, String> map, String name) {
    if (Strings.isNullOrEmpty(name)) {
      return "?";
    } else {
      String code = map.get(name);
      return (code != null) ? code : "?";
    }
  }

  private int importLanguages() throws Exception {
    int initialSize = references.size();
    Map<String, String> map = createNameCodeMap();
    LineIterator iterator = getLineIterator("languages.json");
    String line = "";
    try {
      System.out.printf("\"language\",\"ISO-code\",\"ISO-name\"%n");
      while (iterator.hasNext()) {
        line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          XLanguage object = objectMapper.readValue(line, XLanguage.class);
          String name = verifyNonEmptyField(line, "name", filterField(object.name));
          if (name == null) {
            invalids.add(newKey("Language", object.tempid));
          } else {
            String code = mapName(map, name);
            // Get WWLanguage instance with values of primitive entity Language
            WWLanguage language = storageManager.findEntity(WWLanguage.class, "^code", code);
            if (language == null) {
              verifyNonEmptyField(line, "name", null);
            } else {
              String flag = name.equals(language.getName()) ? "" : "  *";
              System.out.printf("%-30s%-8s%-30s%s%n", name, language.getCode(), language.getName(), flag);
              language.setCore(true);
              // TODO prevent multiple updates for same language
              updateDomainEntity(WWLanguage.class, language);
              String key = newKey("Language", object.tempid);
              storeReference(key, WWLanguage.class, language.getId());
            }
          }
        }
      }
    } catch (JsonMappingException e) {
      System.out.println(line);
      throw e;
    } finally {
      LineIterator.closeQuietly(iterator);
    }
    return references.size() - initialSize;
  }

  public static class XLanguage {
    public String db_name;
    public String tempid;
    public String ISO639_1Code;
    public String ISO639_2Code;
    public String name;
    public int old_id; // ignore
    public String original_table; // ignore
  }

  // --- Locations -------------------------------------------------------------

  private final Map<String, String> conc = Maps.newHashMap();

  private void setupConc() {
    conc.put("alton, hampshire#england", "se:alton.eng");
    conc.put("amsterdam#netherlands", "se:amsterdam.nld");
    conc.put("amsterdam#null", "se:amsterdam.nld");
    conc.put("andros#greece", "re:andros.grc");
    conc.put("athens#greece", "se:athens.grc");
    conc.put("barcelona#spain", "se:barcelona.esp");
    conc.put("belgrade#null", "se:belgrade.srb");
    conc.put("belgrade#serbia", "se:belgrade.srb");
    conc.put("bergen#norway", "se:bergen.nor");
    conc.put("berlin#germany", "se:berlin.deu");
    conc.put("bialystok#poland", "se:bialystok.pol");
    conc.put("bologna#italy", "se:bologna.ita");
    conc.put("bruxelles#belgium/southern netherlands", "se:brussel.bel");
    conc.put("bucharest#romania", "se:bucharest.rou");
    conc.put("budapest#hungary", "se:budapest.hun");
    conc.put("cambridge#england", "se:cambridge.eng");
    conc.put("cesena#italy", "se:cesena.ita");
    conc.put("copenhagen#denmark", "se:kobenhavn.dnk");
    conc.put("den haag#netherlands", "se:den-haag.nld");
    conc.put("deventer#netherlands", "se:deventer.nld");
    conc.put("dublin#ireland", "se:dublin.irl");
    conc.put("dublin#null", "se:dublin.irl");
    conc.put("firenze#italy", "se:firenze.ita");
    conc.put("gothenburg#sweden", "se:goteborg.swe");
    conc.put("groningen#null", "se:groningen.nld");
    conc.put("helsinki#finland", "se:helsinki.fin");
    conc.put("ioannina#greece", "se:ioannina.grc");
    conc.put("kampen#netherlands", "se:kampen.nld");
    conc.put("kopenhagen#denmark", "se:kobenhavn.dnk");
    conc.put("koper#slovenia", "se:koper.svn");
    conc.put("krakow#poland", "se:krakow.pol");
    conc.put("leeuwarden#netherlands", "se:leeuwarden.nld");
    conc.put("leiden#netherlands", "se:leiden.nld");
    conc.put("leiden#null", "se:leiden.nld");
    conc.put("leipzig and frankfurt am main#germany", "IGNORE");
    conc.put("lisboa#portugal", "se:lisbon.prt");
    conc.put("lisbon#portugal", "se:lisbon.prt");
    conc.put("ljubljana#slovenia", "se:ljubljana.svn");
    conc.put("londen#england", "se:london.eng");
    conc.put("london#england", "se:london.eng");
    conc.put("lublin#poland", "se:lublin.pol");
    conc.put("madrid#spain", "se:madrid.esp");
    conc.put("middelburg#netherlands", "se:middelburg.nld");
    conc.put("milan#italy", "se:milano.ita");
    conc.put("milano#italy", "se:milano.ita");
    conc.put("milano#italy", "se:milano.ita");
    conc.put("milano#null", "se:milano.ita");
    conc.put("mölndal#sweden", "se:molndal.swe");
    conc.put("moscow#russia", "se:moscow.rus");
    conc.put("münchen#germany", "se:munchen.deu");
    conc.put("napoli#italy", "se:napoli.ita");
    conc.put("nicosia#null", "se:nicosia.cyp");
    conc.put("nijmegen#netherlands", "se:nijmegen.nld");
    conc.put("northfield, minnesota#united states", "se:northfield.usa");
    conc.put("novi sad#serbia", "se:novi-sad.srb");
    conc.put("null#albania", "co:alb");
    conc.put("null#argentina", "co:arg");
    conc.put("null#australia", "co:aus");
    conc.put("null#austria", "co:aut");
    conc.put("null#austro-hungarian empire", "bl:emp-austro-hungarian");
    conc.put("null#bohemia", "re:bohemia.cze");
    conc.put("null#belgium/southern netherlands", "co:bel");
    conc.put("null#bosnia and herzegovina", "co:bih");
    conc.put("null#brasil", "co:bra");
    conc.put("null#bulgaria", "co:bgr");
    conc.put("null#byzantine empire", "bl:emp-byzantine");
    conc.put("null#canada", "co:can");
    conc.put("null#castilia", "re:castilia.esp");
    conc.put("null#china", "co:chn");
    conc.put("null#colombia", "co:col");
    conc.put("null#colonies of european countries", "IGNORE");
    conc.put("null#croatia", "co:hrv");
    conc.put("null#czech republic", "co:cze");
    conc.put("null#denmark", "co:dnk");
    conc.put("null#egypt", "co:egy");
    conc.put("null#england", "co:eng");
    conc.put("null#england", "co:eng");
    conc.put("null#estonia", "co:est");
    conc.put("null#finland", "co:fin");
    conc.put("null#france", "co:fra");
    conc.put("null#germany", "co:deu");
    conc.put("null#greece", "co:grc");
    conc.put("null#hungary", "co:hun");
    conc.put("null#iceland", "co:isl");
    conc.put("null#india", "co:ind");
    conc.put("null#ireland", "co:irl");
    conc.put("null#italy", "co:ita");
    conc.put("null#italy", "re:emilia-romagna.ita");
    conc.put("null#japan", "co:jpn");
    conc.put("null#kingdom of sardinia - piedmont", "IGNORE");
    conc.put("null#latvia", "co:lva");
    conc.put("null#lithuania", "co:ltu");
    conc.put("null#luxemburg", "co:lux");
    conc.put("null#mexico", "co:mex");
    conc.put("null#montenegro", "co:mne");
    conc.put("null#netherlands", "co:nld");
    conc.put("null#norway", "co:nor");
    conc.put("null#null", "IGNORE");
    conc.put("null#ottoman empire", "bl:emp-ottoman");
    conc.put("null#poland", "co:pol");
    conc.put("null#portugal", "co:prt");
    conc.put("null#romania", "co:rou");
    conc.put("null#russia", "co:rus");
    conc.put("null#scotland", "co:sco");
    conc.put("null#serbia", "co:srb");
    conc.put("null#slovakia", "co:svk");
    conc.put("null#slovenia", "co:svn");
    conc.put("null#south-america (to be specified)", "bl:south-america");
    conc.put("null#spain", "co:esp");
    conc.put("null#sweden", "co:swe");
    conc.put("null#switzerland", "co:che");
    conc.put("null#turkey", "co:tur");
    conc.put("null#ukraine", "co:ukr");
    conc.put("null#united states", "co:usa");
    conc.put("null#unknown / not relevant", "IGNORE");
    conc.put("null#uzbekistan", "co:uzb");
    conc.put("null#wales", "re:wales.gbr");
    conc.put("oslo#norway", "se:oslo.nor");
    conc.put("oviedo#spain", "se:oviedo.esp");
    conc.put("oxford#england", "se:oxford.eng");
    conc.put("parijs#france", "se:paris.fra");
    conc.put("parijs#null", "se:paris.fra");
    conc.put("paris#france", "se:paris.fra");
    conc.put("patra#greece", "se:patras.grc");
    conc.put("patras#greece", "se:patras.grc");
    conc.put("petersburg#russia", "se:saint-petersburg.rus");
    conc.put("porto#portugal", "se:porto.prt");
    conc.put("poznań, poznan#poland", "se:poznan.pol");
    conc.put("poznań#poland", "se:poznan.pol");
    conc.put("praha 1#czech republic", "se:prague.cze");
    conc.put("rethymno#greece", "se:rethymno.grc");
    conc.put("rotterdam#null", "se:rotterdam.nld");
    conc.put("san francisco#united states", "se:san-francisco.usa");
    conc.put("sofia#bulgaria", "se:sofia.bgr");
    conc.put("st. petersburg#russia", "se:saint-petersburg.rus");
    conc.put("st.petersburg#russia", "se:saint-petersburg.rus");
    conc.put("the hague#netherlands", "se:den-haag.nld");
    conc.put("thessaloniki#greece", "se:thessaloniki.grc");
    conc.put("tilburg#netherlands", "se:tilburg.nld");
    conc.put("toledo#spain", "se:toledo.esp");
    conc.put("torino#italy", "se:torino.ita");
    conc.put("turku#finland", "se:turku.fin");
    conc.put("university of helsinki#finland", "se:helsinki.fin");
    conc.put("utrecht#null", "se:utrecht.nld");
    conc.put("vatican city#italy", "co:vat");
    conc.put("volda#norway", "se:volda.nor");
    conc.put("warsaw#poland", "se:warszawa.pol");
    conc.put("warszawa#poland", "se:warszawa.pol");
    conc.put("washington, dc#united states", "se:washington.usa");
    conc.put("xxx#brasil", "co:bra");
    conc.put("warszawa#poland", "se:warszawa.pol");
  }

  private int importLocations() throws Exception {
    setupConc();
    int initialSize = references.size();
    LineIterator iterator = getLineIterator("locations.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleLocation(preprocessLocation(line));
        }
      }
    } finally {
      conc.clear();
      LineIterator.closeQuietly(iterator);
    }
    return references.size() - initialSize;
  }

  private String preprocessLocation(String text) {
    return text;
  }

  private void handleLocation(String json) throws Exception {
    XLocation object = objectMapper.readValue(json, XLocation.class);
    String key = newKey("Location", object.tempid);
    if (references.containsKey(key)) {
      handleError("Duplicate key %s", key);
      return;
    }
    WWLocation converted = convert(json, object);
    if (converted == null) {
      invalids.add(key);
      return;
    }
    String code = String.format("%s#%s", converted.tempSettlement, converted.tempCountry);
    String urn = conc.get(code.toLowerCase());
    if (urn == null) {
      System.out.println(".. Adding new location: " + code);
      String storedId = addDomainEntity(WWLocation.class, converted);
      storeReference(key, WWLocation.class, storedId);
      return;
    }
    if (urn.equals("IGNORE")) {
      System.out.println(".. Ignoring location: " + code);
      invalids.add(key);
      return;
    }
    Location location = storageManager.findEntity(Location.class, "^urn", urn);
    if (location == null) {
      handleError("URN not found: %s", urn);
      return;
    }
    converted.setId(location.getId());
    converted.setRev(location.getRev());
    converted.setUrn(location.getUrn());
    converted.setDefLang(location.getDefLang());
    converted.setNames(location.getNames());
    converted.setLatitude(location.getLatitude());
    converted.setLongitude(location.getLongitude());
    updateDomainEntity(WWLocation.class, converted);
    storeReference(key, WWLocation.class, location.getId());
  }

  private WWLocation convert(String line, XLocation object) {
    WWLocation converted = new WWLocation();

    verifyEmptyField(line, "bloc", object.bloc);
    verifyEmptyField(line, "district", object.district);
    verifyEmptyField(line, "geogName", object.geogName);
    verifyEmptyField(line, "houseNumber", object.houseNumber);
    verifyEmptyField(line, "latitude", object.latitude);
    verifyEmptyField(line, "longitude", object.longitude);
    verifyEmptyField(line, "notes", object.notes);
    verifyEmptyField(line, "period", object.period);
    verifyEmptyField(line, "region", object.region);

    converted.tempAddress = filterField(object.address);
    converted.tempSettlement = filterField(object.settlement);
    converted.tempCountry = filterField(object.country);
    converted.tempZipcode = filterField(object.zipcode);

    if (converted.isValid()) {
      return converted;
    } else {
      handleError("Empty location: ", line);
      return null;
    }
  }

  private static class XLocation {
    public String tempid;
    public String address;
    public String bloc; // EMPTY
    public String country;
    public String district; // EMPTY
    public String geogName; // contains settlement
    public String houseNumber; // EMPTY
    public String latitude; // EMPTY
    public String longitude; // EMPTY
    public String notes; // EMPTY
    public String period; // EMPTY
    public String region; // EMPTY
    public String settlement; // EMPTY
    public String zipcode; // 54 values
  }

  // --- Persons ---------------------------------------------------------------

  private final Pattern simpleNamePattern = Pattern.compile("^([A-Z]\\w+), ([A-Z]\\w+)$");
  private final Set<String> excludedNames = Sets.newHashSet("Comtesse", "Madame", "Madamoiselle", "Mejuffrouw", "Mevrouw", "Mme", "Mrs", "Queen", "Vrou");

  // maps line without id to stored id
  private final Map<String, String> lines = Maps.newHashMap();

  private int nUnknown = 0;
  private int nArchetype = 0;
  private int nAuthor = 0;
  private int nPseudonym = 0;
  private int nDuplicates = 0;

  private int importPersons() throws Exception {
    int initialSize = references.size();
    LineIterator iterator = getLineIterator("persons.json");
    String line = "";
    try {
      while (iterator.hasNext()) {
        line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handlePerson(preprocessPerson(line));
        }
      }
    } catch (JsonMappingException e) {
      System.out.println(line);
      throw e;
    } finally {
      LineIterator.closeQuietly(iterator);
    }
    for (String type : types) {
      System.out.printf("type %s%n", type);
    }
    lines.clear();
    return references.size() - initialSize;
  }

  private String preprocessPerson(String text) {
    text = text.replaceAll("\"financials\" : \\[\\]", "\"health\" : null");
    text = text.replaceAll("\"health\" : \\[\\]", "\"health\" : null");
    text = text.replaceAll("\"languages\" : \"\"", "\"languages\" : null");
    text = text.replaceAll("\"url\" : \"\"", "\"url\" : null");
    return text;
  }

  private void handlePerson(String json) throws Exception {
    XPerson object = objectMapper.readValue(json, XPerson.class);
    String key = newKey("Person", object.tempid);
    if (references.containsKey(key)) {
      handleError("Duplicate key %s", key);
    } else {
      WWPerson converted = convert(json, object);
      if (converted == null) {
        handleError("Ignoring invalid record: %s", json);
      } else {
        String line = json.replaceFirst("\"tempid\"\\s*:\\s*\"[^\"]*\",", "");
        String storedId = lines.get(line);
        if (storedId != null) {
          nDuplicates++;
        } else {
          storedId = addDomainEntity(WWPerson.class, converted);
          lines.put(line, storedId);
        }
        storeReference(key, WWPerson.class, storedId);
      }
    }
  }

  private WWPerson convert(String line, XPerson object) {
    String text;
    WWPerson converted = new WWPerson();

    converted.setBibliography(filterField(object.bibliography));

    text = filterField(object.born_in);
    if (text != null) {
      converted.tempBirthPlace = text;
    }

    converted.tempChildren = filterField(object.children);
    converted.tempCollaborations = concatenate(object.collaborations);

    text = filterField(object.dateOfBirth);
    if (text != null) {
      converted.setBirthDate(new Datable(text));
    }

    text = filterField(object.dateOfDeath);
    if (text != null) {
      converted.setDeathDate(new Datable(text));
    }

    converted.tempDeath = filterField(object.death);

    if (object.education != null) {
      for (String item : object.education) {
        converted.addEducation(filterField(item));
      }
    }

    converted.tempFinancialSituation = filterField(object.financial_situation);

    verifyEmptyField(line, "financialSituation", object.financialSituation);

    if (object.financials != null) {
      for (String item : object.financials) {
        converted.addFinancial(filterField(item));
      }
    }

    if (object.fs_pseudonyms != null) {
      for (String item : object.fs_pseudonyms) {
        converted.addFsPseudonym(filterField(item));
      }
    }

    text = filterField(object.gender);
    if (text == null) {
      converted.setGender(Person.Gender.UNKNOWN);
    } else if (text.equals("M")) {
      converted.setGender(Person.Gender.MALE);
    } else if (text.equals("F")) {
      converted.setGender(Person.Gender.FEMALE);
    } else if (text.equals("U")) {
      converted.setGender(Person.Gender.UNKNOWN);
    } else {
      handleError("Unknown gender: %s", text);
      converted.setGender(Person.Gender.UNKNOWN);
    }

    converted.setHealth(filterField(object.health));
    converted.tempLanguages = concatenate(object.languages);
    converted.setLivedIn(filterField(object.lived_in));
    converted.setMaritalStatus(filterField(object.marital_status));
    converted.tempMemberships = concatenate(object.memberships);
    converted.tempMotherTongue = filterField(object.mother_tongue);

    String name = converted.tempName = filterField(object.name);
    if (name != null) {
      Matcher matcher = simpleNamePattern.matcher(name);
      if (matcher.matches()) {
        String surname = matcher.group(1);
        String forename = matcher.group(2);
        if (!excludedNames.contains(forename)) {
          converted.addName(PersonName.newInstance(forename, surname));
        }
      }
    }

    converted.setNationality(filterField(object.nationality));
    converted.setNotes(filterNotesField(object.notes));
    converted.setPersonalSituation(filterField(object.personal_situation));

    verifyEmptyField(line, "personalSituation", object.personalSituation);

    converted.tempPlaceOfBirth = concatenate(object.placeOfBirth);
    converted.tempDeathPlace = filterField(object.placeOfDeath);

    if (object.professions != null) {
      for (String item : object.professions) {
        converted.addProfession(filterField(item));
      }
    }

    converted.tempPseudonyms = concatenate(object.pseudonyms);
    converted.tempPsChildren = concatenate(object.ps_children);

    if (object.religion != null) {
      for (String item : object.religion) {
        converted.addReligion(filterField(item));
      }
    }

    if (object.social_class != null) {
      for (String item : object.social_class) {
        converted.addSocialClass(filterField(item));
      }
    }

    converted.tempPublishingLanguages = concatenate(object.publishing_languages);
    converted.tempSpouse = filterField(object.spouse);

    String type = filterField(object.type);
    if (converted.tempName != null && converted.tempName.startsWith("~")) {
      converted.addType(Person.Type.ARCHETYPE);
      nArchetype++;
    } else if (type == null || type.equalsIgnoreCase("unknown")) {
      nUnknown++;
    } else if (type.equalsIgnoreCase("author")) {
      converted.addType(Person.Type.AUTHOR);
      nAuthor++;
    } else if (type.equalsIgnoreCase("pseudonym")) {
      converted.addType(Person.Type.PSEUDONYM);
      nPseudonym++;
    } else {
      this.handleError("Illegal type '%s'%n", type);
    }

    if (object.url != null) {
      for (Map.Entry<String, String> entry : object.url.entrySet()) {
        String label = filterField(entry.getKey());
        String url = filterField(entry.getValue());
        converted.addLink(new Link(url, label));
      }
    }

    if ("author".equalsIgnoreCase(type) && "authors".equals(object.original_table) && object.old_id != 0) {
      converted.tempOldId = Integer.toString(object.old_id);
    }

    return converted;
  }

  private String concatenate(String[] items) {
    StringBuilder builder = new StringBuilder();
    if (items != null) {
      for (String item : items) {
        appendTo(builder, filterField(item), "; ");
      }
    }
    return (builder.length() > 0) ? builder.toString() : null;
  }

  private final Set<String> types = Sets.newTreeSet();

  protected static class XPerson {
    public String tempid;
    public String bibliography; // text
    public String born_in; // must be mapped to birthPlace
    public String children; // number of children
    public String[] collaborations; // seem to be references to persons
    public String dateOfBirth; // birth year
    public String dateOfDeath; // death year
    public String death; // unstructured
    public String[] education; // unstructured
    public String financial_situation; // INCORRECT
    public String financialSituation; // EMPTY
    public String[] financials; // sparse, unstructured
    public String[] fs_pseudonyms; // sparse, unstructured
    public String gender; // U, M, F
    public String health; // sparse, unstructured
    public String[] languages; //clarfy: spoken, published, ...
    public String lived_in; // sparse, unstructured
    public String marital_status; // sparse, unstructured
    public String[] memberships; // sparse, unstructured
    public String mother_tongue; // unstructured, how does this relate to languages?
    public String name; // unstructured
    public String nationality; // sparse, how does this relate to pace of birth?
    public String notes; // text
    public int old_id; // record number in NEWW database
    public String original_field; // ignore
    public String original_table; // table in NEWW database
    public String personal_situation; // unstructured
    public String personalSituation; // EMPTY
    public String[] placeOfBirth; // how can this be an array?
    public String placeOfDeath; // unstructured
    public String[] professions;
    public String[] ps_children;
    public String[] pseudonyms;
    public String[] publishing_languages;
    public String[] religion;
    public String[] social_class;
    public String spouse; // as relation
    public String spouse_id; // ignore
    public String type; // 'author', 'pseudonym', 'unknown'
    public Map<String, String> url;
  }

  // --- Relations -------------------------------------------------------------

  // Maps from names used in json to registered relation type names
  private RelationTypeConcordance relationTypeConcordance;

  private RelationTypeConcordance getRelationTypeConcordance() throws Exception {
    File file = new File(inputDir, "relationtypes.txt");
    return new RelationTypeConcordance(file);
  }

  public Set<String> names = Sets.newTreeSet();

  private int missingRelationTypes = 0;
  private int unstoredRelations = 0;

  private final Map<String, Reference> relationTypes = Maps.newHashMap();

  private void setupRelationDefs() {
    StorageIterator<RelationType> iterator = storageManager.getAll(RelationType.class);
    while (iterator.hasNext()) {
      RelationType type = iterator.next();
      relationTypes.put(type.getRegularName(), new Reference(RelationType.class, type.getId()));
    }
  }

  private Reference getReference(String type, String id) {
    if (type == null || type.isEmpty()) {
      return null;
    } else {
      String key = newKey(type, id);
      return references.get(key);
    }
  }

  private void importRelations() throws Exception {
    relationTypeConcordance = getRelationTypeConcordance();
    LineIterator iterator = getLineIterator("relations.json");
    String line = "";
    try {
      while (iterator.hasNext()) {
        line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleRelation(line);
        }
      }
    } catch (Exception e) {
      System.out.println(line);
      throw e;
    } finally {
      LineIterator.closeQuietly(iterator);
      relationTypeConcordance = null;
    }
    // for (String name : names) {
    //   System.out.println(name);
    // }
  }

  private void handleRelation(String line) throws Exception {
    XRelation object = objectMapper.readValue(line, XRelation.class);

    // First verify that essential fields are non-empty
    String relationType = filterField(object.relation_type);
    if (relationType == null) {
      if (++missingRelationTypes <= 5) {
        verifyNonEmptyField(line, "relation_type", relationType);
      }
      return;
    }
    String leftObject = verifyNonEmptyField(line, "leftObject", filterField(object.leftObject));
    String leftId = verifyNonEmptyField(line, "leftId", filterField(object.leftId));
    String rightObject = verifyNonEmptyField(line, "rightObject", filterField(object.rightObject));
    String rightId = verifyNonEmptyField(line, "rightId", filterField(object.rightId));
    if (leftObject == null || leftId == null || rightObject == null || rightId == null) {
      return;
    }

    // Keep track of types seen
    names.add(relationType + ":" + leftObject + "-->" + rightObject);

    // Map to registered relation type and validate
    RelationTypeConcordance.Mapping mapping = relationTypeConcordance.lookup(relationType, leftObject, rightObject);
    if (mapping == null) {
      handleError("No relation type for (%s, %s, %s)", relationType, leftObject, rightObject);
      return;
    }
    if (mapping.newName.equalsIgnoreCase("ILLEGAL")) {
      // skip this one, append to log file
      return;
    }

    if (mapping.inverse) {
      String tempObject = leftObject;
      leftObject = rightObject;
      rightObject = tempObject;
      String tempId = leftId;
      leftId = rightId;
      rightId = tempId;
    }

    // Now we should be able to retrieve the reference to the relation type
    Reference relationRef = relationTypes.get(mapping.newName);
    if (relationRef == null) {
      handleError("No relation type for '%s' (derived from '%s')", mapping.newName, mapping.oldName);
      return;
    }

    // Obtain references to source and target entities
    Reference sourceRef = getReference(leftObject, leftId);
    if (sourceRef == null) {
      if (!invalids.contains(newKey(leftObject, leftId))) {
        handleError("No source reference for '%s-%s'", leftObject, leftId);
      }
      return;
    }
    Reference targetRef = getReference(rightObject, rightId);
    if (targetRef == null) {
      if (!invalids.contains(newKey(rightObject, rightId))) {
        handleError("No target reference for '%s-%s'", rightObject, rightId);
      }
      return;
    }

    // Some more validation, to check Json lives up to our expectations
    verifyEmptyField(line, "canonizing", filterField(object.canonizing));
    verifyEmptyField(line, "certainty", filterField(object.certainty));
    //verifyEmptyField( line, "child_female",  filterTextField(object.child_female));
    //verifyEmptyField( line, "child_male",  filterTextField(object.child_male));
    verifyEmptyField(line, "notes", filterField(object.notes));
    //verifyEmptyField( line, "parent_female",  filterTextField(object.parent_female));
    //verifyEmptyField( line, "parent_male",  filterTextField(object.parent_male));
    verifyEmptyField(line, "qualification", filterField(object.qualification));

    // Finally we're ready to store
    String storedId = null;
    try {
      storedId = storeRelation(WWRelation.class, sourceRef, relationRef, targetRef, change, line);
    } catch (Exception e) {
      LOG.error("Failed to store: {}", line);
      System.exit(-1);
    }
    if (storedId == null) {
      // WHY would this happen??
      if (++unstoredRelations <= 5) {
        handleError("Not stored.. %s", line);
      }
      return;
    }

    // Once the relation is in place, we update the project specific attributes
    WWRelation relation = storageManager.getEntity(WWRelation.class, storedId);
    relation.setAccepted(true);
    relation.setReception(object.isReception);
    updateDomainEntity(WWRelation.class, relation);
  }

  private <T extends Relation> String storeRelation(Class<T> type, Reference sourceRef, Reference relTypeRef, Reference targetRef, Change change, String line) {
    if (sourceRef == null || relTypeRef == null || targetRef == null) {
      System.out.println(line);
      LOG.info("sourceRef {}", sourceRef);
      LOG.info("relTypeRef {}", relTypeRef);
      LOG.info("targetRef {}", targetRef);

      throw new IllegalArgumentException("Missing references");
    }
    try {
      return relationManager.storeRelation(type, sourceRef, relTypeRef, targetRef, change);
    } catch (IllegalArgumentException e) {
      System.out.println(line);
      throw e;
    }
  }

  protected static class XRelation {
    public String tempid;
    public String canonizing; // EMPTY
    public String certainty; // EMPTY
    public String child_female;
    public String child_male;
    public boolean isReception;
    public String leftId;
    public String leftName;
    public String leftObject;
    public String notes; // EMPTY
    public int old_id;
    public String original_table;
    public String parent_female;
    public String parent_male;
    public String qualification; // EMPTY
    public String reception_relation_type; // very sparse
    public String relation_type; // text
    public String rightId;
    public String rightName;
    public String rightObject;
  }

}
