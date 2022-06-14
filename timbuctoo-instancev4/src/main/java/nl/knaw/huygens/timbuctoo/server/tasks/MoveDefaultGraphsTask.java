package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.PredicateMutationRdfPatcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.BasicRdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation.replace;

public class MoveDefaultGraphsTask extends Task {
  private final DataSetRepository dataSetRepository;

  public MoveDefaultGraphsTask(DataSetRepository dataSetRepository) {
    super("moveDefaultGraphs");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    boolean printDataSet;
    boolean hasMutations;
    PredicateMutation predicateMutation;

    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      printDataSet = true;
      hasMutations = false;
      predicateMutation = new PredicateMutation();

      String baseUri = dataSet.getMetadata().getBaseUri().endsWith("/") ?
          dataSet.getMetadata().getBaseUri() :
          dataSet.getMetadata().getBaseUri() + "/";

      Optional<Graph> graphOne = Optional.of(new Graph(baseUri));
      Optional<Graph> graphTwo = Optional.of(new Graph(baseUri.substring(0, baseUri.length() - 1)));
      try (Stream<CursorQuad> quads = dataSet.getQuadStore().getAllQuads()
                                             .filter(quad -> quad.inGraph(graphOne) || quad.inGraph(graphTwo))) {
        for (CursorQuad quad : (Iterable<CursorQuad>) quads::iterator) {
          if (printDataSet) {
            printDataSet = false;
            output.println(dataSet.getMetadata().getCombinedId());
            output.println();
            output.flush();
          }

          output.println(quad.toString());

          if (quad.getSubject().equals(baseUri) && quad.getDirection() == Direction.OUT) {
            hasMutations =  true;
            predicateMutation.entity(baseUri, replace(quad.getPredicate(), new PredicateMutation.Value(
                quad.getObject(),
                quad.getValuetype().orElse(null),
                quad.getLanguage().orElse(null),
                null
            )));
          }
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
