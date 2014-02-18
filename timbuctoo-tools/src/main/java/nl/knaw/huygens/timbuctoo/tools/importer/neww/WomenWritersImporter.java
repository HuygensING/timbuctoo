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
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARRelation;
import nl.knaw.huygens.timbuctoo.model.neww.Print;
import nl.knaw.huygens.timbuctoo.model.neww.WWCollective;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWLanguage;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
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

  private final ObjectMapper objectMapper;
  private final RelationManager relationManager;
  private final File inputDir;

  protected final Map<String, Reference> tempRefMap = Maps.newHashMap();

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

    printBoxedText("1. Initialization");

    // displayStatus();

    // removeNonPersistentEnties(storageManager, indexManager);

    // displayStatus();

    setup(relationManager);

    printBoxedText("2. Basic properties");

    boolean importCollectives = false;
    boolean importDocuments = true;
    boolean importKeywords = false;
    boolean importLanguages = false;
    boolean importLocations = false;
    boolean importPersons = false;

    if (importCollectives) {
      System.out.println(".. Collectives");
      tempRefMap.clear();
      importCollectives(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    if (importDocuments) {
      System.out.println(".. Documents");
      tempRefMap.clear();
      importDocuments(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    if (importKeywords) {
      System.out.println(".. Keywords");
      tempRefMap.clear();
      importKeywords(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    if (importLanguages) {
      System.out.println(".. Languages");
      tempRefMap.clear();
      importLanguages(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    if (importLocations) {
      System.out.println(".. Locations");
      tempRefMap.clear();
      importLocations(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    if (importPersons) {
      System.out.println(".. Persons");
      tempRefMap.clear();
      importPersons(tempRefMap);
      System.out.printf("Number of entries = %d%n", tempRefMap.size());
    }

    // printBoxedText("3. Relations");

    // printBoxedText("4. Indexing");

    // Like this:
    // indexEntities(DCARKeyword.class);

    // displayStatus();

    displayErrorSummary();
  }

  protected void removeNonPersistentEnties(StorageManager storageManager, IndexManager indexManager) throws IOException, IndexException {
    // like this:
    // removeNonPersistentEntities(DCARKeyword.class);
  }

  // --- relations -----------------------------------------------------

  protected Reference retrieveRelationType(String name) {
    RelationType type = relationManager.getRelationTypeByName(name);
    if (type != null) {
      LOG.debug("Retrieved {}", type.getDisplayName());
      return new Reference(RelationType.class, type.getId());
    } else {
      LOG.error("Failed to retrieve relation type {}", name);
      throw new IllegalStateException("Initialization error");
    }
  }

  protected void addRegularRelations(Reference sourceRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        relationManager.storeRelation(DCARRelation.class, sourceRef, relTypeRef, map.get(key), change);
      }
    }
  }

  protected void addInverseRelations(Reference targetRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        relationManager.storeRelation(DCARRelation.class, map.get(key), relTypeRef, targetRef, change);
      }
    }
  }

  private LineIterator getLineIterator(String filename) throws IOException {
    File file = new File(inputDir, filename);
    return FileUtils.lineIterator(file, "UTF-8");
  }

  private String preprocessJson(String line) {
    line = StringUtils.stripToEmpty(line);
    line = line.replaceAll("\"_id\"", "\"tempid\"");
    line = line.replaceAll("ObjectId\\(\\s*(\\S+)\\s*\\)", "$1");
    return line;
  }

  private void verifyEmptyField(String line, String key, String value) {
    if (!Strings.isNullOrEmpty(value)) {
      System.err.printf("## Unexpected value for '%s'%n", key);
      System.err.printf("## %s%n", line);
    }
  }

  private void verifyNonEmptyField(String line, String key, String value) {
    if (Strings.isNullOrEmpty(value)) {
      System.err.printf("## Missing value for '%s'%n", key);
      System.err.printf("## %s%n", line);
    }
  }

  // --- Collectives -----------------------------------------------------------

  private void importCollectives(Map<String, Reference> references) throws Exception {
    LineIterator iterator = getLineIterator("collectives.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleCollective(line, references);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
  }

  private void handleCollective(String json, Map<String, Reference> references) throws Exception {
    XCollective object = objectMapper.readValue(json, XCollective.class);
    String jsonId = object.tempid;
    if (references.containsKey(jsonId)) {
      handleError("Duplicate id %s", jsonId);
    } else {
      WWCollective collective = convert(json, object);
      String storedId = addDomainEntity(WWCollective.class, collective);
      references.put(jsonId, new Reference(WWCollective.class, storedId));
    }
  }

  private WWCollective convert(String line, XCollective object) {
    WWCollective converted = new WWCollective();
    converted.setType(filterTextField(object.type));
    verifyNonEmptyField(line, "type", converted.getType());
    converted.setName(filterTextField(object.name));
    verifyNonEmptyField(line, "name", converted.getName());
    converted.setShortName(filterTextField(object.short_name));
    converted.setTelephone(filterTextField(object.telephone));
    converted.setEmail(filterTextField(object.email));
    converted.setUrl(filterTextField(object.url));
    converted.setNotes(filterTextField(object.notes));
    converted.tempLocationPlacename = filterTextField(object.location_placename);
    converted.tempOrigin = filterTextField(object.origin);
    return converted;
  }

  public static class XCollective {
    public String tempid;
    public String email;
    public String location_id; // needed for relation, don't store
    public String location_placename; // store temporarily
    public String name; // 4 entries without a anme, but they occur in relations
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

  private Map<String, String> documentTypeMap;

  private Map<String, String> createDocumentTypeMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("Article", "Article");
    map.put("Catalogue", "Catalogue");
    map.put("List", "List");
    map.put("Picture", "Picture");
    map.put("Publicity", "Publicity");
    map.put("TBD", "TBD");
    map.put("To Be Done", "TBD");
    map.put("To be done", "TBD");
    map.put("Work", "Work");
    map.put("work", "Work");
    return map;
  }

  private final Set<String> values = Sets.newTreeSet();

  private void importDocuments(Map<String, Reference> references) throws Exception {
    documentTypeMap = createDocumentTypeMap();
    LineIterator iterator = getLineIterator("documents.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          handleDocument(preprocessDocument(line), references);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }

    for (String value : values) {
      System.out.println(value);
    }
  }

  private String preprocessDocument(String text) {
    text = text.replaceAll("\"prints\" : \"\"", "\"prints\" : null");
    text = text.replaceAll("\"source\" : \"\"", "\"source\" : null");
    text = text.replaceAll("\"subject\" : \"\"", "\"subject\" : null");
    text = text.replaceAll("\"subject\" : \\[\\]", "\"subject\" : null");
    text = text.replaceAll("\"topoi\" : \"\"", "\"topoi\" : null");
    text = text.replaceAll("\"url\" : \\{ \"url\" : null, \"label\" : null \\}", "\"url\": null");
    return text;
  }

  private void handleDocument(String json, Map<String, Reference> references) throws Exception {
    XDocument object = objectMapper.readValue(json, XDocument.class);
    String jsonId = object.tempid;
    if (references.containsKey(jsonId)) {
      handleError("Duplicate id %s", jsonId);
    } else {
      WWDocument collective = convert(json, object);
      String storedId = addDomainEntity(WWDocument.class, collective);
      references.put(jsonId, new Reference(WWDocument.class, storedId));
    }
  }

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

  private WWDocument convert(String line, XDocument object) {
    WWDocument converted = new WWDocument();

    String type = filterTextField(object.type);
    if (type != null) {
      type = documentTypeMap.get(type);
    }
    verifyNonEmptyField(line, "type", type);
    converted.setType(type);

    converted.setTitle(filterTextField(object.title));
    verifyNonEmptyField(line, "title", converted.getTitle());

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

    if (object.source != null) {
      System.out.println(object.source);
    }

    if (object.prints != null) {
      // order by key
      Map<String, Print> temp = Maps.newTreeMap();
      temp.putAll(object.prints);
      for (Map.Entry<String, Print> entry : temp.entrySet()) {
        Print filteredPrint = new Print();
        filteredPrint.setEdition(filterTextField(entry.getValue().getEdition()));
        filteredPrint.setPublisher(filterTextField(entry.getValue().getPublisher()));
        filteredPrint.setLocation(filterTextField(entry.getValue().getLocation()));
        filteredPrint.setYear(filterTextField(entry.getValue().getYear()));
        converted.addPrint(filteredPrint);
      }
    }

    String url = filterTextField(object.url);
    if (url != null) {
      converted.setUrl(new Link(url, filterTextField(object.url_title)));
    }

    converted.tempCreator = filterTextField(object.creator);
    converted.tempLanguage = filterTextField(object.language);
    return converted;
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
    public String url; // convert to Link
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
  // some keywords need work: "Irrelevant", "TBD"

  protected void importKeywords(Map<String, Reference> referenceMap) throws Exception {
    Set<String> genre = Sets.newTreeSet();
    Set<String> topos = Sets.newTreeSet();
    LineIterator iterator = getLineIterator("keywords.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          XKeyword object = objectMapper.readValue(line, XKeyword.class);
          referenceMap.put(object.tempid, null);
          if (Strings.isNullOrEmpty(object.type)) {
            handleError("%s: Missing type", object.tempid);
          } else if (Strings.isNullOrEmpty(object.keyword)) {
            handleError("%s: Missing keyword", object.tempid);
          } else if ("genre".equals(object.type)) {
            genre.add(object.keyword);
          } else if ("topos".equals(object.type)) {
            topos.add(object.keyword);
          } else {
            handleError("%s: Unexpected type", object.tempid);
          }
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
    System.out.printf("genre: %4d%n", genre.size());
    for (String keyword : genre) {
      System.out.println(keyword);
    }
    System.out.printf("topos: %4d%n", topos.size());
    for (String keyword : topos) {
      System.out.println(keyword);
    }
  }

  public static class XKeyword {
    public String tempid;
    public String keyword;
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
    map.put("Atticism", "?");
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
    map.put("Greek", "?");
    map.put("Hebrew", "?");
    map.put("Hungarian", "hun");
    map.put("Icelandic", "isl");
    map.put("Irish Gaelic", "?");
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
    map.put("Slavo-Serbian", "?");
    map.put("Slovakian", "slk");
    map.put("Slovenian", "slv");
    map.put("Sorbian language", "?");
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

  private void importLanguages(Map<String, Reference> referenceMap) throws Exception {
    Map<String, String> map = createNameCodeMap();
    LineIterator iterator = getLineIterator("languages.json");
    try {
      System.out.printf("\"language\",\"ISO-code\",\"ISO-name\"%n");
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          XLanguage object = objectMapper.readValue(line, XLanguage.class);
          String name = object.name;
          if (Strings.isNullOrEmpty(name)) {
            handleError("%s: Missing name", object.tempid);
          } else {
            name = name.trim();
            String code = mapName(map, name);
            WWLanguage language = storageManager.findEntity(WWLanguage.class, "^code", code);
            if (language == null) {
              System.out.printf("\"%s\",\"?\",\"?\" *%n", name);
            } else if (name.equals(language.getName())) {
              System.out.printf("\"%s\",\"%s\",\"%s\"%n", name, language.getCode(), language.getName());
              language.setCore(true);
              updateDomainEntity(WWLanguage.class, language);
              referenceMap.put(object.tempid, new Reference(WWLanguage.class, language.getId()));
            } else {
              System.out.printf("\"%s\",\"%s\",\"%s\" *%n", name, language.getCode(), language.getName());
              language.setCore(true);
              updateDomainEntity(WWLanguage.class, language);
              referenceMap.put(object.tempid, new Reference(WWLanguage.class, language.getId()));
            }
          }
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
  }

  public static class XLanguage {
    public String tempid;
    public String ISO639_1Code;
    public String ISO639_2Code;
    public String name;
  }

  // --- Locations -------------------------------------------------------------

  // normalize whitespace (collapse, trim)
  // error: latitude is used instead of settlement
  // we could probably map countries --> standard list required
  // we should check for fully empty records

  protected void importLocations(Map<String, Reference> referenceMap) throws Exception {
    LineIterator iterator = getLineIterator("locations.json");
    try {
      while (iterator.hasNext()) {
        String line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          XLocation object = objectMapper.readValue(line, XLocation.class);
          referenceMap.put(object.tempid, null);
          verifyEmptyField(line, "bloc", object.bloc);
          verifyEmptyField(line, "district", object.district);
          verifyEmptyField(line, "houseNumber", object.houseNumber);
          verifyEmptyField(line, "latitude", object.latitude);
          verifyEmptyField(line, "longitude", object.longitude);
          verifyEmptyField(line, "notes", object.notes);
          verifyEmptyField(line, "period", object.period);
          verifyEmptyField(line, "region", object.region);
          verifyEmptyField(line, "settlement", object.settlement);
          object.address = StringUtils.stripToEmpty(object.address).replaceAll("\\s+", " ");
          object.settlement = StringUtils.stripToEmpty(object.geogName).replaceAll("\\s+", " ");
          object.geogName = null;
          object.country = StringUtils.stripToEmpty(object.country).replaceAll("\\s+", " ");
          object.zipcode = StringUtils.stripToEmpty(object.zipcode).replaceAll("\\s+", " ");
          // System.out.printf("[%s][%s][%s][%s]%n", object.address, object.settlement, object.country, object.zipcode);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
    }
  }

  public static class XLocation {
    public String tempid;
    public String address;
    public String bloc; // no data
    public String country;
    public String district; // no data
    public String geogName; // contains settlement
    public String houseNumber; // no data
    public String latitude; // no data
    public String longitude; // no data
    public String notes; // no data
    public String period; // no data
    public String region; // no data
    public String settlement; // no data
    public String zipcode; // 54 values
  }

  // --- Persons ---------------------------------------------------------------

  // Error: an empty "url" filed has been mapped to a single string
  // Fix: "url" : "" --> "url" : null
  // Fix: "languages" : "" --> "languages" : null
  // Fix: "health" : [] --> "health" : null (1x)

  protected void importPersons(Map<String, Reference> referenceMap) throws Exception {
    LineIterator iterator = getLineIterator("persons.json");
    String line = "'";
    try {
      while (iterator.hasNext()) {
        line = preprocessJson(iterator.nextLine());
        if (!line.isEmpty()) {
          XPerson object = objectMapper.readValue(line, XPerson.class);
          referenceMap.put(object.tempid, null);
          // if (object.placeOfBirth != null && object.placeOfBirth.length > 1) {
          //   System.err.println("-- curious: " + StringUtils.join(object.placeOfBirth, ","));
          // }
        }
      }
    } catch (JsonMappingException e) {
      System.err.println(line);
      throw e;
    } finally {
      LineIterator.closeQuietly(iterator);
    }
  }

  public static class XPerson {
    public String tempid;
    public String bibliography;
    public String born_in;
    public String children;
    public String[] collaborations;
    public String dateOfBirth;
    public String dateOfDeath;
    public String death;
    public String[] education;
    public String financial_situation;
    public String financialSituation;
    public String[] financials;
    public String[] fs_pseudonyms;
    public String gender;
    public String health;
    public String[] languages;
    public String lived_in;
    public String marital_status;
    public String[] memberships;
    public String mother_tongue;
    public String name;
    public String nationality;
    public String notes;
    public String original_field;
    public String personal_situation;
    public String personalSituation;
    public String[] placeOfBirth;
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
    public XURL url;
  }

  public static class XURL {
    public String url;
    public String label;
  }

}
