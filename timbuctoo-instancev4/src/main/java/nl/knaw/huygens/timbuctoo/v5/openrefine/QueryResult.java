package nl.knaw.huygens.timbuctoo.v5.openrefine;

public class QueryResult {
  
  public String id;
  public String name;
  public String[] type;
  public double score;
  public boolean match;
  //"\"" + key + "\": {";
  //      result += "\"result\" : [ { \"id\" : " + key.substring(1) + ",";
  //      result += "\"name\" : " + name.toString() + ",";
  //      result += "\"type\" : [\"String\"] ,";
  //      result += "\"score\" : 1.0 ,";
  //      result += "\"match\" : true";
  //      result += " } ] } ,";
}
