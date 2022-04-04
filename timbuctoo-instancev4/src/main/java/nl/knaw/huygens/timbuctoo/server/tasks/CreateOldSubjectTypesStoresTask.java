package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class CreateOldSubjectTypesStoresTask extends Task {
  private final DataSetRepository dataSetRepository;

  public CreateOldSubjectTypesStoresTask(DataSetRepository dataSetRepository) {
    super("createOldSubjectTypesStores");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      output.println("Create for dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();

      final OldSubjectTypesStore oldSubjectTypesStore = dataSet.getOldSubjectTypesStore();
      final BdbTruePatchStore truePatchStore = dataSet.getTruePatchStore();

      try (final Stream<Integer> versionsStream = dataSet.getUpdatedPerPatchStore().getVersions()) {
        List<Integer> versions = versionsStream.sorted().collect(Collectors.toList());
        for (int version : versions) {
          truePatchStore.getChangesOfVersion(version, true)
                        .filter(quad -> quad.getPredicate().equals(RDF_TYPE) && quad.getDirection() == Direction.OUT)
                        .forEach(quad -> {
                          try {
                            for (int v : versions) {
                              if (v < version) {
                                oldSubjectTypesStore.delete(quad.getSubject(), quad.getObject(), v);
                              }
                            }
                          } catch (DatabaseWriteException e) {
                            e.printStackTrace();
                          }
                        });

          truePatchStore.getChangesOfVersion(version, false)
                        .filter(quad -> quad.getPredicate().equals(RDF_TYPE) && quad.getDirection() == Direction.OUT)
                        .forEach(quad -> {
                          try {
                            oldSubjectTypesStore.put(quad.getSubject(), quad.getObject(), version);
                          } catch (DatabaseWriteException e) {
                            e.printStackTrace();
                          }
                        });
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      oldSubjectTypesStore.commit();

      output.println("Created for dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();
    }
  }
}
