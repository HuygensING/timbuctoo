package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.sleepycat.bind.tuple.TupleBinding;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;

import java.io.PrintWriter;
import java.util.stream.Stream;

public class MigrateUpdatedPerPatchStoresTask extends Task {
  private final BdbEnvironmentCreator dataStoreFactory;
  private final DataSetRepository dataSetRepository;

  public MigrateUpdatedPerPatchStoresTask(BdbEnvironmentCreator dataStoreFactory, DataSetRepository dataSetRepository) {
    super("migrateUpdatedPerPatchStores");
    this.dataStoreFactory = dataStoreFactory;
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      output.println("Migrate dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();

      final UpdatedPerPatchStore updatedPerPatchStore = dataSet.getUpdatedPerPatchStore();
      final BdbWrapper<Integer, String> oldStore = dataStoreFactory.getDatabase(
          dataSet.getMetadata().getOwnerId(),
          dataSet.getMetadata().getDataSetId(),
          "updatedPerPatchOld",
          true,
          TupleBinding.getPrimitiveBinding(Integer.class),
          TupleBinding.getPrimitiveBinding(String.class),
          new IsCleanHandler<>() {
            @Override
            public Integer getKey() {
              return Integer.MAX_VALUE;
            }

            @Override
            public String getValue() {
              return "isClean";
            }
          }
      );

      try (Stream<Tuple<Integer, String>> stream = oldStore.databaseGetter().getAll()
                                                           .getKeysAndValues(oldStore.keyValueConverter(Tuple::new))) {
        stream.forEach(values -> {
          try {
            updatedPerPatchStore.put(values.getLeft(), values.getRight());
          } catch (DatabaseWriteException e) {
            e.printStackTrace();
          }
        });
      }

      updatedPerPatchStore.commit();

      oldStore.empty();
      oldStore.close();

      output.println("Finished migration of dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();
    }
  }
}
