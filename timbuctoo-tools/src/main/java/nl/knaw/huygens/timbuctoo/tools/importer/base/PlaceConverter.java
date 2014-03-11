package nl.knaw.huygens.timbuctoo.tools.importer.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.util.PlaceName;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PlaceConverter extends CSVImporter {

  private static final char SEPERATOR_CHAR = ';';
  private static final char QUOTE_CHAR = '"';
  private static final int LINES_TO_SKIP = 0;

  public static void main(String[] args) throws Exception {
    String fileName = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/neww/locations.txt";

    PlaceConverter converter = new PlaceConverter();
    converter.handleFile(fileName, 9, false);
    System.out.println("-- done --");
  }

  private final Set<String> urns = Sets.newTreeSet();
  private final ObjectMapper mapper;
  private final FileOutputStream stream;

  public PlaceConverter() throws Exception {
    super(new PrintWriter(System.err), SEPERATOR_CHAR, QUOTE_CHAR, LINES_TO_SKIP);
    mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    stream = new FileOutputStream(new File("locations.json"), false);
  }

  @Override
  protected void handleLine(String[] items) {
    Place place = new Place();
    int index = 0;

    String urn = convert(items[index++]);

    String lang = convert(items[index++]);
    place.setDefLang(lang);

    PlaceName name = new PlaceName();
    name.setDistrict(convert(items[index++]));
    name.setSettlement(convert(items[index++]));

    boolean forceRegion = false;
    String region = convert(items[index++]);
    if (region != null && region.startsWith("*")) {
      region = region.substring(1);
      forceRegion = true;
    }
    name.setRegion(region);

    name.setCountry(convert(items[index++]));
    if (name.getCountry() != null) {
      String code = urn.substring(3, 6).toUpperCase();
      name.setCountryCode(code);
    }
    place.addName(lang, name);

    StringBuilder builder = new StringBuilder();
    append(builder, name.getDistrict());
    append(builder, name.getSettlement());
    if (forceRegion || builder.length() == 0) {
      append(builder, name.getRegion());
    }
    append(builder, urn.substring(3));
    urn = removeDiacritics(urn.substring(0, 3) + builder.toString());
    urn = urn.replaceAll("['\\-/]", " ");
    urn = urn.replaceAll("\\b\\w ", " ");
    urn = urn.replaceAll("\\baan ", "");
    urn = urn.replaceAll("\\bde ", "");
    urn = urn.replaceAll("\\ben ", "");
    urn = urn.replaceAll("\\bhet ", "");
    urn = urn.replaceAll("\\bla ", "");
    urn = urn.replaceAll("\\ble ", "");
    urn = urn.replaceAll("\\bles ", "");
    urn = urn.replaceAll("\\bop ", " ");
    urn = urn.replaceAll("\\bvan ", " ");
    urn = urn.replaceAll("\\bsur ", " ");
    urn = urn.replaceAll("\\bupon ", " ");
    urn = urn.replaceAll("\\bst\\.", "st ");
    urn = urn.replaceAll(":\\s+", ":");
    urn = urn.replaceAll("\\s+", "-");
    place.setUrn(urn);
    if (!urns.add(urn)) {
      System.out.println("## Duplicate URN: " + urn);
    }

    if (!"eng".equals(lang)) {
      PlaceName engName = new PlaceName();
      engName.setDistrict(name.getDistrict());
      engName.setSettlement(name.getSettlement());
      engName.setRegion(name.getRegion());
      engName.setCountry(convertCountry(name.getCountry()));
      engName.setCountryCode(name.getCountryCode());
      place.addName("eng", engName);
    }
    place.setLatitude(convert(items[index++]));
    place.setLongitude(convert(items[index++]));
    place.setNotes(convert(items[index++]));
    write(place);
  }

  private void append(StringBuilder builder, String text) {
    if (text != null && text.length() != 0) {
      String separator = (builder.length() == 0) ? "" : ".";
      builder.append(separator).append(text);
    }
  }

  private String convert(String text) {
    text = StringUtils.stripToEmpty(text);
    return (text.isEmpty() || text.equals("-")) ? null : text;
  }

  private String convertCountry(String name) {
    if (name == null) {
      return "";
    } else if (name.equals("België")) {
      return "Belgium";
    } else if (name.equals("Danmark")) {
      return "Denmark";
    } else if (name.equals("Deutschland")) {
      return "Germany";
    } else if (name.equals("Italia")) {
      return "Italy";
    } else if (name.equals("Nederland")) {
      return "Netherlands";
    } else if (name.equals("Österreich")) {
      return "Austria";
    } else if (name.equals("Polska")) {
      return "Poland";
    } else if (name.equals("Suomi")) {
      return "Finland";
    } else {
      return name;
    }
  }

  private void write(Place place) {
    try {
      String value = mapper.writeValueAsString(place);
      IOUtils.write(value, stream, "UTF-8");
      IOUtils.write("\n", stream, "UTF-8");
      // System.out.println(value);
    } catch (Exception e) {
      System.out.println("## " + e.getMessage());
    }
  }

  private String removeDiacritics(String s) {
    char[] input = s.toLowerCase().toCharArray();
    char[] output = new char[512];
    int pos = ASCIIFoldingFilter.foldToASCII(input, 0, output, 0, input.length);
    return new String(output, 0, pos);
  }

  // Language aware
  // We accept data redundancy for easy of processing
  public static class Place {
    private String defLang;
    private Map<String, PlaceName> names;
    private String latitude;
    private String longitude;
    // URN for making concordances
    private String urn;
    private String notes;

    public Place() {
      names = Maps.newTreeMap();
    }

    public String getDefLang() {
      return defLang;
    }

    public void setDefLang(String lang) {
      defLang = lang;
    }

    @JsonIgnore
    public PlaceName getName() {
      return getName(defLang);
    }

    @JsonIgnore
    public PlaceName getName(String lang) {
      return names.get(lang);
    }

    public Map<String, PlaceName> getNames() {
      return names;
    }

    public void setNames(Map<String, PlaceName> names) {
      this.names = names;
    }

    public void addName(String lang, PlaceName name) {
      names.put(lang, name);
    }

    public String getLatitude() {
      return latitude;
    }

    public void setLatitude(String latitude) {
      this.latitude = latitude;
    }

    public String getLongitude() {
      return longitude;
    }

    public void setLongitude(String longitude) {
      this.longitude = longitude;
    }

    public String getUrn() {
      return urn;
    }

    public void setUrn(String urn) {
      this.urn = urn;
    }

    public String getNotes() {
      return notes;
    }

    public void setNotes(String notes) {
      this.notes = notes;
    }
  }

}
