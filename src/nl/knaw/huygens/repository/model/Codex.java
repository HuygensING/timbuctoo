package nl.knaw.huygens.repository.model;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;
import nl.knaw.huygens.repository.model.storage.GenericDBRef;
import nl.knaw.huygens.repository.model.storage.RelatedDocument;
import nl.knaw.huygens.repository.model.storage.RelatedDocuments;
import nl.knaw.huygens.repository.model.storage.Storage;
import nl.knaw.huygens.repository.util.FacetUtils;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RelatedDocuments({
  @RelatedDocument(type = Person.class, accessors = {"persons", "person"}),
  @RelatedDocument(type = Person.class, accessors = {"marginals", "annotators", "person"}),
  @RelatedDocument(type = Person.class, accessors = {"script", "scribe", "person"}),
  @RelatedDocument(type = Text.class, accessors = {"textUnits", "text"})
})
public class Codex extends Document {
  @Override
  @IndexAnnotation(fieldName="facet_sort_locations")
  public String getDescription() {
    return Joiner.on("; ").skipNulls().join(locations);
  }

  private List<Location> locations = Lists.newArrayList();
  private List<Identifier> identifiers = Lists.newArrayList();

  private String thumbnailInfo = "";

  private String contentSummary = "";
  private List<TextUnitReference> textUnits = Lists.newArrayList();

  private Date date = new Date();
  private List<PlaceAndDate> provenance = Lists.newArrayList();
  private PlaceInfo origin = new PlaceInfo();
  private String dateAndLocaleRemarks = "";

  private String marginalsSummary = "";
  private List<Marginals> marginals = Lists.newArrayList();

  private MarginalQuantities marginalQuantities = new MarginalQuantities();

  private int folia;
  private Dimensions bookDimensions = new Dimensions();
  private List<PageLayout> pageLayouts = Lists.newArrayList();
  private String quireStructure = "";
  private String layoutRemarks = "";
  private Script script;

  private List<PersonReference> persons = Lists.newArrayList();

  private List<String> bibliographies = Lists.newArrayList();
  public List<String> URLs = Lists.newArrayList();

  private List<String> interestingFor = Lists.newArrayList();

  private String examinationLevel = "";
  private Map<String, String> userRemarks = Maps.newHashMap();


  @IndexAnnotation(isFaceted = true, accessors = {"institute"})
  public List<Location> getLocations() {
    return locations;
  }
  public void setLocations(List<Location> locations) {
    this.locations = locations;
  }

  @IndexAnnotation(isFaceted = true, accessors = {"type"})
  public List<Identifier> getIdentifiers() {
    return identifiers;
  }
  public void setIdentifiers(List<Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  public String getThumbnailInfo() {
    return thumbnailInfo;
  }
  public void setThumbnailInfo(String thumbnailInfo) {
    this.thumbnailInfo = thumbnailInfo;
  }
  public String getContentSummary() {
    return contentSummary;
  }
  public void setContentSummary(String contentSummary) {
    this.contentSummary = contentSummary;
  }

  @IndexAnnotations({
    @IndexAnnotation(accessors = {"text", "getItem", "getAuthors", "person", "getItem", "name"}, fieldName = "facet_s_text_author", isFaceted = true),
    @IndexAnnotation(accessors = {"text", "getItem", "getAuthors"}, customIndexer = PersonIndexer.class, isFaceted = true),
    @IndexAnnotation(accessors = {"text", "getItem", "getTitle"}, fieldName = "facet_s_text_title", isFaceted = true),
    @IndexAnnotation(accessors = {"text", "getItem", "getContentTypes"}, fieldName = "facet_s_text_content_type", isFaceted = true),
    @IndexAnnotation(accessors = {"text", "getItem", "getPeriod"}, fieldName = "facet_s_text_period", isFaceted = true),
    @IndexAnnotation(accessors = {"stateOfPreservation"}, fieldName = "facet_s_text_state", isFaceted = true)
  })
  public List<TextUnitReference> getTextUnits() {
    return textUnits;
  }
  public void setTextUnits(List<TextUnitReference> textUnits) {
    this.textUnits = textUnits;
  }

  @IndexAnnotations({
    @IndexAnnotation(accessors = {"date", "getFromYear"}, fieldName = "facet_i_date_lo", isFaceted = true, canBeEmpty = true),
    @IndexAnnotation(accessors = {"date", "getToYear"}, fieldName = "facet_i_date_hi", isFaceted = true, canBeEmpty = true)
  })
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }

  @JsonIgnore
  @IndexAnnotation
  public boolean getHasDate() {
    return date != null && date.date != null && date.date.isValid();
  }


  public String getMarginalsSummary() {
    return marginalsSummary;
  }
  public void setMarginalsSummary(String marginalsSummary) {
    this.marginalsSummary = marginalsSummary;
  }

  @IndexAnnotations({
    @IndexAnnotation(accessors = {"typology", "type"}, fieldName = "facet_s_marg_typology", isFaceted = true),
    @IndexAnnotation(accessors = {"typology", "quantification"}, fieldName = "facet_s_marg_typology_quant", isFaceted = true, canBeEmpty = true),
    @IndexAnnotation(accessors = {"specificPhenomena", "type"}, fieldName = "facet_s_marg_phenomena", isFaceted = true),
    @IndexAnnotation(accessors = {"specificPhenomena", "quantification"}, fieldName = "facet_s_marg_phenomena_quant", isFaceted = true, canBeEmpty = true),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marg_loc_region", accessors = {"origins", "place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marg_loc_place", accessors = {"origins", "place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marg_loc_scriptorium", accessors = {"origins", "place", "getScriptorium"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_region", accessors = {"origins", "place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_place", accessors = {"origins", "place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scriptorium", accessors = {"origins", "place", "getScriptorium"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marg_hands", accessors = {"handCount"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marg_script_type", accessors = {"scripts"}),
    @IndexAnnotation(accessors = {"date", "getFromYear"}, fieldName = "facet_i_marg_date_lo", isFaceted = true, canBeEmpty = true),
    @IndexAnnotation(accessors = {"date", "getToYear"}, fieldName = "facet_i_marg_date_hi", isFaceted = true, canBeEmpty = true),
    @IndexAnnotation(accessors = {"annotators", "person", "getItem", "name"}, fieldName = "facet_s_annotator", isFaceted = true),
    @IndexAnnotation(accessors = {"annotators"}, customIndexer = PersonIndexer.class, isFaceted = true),
    @IndexAnnotation(accessors = {"languages"}, fieldName = "facet_s_marg_languages", isFaceted = true)
  })
  public List<Marginals> getMarginals() {
    return marginals;
  }
  public void setMarginals(List<Marginals> marginals) {
    this.marginals = marginals;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_b_marg_has_date")
  public boolean getMarginalHasDate() {
    if (marginals == null || marginals.size() == 0) {
      return false;
    }
    for (Marginals m : marginals) {
      if (m.date != null && m.date.isValid()) {
        return true;
      }
    }
    return false;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_s_marg_firstpage", isFaceted = true)
  public String firstPageCategory() {
    if (marginalQuantities == null) {
      return null;
    }
    long r = Math.round(((double) marginalQuantities.firstPagesWithMarginals) / (double) marginalQuantities.firstPagesConsidered * 100.0);
    return FacetUtils.categorizeNumeric(r, 20, 0, 100);
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_s_marg_mostfilled", isFaceted = true)
  public String mostFilledCategory() {
    if (marginalQuantities == null) {
      return null;
    }
    long r = Math.round(marginalQuantities.mostFilledPagePctage);
    return FacetUtils.categorizeNumeric(r, 20, 0, 100);
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_s_marg_empty", isFaceted = true)
  public String emptyCategory() {
    if (marginalQuantities == null) {
      return null;
    }
    long r = Math.round(((double) marginalQuantities.totalBlankPages) / (double) folia * 100.0);
    return FacetUtils.categorizeNumeric(r, 20, 0, 100);
  }

  public MarginalQuantities getMarginalQuantities() {
    return marginalQuantities;
  }
  public void setMarginalQuantities(MarginalQuantities marginalQuantities) {
    this.marginalQuantities = marginalQuantities;
  }

  @IndexAnnotations({
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_prov_region", accessors = {"place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_prov_place", accessors = {"place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_prov_scriptorium", accessors = {"place", "getScriptorium"}),

    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_region", accessors = {"place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_place", accessors = {"place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scriptorium", accessors = {"place", "getScriptorium"})
  })
  public List<PlaceAndDate> getProvenance() {
    return provenance;
  }
  public void setProvenance(List<PlaceAndDate> provenance) {
    this.provenance = provenance;
  }
  @IndexAnnotations({
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_origin_region", accessors = {"place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_origin_place", accessors = {"place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_origin_scriptorium", accessors = {"place", "getScriptorium"}),

    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_region", accessors = {"place", "getRegion"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_place", accessors = {"place", "getPlace"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scriptorium", accessors = {"place", "getScriptorium"})
  })
  public PlaceInfo getOrigin() {
    return origin;
  }
  public void setOrigin(PlaceInfo origin) {
    this.origin = origin;
  }

  public String getDateAndLocaleRemarks() {
    return dateAndLocaleRemarks;
  }
  public void setDateAndLocaleRemarks(String dateAndLocaleRemarks) {
    this.dateAndLocaleRemarks = dateAndLocaleRemarks;
  }

  public int getFolia() {
    return folia;
  }
  public void setFolia(int folia) {
    this.folia = folia;
  }

  @JsonIgnore
  @IndexAnnotation(isFaceted = true, fieldName = "facet_s_folia")
  public String getFoliaCount() {
    return FacetUtils.categorizeNumeric(folia, 30);
  }


  @IndexAnnotations({
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_height", accessors = {"getHeightCategory"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_width", accessors = {"getWidthCategory"})
  })
  public Dimensions getBookDimensions() {
    return bookDimensions;
  }
  public void setBookDimensions(Dimensions bookDimensions) {
    this.bookDimensions = bookDimensions;
  }

  @IndexAnnotation(isFaceted = true, fieldName = "facet_s_columns", accessors = {"getColumnCategory"})
  public List<PageLayout> getPageLayouts() {
    return pageLayouts;
  }
  public void setPageLayouts(List<PageLayout> pageLayouts) {
    this.pageLayouts = pageLayouts;
  }

  @JsonIgnore
  @IndexAnnotation(isFaceted = true, fieldName = "facet_s_marginalspace")
  public String getMarginalSpaceCategory() {
    if (pageLayouts != null) {
      int foliaTotal = 0;
      long space = 0;
      for (PageLayout layout : pageLayouts) {
        foliaTotal += (layout.foliaCount > 0 ? layout.foliaCount : 1);
        space += layout.whitespacePercentage() * (layout.foliaCount > 0 ? layout.foliaCount : 1);
      }
      if (foliaTotal == 0 || space == 0) {
        return null;
      }
      space = space / foliaTotal;
      return FacetUtils.categorizeNumeric(space, 10, 0, 50);
    }
    return null;
  }

  @JsonIgnore
  @IndexAnnotation(isFaceted = true, fieldName = "facet_s_lines")
  public String getLinesCategory() {
    if (pageLayouts != null) {
      int foliaTotal = 0;
      long lines = 0;
      for (PageLayout layout : pageLayouts) {
        foliaTotal += (layout.foliaCount > 0 ? layout.foliaCount : 1);
        lines += layout.getLinesAvg() * (layout.foliaCount > 0 ? layout.foliaCount : 1);
      }
      if (foliaTotal == 0 || lines == 0) {
        return null;
      }
      lines = lines / foliaTotal;
      return FacetUtils.categorizeNumeric(lines, 10, 0, 50);
    }
    return null;
  }

  public String getQuireStructure() {
    return quireStructure;
  }
  public void setQuireStructure(String quireStructure) {
    this.quireStructure = quireStructure;
  }
  public String getLayoutRemarks() {
    return layoutRemarks;
  }
  public void setLayoutRemarks(String layoutRemarks) {
    this.layoutRemarks = layoutRemarks;
  }
  @IndexAnnotations({
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scripttype", accessors = {"types"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scripthands", accessors = {"handsCount"}),
    @IndexAnnotation(isFaceted = true, fieldName = "facet_s_scribe", accessors = {"scribes", "person", "getItem", "name"}),
    @IndexAnnotation(isFaceted = true, customIndexer = PersonIndexer.class, accessors = {"scribes"})
  })
  public Script getScript() {
    return script;
  }
  public void setScript(Script script) {
    this.script = script;
  }


  @IndexAnnotation(isFaceted = true, customIndexer = PersonIndexer.class)
  public List<PersonReference> getPersons() {
    return persons;
  }
  public void setPersons(List<PersonReference> persons) {
    this.persons = persons;
  }
  public List<String> getBibliographies() {
    return bibliographies;
  }
  public void setBibliographies(List<String> bibliographies) {
    this.bibliographies = bibliographies;
  }

  @JsonProperty("URLs")
  public List<String> getURLs() {
    return URLs;
  }
  @JsonProperty("URLs")
  public void setURLs(List<String> URLs) {
    this.URLs = URLs;
  }

  @IndexAnnotation(isFaceted = true)
  public List<String> getInterestingFor() {
    return interestingFor;
  }
  public void setInterestingFor(List<String> interestingFor) {
    this.interestingFor = interestingFor;
  }

  public String getExaminationLevel() {
    return examinationLevel;
  }
  public void setExaminationLevel(String examinationLevel) {
    this.examinationLevel = examinationLevel;
  }

  public Map<String, String> getUserRemarks() {
    return userRemarks;
  }
  public void setUserRemarks(Map<String, String> userRemarks) {
    this.userRemarks = userRemarks;
  }

  public static class PageLayout {
    public List<Integer> columnWidths = Lists.newArrayList();
    public List<Integer> blockHeights = Lists.newArrayList();
    public int marginLeft;
    public int marginRight;
    public int marginTop;
    public int marginBottom;

    public int textWidthMin;
    public int textWidthMax;
    public int textHeightMin;
    public int textHeightMax;

    public int linesMin;
    public int linesMax;

    public int lineHeight;
    public int foliaCount;
    public String pages = "";

    public String remarks = "";

    @JsonIgnore
    public double getLinesAvg() {
      return (linesMin + linesMax) / 2.0;
    }

    @JsonIgnore
    public long whitespacePercentage() {
      if (columnWidths == null || blockHeights == null) {
        return 0;
      }
      long columnWidth = 0;
      long pageWidth = marginLeft + marginRight;
      long blockHeight = 0;
      long pageHeight = marginTop + marginBottom;
      for (int i = 0; i < columnWidths.size(); i++) {
        int w = columnWidths.get(i);
        if (i % 2 == 0) {
          columnWidth += w;
        }
        pageWidth += w;
      }
      for (int i = 0; i < blockHeights.size(); i++) {
        int h = blockHeights.get(i);
        if (i % 2 == 0) {
          blockHeight += h;
        }
        pageHeight += h;
      }
      long pageSize = pageHeight * pageWidth;
      if (pageSize == 0) {
        return 0;
      }

      return Math.round((pageSize - (columnWidth * blockHeight)) / pageSize * 100);
    }

    @JsonIgnore
    public String getColumnCategory() {
      if (columnWidths == null || columnWidths.size() == 0) {
        return "(empty)";
      }
      int n = (columnWidths.size() + 1) / 2;
      if (n < 3) {
        return String.format("%d columns", n);
      }
      return "more columns";
    }
  }

  public static class Dimensions {
    public long width;
    public long height;

    @JsonIgnore
    public String getHeightCategory() {
      return FacetUtils.categorizeNumeric(height, 50);
    }

    @JsonIgnore
    public String getWidthCategory() {
      return FacetUtils.categorizeNumeric(width, 50);
    }
  }

  public static class Script {
    public List<String> types = Lists.newArrayList();
    public String typesRemarks = "";
    public String characteristics = "";
    public String characteristicsRemarks = "";
    public String handsCount = "";
    public String handsRange = "";

    public List<PersonReference> scribes = Lists.newArrayList();

    public String scribeRemarks = "";
    public String additionalRemarks = "";

  }

  public static class Identifier {
    public String identifier = "";
    public String type = "";
  }

  public static class Location {
    public String shelfmark = "";
    public String pages = "";
    public String institute = "";
    @JsonIgnore
    @Override
    public String toString() {
      String rv = institute;
      if (StringUtils.isNotBlank(shelfmark)) {
        rv += ", " + shelfmark;
      }
      if (StringUtils.isNotBlank(pages)) {
        rv += " (" + pages + ")";
      }
      return rv;
    }
  }

  public static class TextUnitReference {
    public String titleInCodex = "";
    public String stateOfPreservation = "";
    public String stateOfPreservationRemarks = "";
    public String incipit = "";
    public String explicit = "";
    public String remarks = "";
    public String pages = "";

    public GenericDBRef<Text> text;
  }

  public static class Date {
    public String source = "";
    public Datable date = new Datable("");

    public void setDate(String edtf) {
      date = new Datable(edtf);
    }
  }

  public static class PlaceAndDate extends PlaceInfo {
    public Datable date = new Datable("");
    public String dateInfo = "";
  }

  public static class PlaceInfo {
    public Place place = new Place();
    public String remarks = "";
    public boolean certain;
  }

  public static class MarginalQuantities {
    public int firstPagesWithMarginals;
    public int firstPagesConsidered;

    public double mostFilledPagePctage;
    public String mostFilledPageDesignation = "";

    public int totalBlankPages;
  }

  @Override
  @JsonIgnore
  public void fetchAll(Storage storage) {
    int desiredSize = (persons == null ? 0 : persons.size()) +
                      (textUnits == null ? 0 : textUnits.size());
    List<GenericDBRef<Person>> personRefs = Lists.newArrayListWithCapacity(desiredSize + 10);
    if (textUnits != null) {
      List<GenericDBRef<Text>> textRefs = Lists.newArrayListWithExpectedSize(textUnits.size());
      for (TextUnitReference ref : textUnits) {
        if (ref.text != null) {
          textRefs.add(ref.text);
        }
      }
      if (textRefs.size() > 0) {
        storage.fetchAll(textRefs, Text.class);
        // Then also get all the required authors in one go:
        scrapeAuthors(personRefs, textRefs);
      }
    }

    if (persons != null) {
      for (PersonReference ref : persons) {
        personRefs.add(ref.person);
      }
    }

    scrapeScribes(personRefs);
    scrapeMarginalPersons(personRefs);

    if (personRefs.size() > 0) {
      storage.fetchAll(personRefs, Person.class);
    }
  }
  private void scrapeAuthors(List<GenericDBRef<Person>> personRefs, List<GenericDBRef<Text>> textRefs) {
    for (GenericDBRef<Text> ref : textRefs) {
      Text text = ref.getItem();
      if (text == null) {
        continue;
      }
      List<PersonReference> authors = text.getAuthors();
      if (authors != null) {
        for (PersonReference author : authors) {
          personRefs.add(author.person);
        }
      }
    }
  }
  private void scrapeScribes(List<GenericDBRef<Person>> personRefs) {
    if (script != null && script.scribes != null) {
      for (PersonReference ref : script.scribes) {
        personRefs.add(ref.person);
      }
    }
  }

  private void scrapeMarginalPersons(List<GenericDBRef<Person>> personRefs) {
    if (marginals != null) {
      for (Marginals m : marginals) {
        if (m.annotators != null) {
          for (PersonReference ref : m.annotators) {
            personRefs.add(ref.person);
          }
        }
      }
    }
  }
}
