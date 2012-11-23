package nl.knaw.huygens.repository.model;

import java.util.List;

import com.google.common.collect.Lists;

public class Marginals {
  public static class Origin {
    public Place place;
    public boolean certain;
  }

  public static class Aspect {
    public String type;
    public String remarks;
    public String quantification;
  }

  public List<Aspect> typology = Lists.newArrayList();
  public List<Aspect> specificPhenomena = Lists.newArrayList();
  public String typologyRemarks = "";

  public String functionalAspects = "";

  public List<Origin> origins = Lists.newArrayList();

  public String handCount = "";
  public String generalObservations = "";

  public List<PersonReference> annotators = Lists.newArrayList();

  public List<String> languages = Lists.newArrayList();

  public List<String> scripts = Lists.newArrayList();
  public String scriptsRemarks = "";

  public String relativeDate = "";
  public Datable date = new Datable("");

  public String identifier = "";
  public String pages = "";

  public List<String> bibliographies = Lists.newArrayList();
}