package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.rssource.DataSetQuadGenerator;
import nl.knaw.huygens.timbuctoo.graphql.mutations.PredicateMutationRdfPatcher;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.rdfio.implementations.BasicRdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.util.Graph;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.graphql.mutations.dto.PredicateMutation.replace;

public class MoveDefaultGraphsTask extends Task {
  private static final DataSetQuadGenerator DATASET_QUAD_GENERATOR = new DataSetQuadGenerator();

  private final DataSetRepository dataSetRepository;

  public MoveDefaultGraphsTask(DataSetRepository dataSetRepository) {
    super("moveDefaultGraphs");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    boolean hasMutations;
    PredicateMutation predicateMutation;

    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      hasMutations = false;
      predicateMutation = new PredicateMutation();

      String baseUri = dataSet.getMetadata().getBaseUri().endsWith("/") ?
          dataSet.getMetadata().getBaseUri() :
          dataSet.getMetadata().getBaseUri() + "/";

      Optional<Graph> graphOne = Optional.of(new Graph(baseUri));
      Optional<Graph> graphTwo = Optional.of(new Graph(baseUri.substring(0, baseUri.length() - 1)));
      try (Stream<CursorQuad> quads = dataSet.getQuadStore().getAllQuads()
                                             .filter(quad -> quad.getDirection() == Direction.OUT)
                                             .filter(quad -> quad.inGraph(graphOne) || quad.inGraph(graphTwo))) {
        for (CursorQuad quad : (Iterable<CursorQuad>) quads::iterator) {
          if (!hasMutations) {
            hasMutations = true;
            output.println(dataSet.getMetadata().getCombinedId());
            output.println();
            output.flush();
          }

          Optional<String> dataType = quad.getValuetype();
          Optional<String> language = quad.getLanguage();
          if (dataType == null || dataType.isEmpty()) {
            output.println(DATASET_QUAD_GENERATOR.onRelation(
                quad.getSubject(),
                quad.getPredicate(),
                quad.getObject(),
                quad.getGraph().orElse(null)
            ));
          } else if (language != null && language.isPresent() && dataType.get().equals(RdfConstants.LANGSTRING)) {
            output.println(DATASET_QUAD_GENERATOR.onLanguageTaggedString(
                quad.getSubject(),
                quad.getPredicate(),
                quad.getObject(),
                language.get(),
                quad.getGraph().orElse(null)
            ));
          } else {
            output.println(DATASET_QUAD_GENERATOR.onValue(
                quad.getSubject(),
                quad.getPredicate(),
                quad.getObject(),
                dataType.get(),
                quad.getGraph().orElse(null)
            ));
          }

          predicateMutation.entity(quad.getSubject(), replace(quad.getPredicate(), new PredicateMutation.Value(
              quad.getObject(),
              quad.getValuetype().orElse(null),
              quad.getLanguage().orElse(null),
              null
          )));
        }

        if (hasMutations) {
          output.println();
          output.flush();

          PredicateMutationRdfPatcher rdfPatcher = new PredicateMutationRdfPatcher(predicateMutation);
          rdfPatcher.sendQuads(new BasicRdfPatchSerializer(output), null, dataSet);
          output.println();
          output.flush();

          if (!parameters.containsKey("dry-run")) {
            dataSet.getImportManager().generateLog(baseUri, null, rdfPatcher).get();
          }
        }
      }
    }
  }
}
