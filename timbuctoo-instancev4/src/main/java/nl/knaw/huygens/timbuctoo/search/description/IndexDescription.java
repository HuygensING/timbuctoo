package nl.knaw.huygens.timbuctoo.search.description;


import java.util.List;

public interface IndexDescription {

  List<String> getSortIndexPropertyNames(List<String> vertexTypes);
}
