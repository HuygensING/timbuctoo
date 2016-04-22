package nl.knaw.huygens.timbuctoo.search.description;


import java.util.List;

public interface IndexDescription {

  List<String> getSortIndexes(List<String> vertexTypes);
}
