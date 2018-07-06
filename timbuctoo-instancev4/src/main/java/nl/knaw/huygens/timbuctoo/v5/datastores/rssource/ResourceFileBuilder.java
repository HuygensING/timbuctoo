package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.dataset.CurrentStateRetriever;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceFileBuilder {

  private final String graph;
  private DataSetQuadGenerator dataSetQuadGenerator;

  public ResourceFileBuilder(String graph) {
    this.graph = graph;
    this.dataSetQuadGenerator = new DataSetQuadGenerator(graph);
  }

  public List<String> retrieveData(CurrentStateRetriever currentStateRetriever) {
    List<String> data = new ArrayList<>();

    List<CursorQuad> quads = currentStateRetriever.retrieveData();

    quads.forEach(quad -> {
      Optional<String> dataType = quad.getValuetype();
      if (dataType == null || !dataType.isPresent()) {
        data.add(
          dataSetQuadGenerator.onRelation(quad.getSubject(), quad.getPredicate(), quad.getObject(), graph)
        );
      } else {
        Optional<String> language = quad.getLanguage();
        String dataTypeString = dataType.get();
        if (language != null && language.isPresent() && dataTypeString.equals(RdfConstants.LANGSTRING)) {
          data.add(
            dataSetQuadGenerator.onLanguageTaggedString(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              language.get(),
              graph
            )
          );
        } else {
          data.add(
            dataSetQuadGenerator.onValue(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              dataTypeString,
              graph
            )
          );
        }
      }
    });

    return data;
  }
}
