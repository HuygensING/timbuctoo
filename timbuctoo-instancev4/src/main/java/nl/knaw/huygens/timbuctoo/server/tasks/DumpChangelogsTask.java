package nl.knaw.huygens.timbuctoo.server.tasks;

import com.sleepycat.bind.tuple.TupleBinding;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.rssource.ChangeListBuilder;
import nl.knaw.huygens.timbuctoo.v5.filestorage.ChangeLogStorage;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;

public class DumpChangelogsTask extends Task {
  private final DataSetRepository dataSetRepository;
  private final BdbEnvironmentCreator dataStoreFactory;

  public DumpChangelogsTask(DataSetRepository dataSetRepository, BdbEnvironmentCreator dataStoreFactory) {
    super("dumpChangelogs");
    this.dataSetRepository = dataSetRepository;
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      forDataset(dataSet, output);
    }
  }

  private void forDataset(DataSet dataSet, PrintWriter output) {
    try {
      output.println("Dump changelogs for dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();

      final UpdatedPerPatchStore updatedPerPatchStore = dataSet.getUpdatedPerPatchStore();
      final BdbTruePatchStore truePatchStore = new BdbTruePatchStore(version ->
          dataStoreFactory.getDatabase(
              dataSet.getMetadata().getOwnerId(),
              dataSet.getMetadata().getDataSetId(),
              "truePatch" + version,
              true,
              TupleBinding.getPrimitiveBinding(String.class),
              TupleBinding.getPrimitiveBinding(String.class),
              new StringStringIsCleanHandler()
          ), updatedPerPatchStore
      );

      final List<Integer> versions;
      try (Stream<Integer> versionsStream = updatedPerPatchStore.getVersions()) {
        versions = versionsStream.collect(Collectors.toList());
      }

      final ChangeLogStorage changeLogStorage = dataSet.getChangeLogStorage();
      for (int version : versions) {
        try (Stream<CursorQuad> changesStream = truePatchStore.retrieveChanges(version)) {
          if (changesStream.findAny().isPresent()) {
            output.println("Version " + version);
            output.flush();

            try (OutputStream out = changeLogStorage.getChangeLogOutputStream(version)) {
              ChangeListBuilder changeListBuilder = new ChangeListBuilder();
              BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

              try (Stream<CursorQuad> changes = truePatchStore.retrieveChanges(version);
                   Stream<String> data = changeListBuilder.retrieveChanges(changes)) {
                for (Iterator<String> dataIt = data.iterator(); dataIt.hasNext(); ) {
                  writer.write(dataIt.next());
                }
              }

              writer.flush();
            }
          }
        }

        output.println("Finished version " + version);
        output.flush();
      }

      output.println("Written changelogs; now emptying store");
      output.flush();

      truePatchStore.empty();
      truePatchStore.commit();
      truePatchStore.close();

      for (int version : versions) {
        dataStoreFactory.removeDatabase(
            dataSet.getMetadata().getOwnerId(),
            dataSet.getMetadata().getDataSetId(),
            "truePatch" + version);
      }

      output.println("Emptied and removed stores");
      output.flush();
    } catch (Exception e) {
      output.println("Failed to dump changelogs for dataset: " + dataSet.getMetadata().getCombinedId());
      output.println("Error message: " + e.getMessage());
      e.printStackTrace(output);
      output.flush();
    }
  }

  private static class BdbTruePatchStore {
    private final HashMap<Integer, BdbWrapper<String, String>> bdbWrappers;

    public BdbTruePatchStore(DatabaseCreator databaseCreator,
                             UpdatedPerPatchStore updatedPerPatchStore) throws DataStoreCreationException {
      bdbWrappers = new HashMap<>();
      try (final Stream<Integer> versions = updatedPerPatchStore.getVersions()) {
        for (Integer version : versions.collect(Collectors.toList())) {
          bdbWrappers.put(version, databaseCreator.createDatabase(String.valueOf(version)));
        }
      } catch (BdbDbCreationException e) {
        throw new DataStoreCreationException(e);
      }
    }

    private Stream<CursorQuad> getChangesOfVersion(int version, boolean assertions) {
      // FIXME partialKey does not work well with endsWidth, it stops the iterator with the first match
      // See issue T141 on https://github.com/knaw-huc/backlogs/blob/master/structured-data.txt
      if (bdbWrappers.containsKey(version)) {
        final BdbWrapper<String, String> bdbWrapper = bdbWrappers.get(version);
        return bdbWrapper.databaseGetter()
                         .getAll()
                         // .partialKey("\n" + version + "\n" + (assertions ? "1" : "0"), (pf,
                         // key) -> key.endsWith(pf))
                         // .dontSkip()
                         // .forwards()
                         .getKeysAndValues(bdbWrapper.keyValueConverter(Tuple::tuple))
                         .filter(kv -> kv.getLeft().endsWith(version + "\n" + (assertions ? "1" : "0")))
                         .map((value) -> makeCursorQuad(value.getLeft().split("\n")[0], assertions, value.getRight()));
      }
      return Stream.empty();
    }

    private CursorQuad makeCursorQuad(String subject, boolean assertions, String value) {
      String[] parts = value.split("\n", 6);
      Direction direction = parts[1].charAt(0) == '1' ? OUT : IN;
      ChangeType changeType = assertions ? ChangeType.ASSERTED : ChangeType.RETRACTED;
      return CursorQuad.create(
          subject,
          parts[0],
          direction,
          changeType,
          parts[5],
          parts[2].isEmpty() ? null : parts[2],
          parts[3].isEmpty() ? null : parts[3],
          parts[4].isEmpty() ? null : parts[4],
          ""
      );
    }

    public Stream<CursorQuad> retrieveChanges(int version) {
      return Stream.concat(
          getChangesOfVersion(version, false),
          getChangesOfVersion(version, true)
      ).filter(quad -> quad.getDirection().equals(Direction.OUT));
    }

    public void close() {
      try {
        for (BdbWrapper<String, String> bdbWrapper : bdbWrappers.values()) {
          bdbWrapper.close();
        }
      } catch (Exception e) {
        // ignored
      }
    }

    public void commit() {
      bdbWrappers.values().forEach(BdbWrapper::commit);
    }

    public void empty() {
      bdbWrappers.values().forEach(BdbWrapper::empty);
    }
  }

  private interface DatabaseCreator {
    BdbWrapper<String, String> createDatabase(String version) throws BdbDbCreationException;
  }
}
