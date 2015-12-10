package nl.knaw.huygens.timbuctoo.model.charter;

/*
 * #%L
 * Timbuctoo model
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import com.google.common.collect.Maps;
import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;

public class CharterDocument extends Document {

  // vindplaats, opgesplitst:
  private String archief;
  private String fonds;
  private String fondsNaam;
  private String inventarisNummer;
  private String volgNummer;
  private String regestNummer;

  private List<Link> thumbs;

  // URL naar entry in online bronarchief (baseurl+item aanduiding)
  // In Document: private List<Link> links;
  // semi verplichte data
  // de tekst zoals die in de (bestaande) inventaris(sen) staat
  private List<String> inventaristekst;
  // tekst van het regest of de regesten (soms zijn er meerdere)
  private List<String> tekstRegest;
  // de bij het regest gevoegde additionele informatie (bezegeling, afschriften, drukken)
  private String additioneleInformatie;
  // taal waarin het stuk is gesteld
  // relation: hasLanguage

  // Extra data voor zoekfunctie
  private String transcription;
  private String translation;
  // beschrijving met link van afschriften, edities of kopieën
  private String descriptionOfEditions;
  //persoonsnamen en namen van instellingen met, indien van toepassing, vermelding van functie binnen de oorkonde (oorkonder, Urheber, destinataris, geadresseerde, getuigen, zegelaars)
  private String namesInCharter;
  //Overige, nog niet genoemde metadata die reeds in bestaande systemen (zoals Cathago) of catalogi zijn opgenomen; deze worden niet structureel doorzoekbaar gemaakt.
  private String overige;

  public CharterDocument() {
    setResourceType(ResourceType.TEXT);
    thumbs = Lists.newArrayList();
  }

  @IndexAnnotation(fieldName = "dynamic_s_archief", isFaceted = true)
  public String getArchief() {
    return archief;
  }

  public void setArchief(String archief) {
    this.archief = archief;
  }

  @IndexAnnotation(fieldName = "dynamic_s_fonds", isFaceted = true)
  public String getFonds() {
    return fonds;
  }

  public void setFonds(String fonds) {
    this.fonds = fonds;
  }

  public String getInventarisNummer() {
    return inventarisNummer;
  }

  public void setInventarisNummer(String inventarisNummer) {
    this.inventarisNummer = inventarisNummer;
  }

  public String getVolgNummer() {
    return volgNummer;
  }

  public void setVolgNummer(String volgnr) {
    this.volgNummer = volgnr;
  }

  public String getRegestNummer() {
    return regestNummer;
  }

  public void setRegestNummer(String regestNummer) {
    this.regestNummer = regestNummer;
  }

  @IndexAnnotation(fieldName = "dynamic_t_alltext", isFaceted = false)
  public String getAllText() {
    String result = "";
    if (inventaristekst != null) {
      result += Joiner.on(" ").join(inventaristekst);
    }
    if (additioneleInformatie != null) {
      result += " " + additioneleInformatie;
    }
    if (tekstRegest != null) {
      result += " " + Joiner.on(" ").join(tekstRegest);
    }
    return result;
  }

  @Override
  public String getIdentificationName() {
    return getSignature() + " " + Joiner.on(" ").join(inventaristekst);
  }

  //  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_inventaristekst", canBeEmpty = true),//
  //      @IndexAnnotation(fieldName = "dynamic_sort_inventaristekst", canBeEmpty = true, isSortable = false) })
  //  @IndexAnnotation(fieldName = "dynamic_s_inventaristekst", canBeEmpty = true, isFaceted = false)
  public List<String> getInventaristekst() {
    return inventaristekst;
  }

  public void setInventaristekst(List<String> inventaristekst) {
    this.inventaristekst = inventaristekst;
  }

  public List<String> getTekstRegest() {
    return tekstRegest;
  }

  public void setTekstRegest(List<String> tekstRegest) {
    this.tekstRegest = tekstRegest;
  }

  public String getAdditioneleInformatie() {
    return additioneleInformatie;
  }

  public void setAdditioneleInformatie(String additioneleInformatie) {
    this.additioneleInformatie = additioneleInformatie;
  }

  public String getTranscription() {
    return transcription;
  }

  public void setTranscription(String transcription) {
    this.transcription = transcription;
  }

  public String getTranslation() {
    return translation;
  }

  public void setTranslation(String translation) {
    this.translation = translation;
  }

  @IndexAnnotation(fieldName = "dynamic_s_editions", canBeEmpty = true, isFaceted = true)
  public String getDescriptionOfEditions() {
    return descriptionOfEditions;
  }

  public void setDescriptionOfEditions(String descriptionOfEditions) {
    this.descriptionOfEditions = descriptionOfEditions;
  }

  public String getNamesInCharter() {
    return namesInCharter;
  }

  public void setNamesInCharter(String namesInCharter) {
    this.namesInCharter = namesInCharter;
  }

  public String getOverige() {
    return overige;
  }

  @IndexAnnotation(fieldName = "dynamic_t_signature")
  public String getSignature() {
    return archief + "-" + fonds + "-" + inventarisNummer;
  }

  public void setOverige(String overige) {
    this.overige = overige;
  }

  @Override
  @IndexAnnotations({
    @IndexAnnotation(isFaceted = true, fieldName = "dynamic_i_date", facetType = FacetType.RANGE),
    @IndexAnnotation(fieldName = "dynamic_k_date", isSortable = true)
  })
  public Datable getDate() {
    return super.getDate();
  }

  public List<Link> getThumbs() {
    return thumbs;
  }

  public void setThumbs(List<Link> thumbs) {
    this.thumbs = thumbs;
  }

  public void setThumbLabels(List<String> labels) {
    try {
      int index = 0;
      for (String label : labels) {
        thumbs.get(index).setLabel(label);
        index++;
      }
    } catch (IndexOutOfBoundsException ioobe) {}
  }

  @Override
  public Map<String, String> createRelSearchRep(Map<String, String> mappedIndexInformation) {
    Map<String, String> filteredMap = Maps.newTreeMap();
    filteredMap.put("date", new Datable(mappedIndexInformation.get("date")).getFromYear() + "");
    return filteredMap;
  }

  public String getFondsNaam() {
    return fondsNaam;
  }

  public void setFondsNaam(String fondsNaam) {
    this.fondsNaam = fondsNaam;
  }
}
