package nl.knaw.huygens.timbuctoo.model.dcar;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Archiver;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.atlg.XRelated;
import nl.knaw.huygens.timbuctoo.model.util.PeriodHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class DCARArchiver extends Archiver {

  /** Migration: Name of source file */
  private String origFilename;

  /** ING Forms: "Name" */
  private String nameNld;

  /** ING Forms: "English name"; text searchable */
  public String nameEng;

  /** ING Forms: "Begin date"; date facet */
  private String beginDate;

  /** ING Forms: "End date"; date facet */
  private String endDate;

  /** ING Forms: "Period description" */
  private String periodDescription;

  /** ING Forms: "History/functions/occupations/activities"; text searchable */
  private String history;

  /** ING Forms: "Title(s) related archive(s); as relation " */

  /** ING Forms: "Title(s) related creator(s); as relation " */

  /** ING Forms: "Keyword(s) geography"; as relation ; place facet */

  /** ING Forms: "Keyword(s) subject"; as relation ; subject facet */

  /** ING Forms: "Keyword(s) person"; as relation ; person facet */

  /** ING Forms: "Remarks"; text searchable */
  private String notes;

  /** ING Forms: "Literature" */
  private String literature;

  /** ING Forms: "Record made by-" */
  private String madeBy;

  /** ING Forms: "Reminders" ??? */
  private String reminders;

  /** ING Forms: "Binnenkomende relaties" */
  private XRelated[] related;

  /** ING Forms: ??? ("person", "family") */
  private List<String> types;

  public DCARArchiver() {
    types = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getNameEng();
  }

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public String getNameNld() {
    return nameNld;
  }

  public void setNameNld(String name) {
    nameNld = name;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_name", canBeEmpty = true, isFaceted = false, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false, isSortable = false) })
  public String getNameEng() {
    return nameEng;
  }

  public void setNameEng(String name) {
    nameEng = name;
  }

  @IndexAnnotation(fieldName = "dynamic_s_begin_date", canBeEmpty = true, isFaceted = true, facetType = FacetType.DATE)
  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String date) {
    beginDate = date;
  }

  @IndexAnnotation(fieldName = "dynamic_s_end_date", canBeEmpty = false, isFaceted = true, facetType = FacetType.DATE)
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    endDate = date;
  }

  @JsonIgnore
  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_period", canBeEmpty = true, isFaceted = false, facetType = FacetType.PERIOD, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_s_period", canBeEmpty = true, isFaceted = true, facetType = FacetType.PERIOD, isSortable = false) })
  public String getActivePeriod() {
    return PeriodHelper.createPeriod(beginDate, endDate);
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String description) {
    periodDescription = description;
  }

  @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false)
  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  @JsonIgnore
  public List<EntityRef> getRelatedArchives() {
    return getRelations().get("is_creator_of");
  }

  @JsonIgnore
  public List<EntityRef> getRelatedArchivers() {
    return getRelations().get("has_sibling_archiver");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_place", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPlaceKeywords() {
    return getRelations().get("has_place");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_subject", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getSubjectKeywords() {
    return getRelations().get("has_keyword");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_person", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPersons() {
    return getRelations().get("has_person");
  }

  @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false)
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getLiterature() {
    return literature;
  }

  public void setLiterature(String literature) {
    this.literature = literature;
  }

  public String getMadeBy() {
    return madeBy;
  }

  public void setMadeBy(String madeBy) {
    this.madeBy = madeBy;
  }

  public String getReminders() {
    return reminders;
  }

  public void setReminders(String reminders) {
    this.reminders = reminders;
  }

  public XRelated[] getRelated() {
    return related;
  }

  public void setRelated(XRelated[] related) {
    this.related = related;
  }

  @IndexAnnotation(fieldName = "dynamic_s_type", accessors = { "toString" }, canBeEmpty = true, isFaceted = true)
  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public void addType(String type) {
    types.add(type);
  }

}
