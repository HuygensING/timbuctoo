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
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Collective;
import nl.knaw.huygens.timbuctoo.model.Document.DocumentType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.neww.WWCollective;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument.Print;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument.Source;
import nl.knaw.huygens.timbuctoo.model.neww.WWKeyword;
import nl.knaw.huygens.timbuctoo.model.neww.WWLanguage;
import nl.knaw.huygens.timbuctoo.model.neww.WWLocation;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.model.neww.WWRelation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Gender;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.mongo.EntityIds;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.util.EncodingFixer;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
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

  static final Logger LOG = LoggerFactory.getLogger(WomenWritersImporter.class);

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
      RelationManager relationManager = new RelationManager(registry, storageManager);
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
    super(registry, storageManager, relationManager, indexManager);
    objectMapper = new ObjectMapper();
    this.relationManager = relationManager;
    inputDir = new File(inputDirName);
    if (inputDir.isDirectory()) {
      System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
    } else {
      System.out.printf("%n.. Not a directory: %s%n", inputDir.getAbsolutePath());
    }
  }

  /** Returns map for a reference map. */
  private String newKey(String name, String id) {
    return name + ":" + id;
  }

  protected <T> T readJsonValue(File file, Class<T> valueType) throws Exception {
    String text = Files.readTextFromFile(file);
    // For Dutch Caribbean it seems OK to map "Ã " --> "à "
    String converted = EncodingFixer.convert2(text).replaceAll("Ã ", "à ");
    if (!converted.equals(text)) {
      int n = text.length() - converted.length();
      handleError("Fixed %d character encoding error%s in '%s'", n, (n == 1) ? "" : "s", file.getName());
    }
    return objectMapper.readValue(converted, valueType);
  }

  public void importAll() throws Exception {

    printBoxedText("Initialization");

    // displayStatus();

    removeNonPersistentEntities(WWCollective.class);
    removeNonPersistentEntities(WWDocument.class);
    removeNonPersistentEntities(WWKeyword.class);
    removeNonPersistentEntities(WWLocation.class);
    removeNonPersistentEntities(WWPerson.class);
    removeNonPersistentEntities(WWRelation.class);

    setup(relationManager);

    printBoxedText("Import");

    System.out.println(".. Collectives");
    System.out.printf("Number = %6d%n%n", importCollectives());

    System.out.println(".. Documents");
    System.out.printf("Number = %6d%n%n", importDocuments());

    System.out.println(".. Keywords");
    System.out.printf("Number = %6d%n%n", importKeywords());

    System.out.println(".. Languages");
    System.out.printf("Number = %6d%n%n", importLanguages());

    System.out.println(".. Locations");
    System.out.printf("Number = %6d%n%n", importLocations());

    System.out.println(".. Persons");
    System.out.printf("Number = %6d%n%n", importPersons());

    System.out.println(".. Relations");
    importRelations();
    System.out.printf("Number of missing relation types = %6d%n%n", missingRelationTypes);
    System.out.printf("Number of unstored relations     = %6d%n%n", unstoredRelations);

    printBoxedText("Indexing");

//    indexEntities(WWDocument.class);
//    indexEntities(WWKeyword.class);
//    indexEntities(WWLanguage.class);
//    indexEntities(WWPerson.class);

    displayStatus();
    displayErrorSummary();

    printBoxedText("Export");
    export();

  }

  // --- Support ---------------------------------------------------------------

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
        references.put(key, new Reference(WWCollective.class, storedId));
      }
    }
  }

  private WWCollective convert(String line, XCollective object) {
    WWCollective converted = new WWCollective();

    String type = filterTextField(object.type);
    verifyNonEmptyField(line, "type", type);
    if (type == null || type.equals("membership")) {
      type = "UNKNOWN";
    }
    try {
      Collective.Type ct = Collective.Type.valueOf(type.toUpperCase());
      converted.setType(ct);
    } catch (Exception e) {
      handleError("Unknown type [%s] in: %s", type, line);
      converted.setType(Collective.Type.UNKNOWN);
      converted.tempType = type;
    }

    String name = filterTextField(object.name);
    if (name == null) {
      handleError("Rejecting name [%s] in: %s", name, line);
      return null;
    }
    converted.setName(name);

    converted.tempLocationPlacename = filterTextField(object.location_placename);
    converted.setNotes(filterTextField(object.notes));
    converted.tempOrigin = filterTextField(object.origin);
    converted.setShortName(filterTextField(object.short_name));
    String url = filterTextField(object.url);
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

  private int importDocuments() throws Exception {
    int initialSize = references.size();
    documentTypeMap = createDocumentTypeMap();
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
        references.put(key, new Reference(WWDocument.class, storedId));
      }
    }
  }

  private WWDocument convert(String line, XDocument object) {
    WWDocument converted = new WWDocument();

    String type = filterTextField(object.type);
    verifyNonEmptyField(line, "type", type);
    DocumentType documentType = (type == null) ? null : documentTypeMap.get(type);
    converted.setDocumentType(documentType);

    String title = filterTextField(object.title);
    verifyNonEmptyField(line, "title", title);
    converted.setTitle(title != null ? title : "TBD");

    converted.setDescription(filterTextField(object.description));

    String date = filterTextField(object.date);
    if (date != null) {
      try {
        converted.setDate(new Datable(date));
      } catch (RuntimeException e) {
        handleError("Illegal 'date' in %s", line);
      }
    }

    converted.setNotes(filterTextField(object.notes));
    converted.setOrigin(filterTextField(object.origin));
    converted.setReference(filterTextField(object.reference));

    // the keywords are not normalized: identical topoi occur as different items
    if (object.topoi != null && object.topoi.length != 0) {
      for (String[] topos : object.topoi) {
        if (topos[0] != null) {
          converted.addTopos(topos[0]);
        }
      }
    }

    if (object.prints != null && object.prints.size() != 0) {
      StringBuilder builder = new StringBuilder();
      builder.append(String.format("%n** Prints of record %d [%s]%n", object.old_id, converted.getTitle()));
      // order by key
      Map<String, Print> temp = Maps.newTreeMap();
      temp.putAll(object.prints);
      for (Map.Entry<String, Print> entry : temp.entrySet()) {
        Print filteredPrint = new Print();
        filteredPrint.setEdition(filterTextField(entry.getValue().getEdition()));
        filteredPrint.setPublisher(filterTextField(entry.getValue().getPublisher()));
        filteredPrint.setLocation(filterTextField(entry.getValue().getLocation()));
        filteredPrint.setYear(filterTextField(entry.getValue().getYear()));
        converted.addTempPrint(filteredPrint);
        builder.append(String.format("%s%n", filteredPrint));
      }
      // records.put(new Integer(object.old_id), builder.toString());
    }

    if (object.source != null) {
      Source source = new Source();
      source.setType(filterTextField(object.source.type));
      source.setFullName(filterTextField(object.source.full_name));
      source.setShortName(filterTextField(object.source.short_name));
      source.setNotes(filterTextField(object.source.notes));
      converted.setSource(source);
    }

    if (object.urls != null) {
      for (Map.Entry<String, String> entry : object.urls.entrySet()) {
        String label = filterTextField(entry.getKey());
        String url = filterTextField(entry.getValue());
        converted.addLink(new Link(url, label));
      }
    }

    converted.tempCreator = filterTextField(object.creator);
    converted.tempLanguage = filterTextField(object.language);

    return converted.isValid() ? converted : null;
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
    public int old_id; // ignore
    public String origin; // item of a list of countries. BUT origin of what?
    public String original_table; // ignore
    public Map<String, Print> prints; // printed editions
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

  // --- Keywords --------------------------------------------------------------

  // First impression:
  // unique: 43 genre, 279 topos, 0 others

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
        references.put(key, new Reference(WWKeyword.class, storedId));
      }
    }
  }

  private WWKeyword convert(String line, XKeyword object) {
    WWKeyword converted = new WWKeyword();

    converted.setType(filterTextField(object.type));
    verifyNonEmptyField(line, "type", converted.getType());

    converted.setValue(filterTextField(object.keyword));
    verifyNonEmptyField(line, "keyword", converted.getValue());

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
          String name = filterTextField(object.name);
          verifyNonEmptyField(line, "name", name);
          if (name != null) {
            String code = mapName(map, name);
            Language language = storageManager.findEntity(Language.class, "^code", code);
            if (language == null) {
              verifyNonEmptyField(line, "name", null);
            } else {
              String flag = name.equals(language.getName()) ? "" : "  *";
              System.out.printf("%-30s%-8s%-30s%s%n", name, language.getCode(), language.getName(), flag);
              WWLanguage wwLanguage = new WWLanguage();
              wwLanguage.setId(language.getId());
              wwLanguage.setRev(language.getRev());
              wwLanguage.setCode(language.getCode());
              wwLanguage.setName(language.getName());
              wwLanguage.setCore(true);
              // TODO prevent multiple updates for same language
              updateDomainEntity(WWLanguage.class, wwLanguage);
              String key = newKey("Language", object.tempid);
              references.put(key, new Reference(WWLanguage.class, wwLanguage.getId()));
              // System.out.printf("%s, %s --> %s%n", key, object.name, wwLanguage.getId());
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

  private int importLocations() throws Exception {
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
    } else {
      WWLocation converted = convert(json, object);
      if (converted == null) {
        invalids.add(key);
      } else {
        String storedId = addDomainEntity(WWLocation.class, converted);
        references.put(key, new Reference(WWLocation.class, storedId));
      }
    }
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

    converted.setAddress(filterTextField(object.address));
    converted.setSettlement(filterTextField(object.settlement));
    converted.setCountry(filterTextField(object.country));
    converted.setZipcode(filterTextField(object.zipcode));

    return converted.isValid() ? converted : null;
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

  //  ~reader(s) female (member of Damesleesmuseum, The Hague)
  //  ~~Nutsbibliotheken Nederland
  //  ~~anonymous Dutch
  //  ~~anonymous English 
  //  ~~anonymous French
  //  ~~anonymous German
  //  ~~anonymous Italian
  //  ~~anonymous Russian woman
  //  ~~anonymous Spanish
  //  ~~author female (name unknown)
  //  ~~author male (name below)
  //  ~~censorship (in one form or another)
  //  ~~editor (name unknown)
  //  ~~foreign editor
  //  ~~historian of literature  (male, name below)
  //  ~~illustrator (name below)
  //  ~~journalist (name below)
  //  ~~journalist (name unknown)
  //  ~~librarian (name below)
  //  ~~reader(s) (gender unknown)
  //  ~~reader(s) female (name below)
  //  ~~reader(s) male (name below)
  //  ~~translator (name unknown)
  //  ~~translator male (name below)

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
        String storedId = addDomainEntity(WWPerson.class, converted);
        references.put(key, new Reference(WWPerson.class, storedId));
      }
    }
  }

  private WWPerson convert(String line, XPerson object) {
    String text;
    WWPerson converted = new WWPerson();

    converted.setBibliography(filterTextField(object.bibliography));

    text = filterTextField(object.born_in);
    if (text != null && !"TBD".equalsIgnoreCase(text) && !"unknown".equalsIgnoreCase(text)) {
      converted.tempBirthPlace = text;
    }

    converted.setNumberOfChildren(filterTextField(object.children));

    if (object.collaborations != null) {
      for (String item : object.collaborations) {
        String collaboration = filterTextField(item);
        if (collaboration != null && !"Not yet checked".equals(collaboration) && !"unknown".equals(collaboration)) {
          converted.addCollaboration(collaboration);
        }
      }
    }

    text = filterTextField(object.dateOfBirth);
    if (text != null) {
      converted.setBirthDate(new Datable(text));
    }

    text = filterTextField(object.dateOfDeath);
    if (text != null) {
      converted.setDeathDate(new Datable(text));
    }

    converted.tempDeath = filterTextField(object.death);

    if (object.education != null) {
      for (String item : object.education) {
        converted.addEducation(filterTextField(item));
      }
    }

    converted.tempFinancialSituation = filterTextField(object.financial_situation);

    verifyEmptyField(line, "financialSituation", object.financialSituation);

    if (object.financials != null) {
      for (String item : object.financials) {
        converted.addFinancial(filterTextField(item));
      }
    }

    if (object.fs_pseudonyms != null) {
      for (String item : object.fs_pseudonyms) {
        converted.addFsPseudonym(filterTextField(item));
      }
    }

    text = filterTextField(object.gender);
    if ("U".equals(text)) {
      converted.setGender(Gender.UNKNOWN);
    } else if ("M".equals(text)) {
      converted.setGender(Gender.MALE);
    } else if ("F".equals(text)) {
      converted.setGender(Gender.FEMALE);
    } else if (text != null) {
      handleError("Unknown gender: %s", text);
    }

    converted.setHealth(filterTextField(object.health));

    if (object.languages != null) {
      for (String item : object.languages) {
        converted.addTempLanguage(filterTextField(item));
      }
    }

    converted.setLivedIn(filterTextField(object.lived_in));

    converted.setMaritalStatus(filterTextField(object.marital_status));

    if (object.memberships != null) {
      for (String item : object.memberships) {
        converted.addMembership(filterTextField(item));
      }
    }

    converted.tempMotherTongue = filterTextField(object.mother_tongue);

    converted.tempName = filterTextField(object.name);

    converted.setNationality(filterTextField(object.nationality));

    converted.setNotes(filterTextField(object.notes));

    converted.setPersonalSituation(filterTextField(object.personal_situation));

    verifyEmptyField(line, "personalSituation", object.personalSituation);

    if (object.placeOfBirth != null) {
      for (String item : object.placeOfBirth) {
        converted.tempPlaceOfBirth.add(filterTextField(item));
      }
    }

    if (object.url != null) {
      for (Map.Entry<String, String> entry : object.url.entrySet()) {
        String label = filterTextField(entry.getKey());
        String url = filterTextField(entry.getValue());
        converted.addLink(new Link(url, label));
      }
    }

    return converted;
  }

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
    public int old_id; // ignore
    public String original_field; // ignore
    public String original_table; // ignore
    public String personal_situation; // unstructured
    public String personalSituation; // EMPTY
    public String[] placeOfBirth; // how can this be an array?
    public String placeOfDeath;
    public String[] professions;
    public String[] ps_children;
    public String[] pseudonyms;
    public String[] publishing_languages;
    public String[] religion;
    public String[] social_class;
    public String spouse;
    public String spouse_id;
    public String type;
    public Map<String, String> url;
  }

  // --- Relations -------------------------------------------------------------

  private static boolean SAME_ORDER = false;
  private static boolean REVERSED_ORDER = true;

  private static class RelationMapping {
    public RelationMapping(String oldName, String newName, boolean reverse) {
      this.oldName=oldName;
      this.newName=newName;
      this.reverse=reverse;
    }
    public String oldName;
    public String newName;
    public boolean reverse;
  }

  private Map<String,RelationMapping> relationMappings = Maps.newHashMap();
  
  private void addRelationMapping(String oldName, String newName, boolean reverse) {
    relationMappings.put(oldName, new RelationMapping(oldName, newName, reverse));
  }
  
  private RelationMapping getRelationMapping(String name) {
    RelationMapping mapping = relationMappings.get(name);
    return (mapping != null) ? mapping : new RelationMapping(name, name, SAME_ORDER);
  }

  private void setupRelationMappings() {
    addRelationMapping("membership", "is_member_of", REVERSED_ORDER);
  }

  private int missingRelationTypes = 0;
  private int unstoredRelations = 0;

  private Map<String, Reference> relationTypes = Maps.newHashMap();

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
    setupRelationMappings();
    setupRelationDefs();
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
    }
  }

  private void handleRelation(String line) throws Exception {
    XRelation object = objectMapper.readValue(line, XRelation.class);

    String type = filterTextField(object.relation_type);
    if (type == null) {
      if (++missingRelationTypes <= 10) {
        verifyNonEmptyField(line, "relation_type", type);
      }
      return;
    }
    RelationMapping mapping = getRelationMapping(type);
    Reference relationRef = relationTypes.get(mapping.newName);
    if (relationRef == null) {
      handleError("No relation type for '%s' --> '%s'", mapping.oldName, mapping.newName);
      return;
    }

    String leftObject = verifyNonEmptyField(line, "leftObject", filterTextField(object.leftObject));
    if (leftObject == null) {
      return;
    }
    String leftId = verifyNonEmptyField(line, "leftId", filterTextField(object.leftId));
    if (leftId == null) {
      return;
    }
    String rightObject = verifyNonEmptyField(line, "rightObject", filterTextField(object.rightObject));
    if (rightObject == null) {
      return;
    }
    String rightId = verifyNonEmptyField(line, "rightId", filterTextField(object.rightId));
    if (rightId == null) {
      return;
    }
    if (mapping.reverse) {
        String tempObject = leftObject;
        leftObject = rightObject;
        rightObject = tempObject;
        String tempId = leftId;
        leftId = rightId;
        rightId = tempId;
    }

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

    verifyEmptyField(line, "canonizing", filterTextField(object.canonizing));
    verifyEmptyField(line, "certainty", filterTextField(object.certainty));
    //verifyEmptyField( line, "child_female",  filterTextField(object.child_female));
    //verifyEmptyField( line, "child_male",  filterTextField(object.child_male));
    verifyEmptyField(line, "notes", filterTextField(object.notes));
    //verifyEmptyField( line, "parent_female",  filterTextField(object.parent_female));
    //verifyEmptyField( line, "parent_male",  filterTextField(object.parent_male));
    verifyEmptyField(line, "qualification", filterTextField(object.qualification));

    String storedId = storeRelation(WWRelation.class, sourceRef, relationRef, targetRef, change, line);
    if (storedId == null) {
      if (++unstoredRelations <= 10) {
        handleError("Not stored.. %s", line);
      }
      return;
    }

    WWRelation relation = storageManager.getEntity(WWRelation.class, storedId);

    relation.setReception(object.isReception);
    updateDomainEntity(WWRelation.class, relation);
  }

  private <T extends Relation> String storeRelation(Class<T> type, Reference sourceRef, Reference relTypeRef, Reference targetRef, Change change, String line) {
    if (sourceRef == null || relTypeRef == null || targetRef == null) {
      System.out.println(line);
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
    public int old_id; // ignored
    public String original_table; // ignored
    public String parent_female;
    public String parent_male;
    public String qualification; // EMPTY
    public String reception_relation_type; // very sparse
    public String relation_type; // text
    public String rightId;
    public String rightName;
    public String rightObject;
  }

  // ---------------------------------------------------------------------------
  
  public void export() {
    File exportDir = new File("export");
    exportDir.mkdirs();
    for (long counter = 1000; counter<= 10000; counter += 1000) {
      exportEntity(exportDir, WWDocument.class, counter);
    }
    for (long counter = 1000; counter<= 10000; counter += 1000) {
      exportEntity(exportDir, WWPerson.class, counter);
    }
  }

  private <T extends DomainEntity> void exportEntity(File exportDir, Class<T> type, long counter) {
    String id = EntityIds.formatEntityId(type, counter);
    T entity = storageManager.getEntityWithRelations(type, id);
    if (entity != null) {
      try {
        File jsonFile = new File(exportDir, id + ".json");
        objectMapper.writeValue(jsonFile, entity);
     } catch (Exception e) {
       System.err.printf("Failed to write %s%n", id);
    	  }
    }
  }

}
