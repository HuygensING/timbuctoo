package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.psql;

import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.TruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;

public class PsqlTruePatchStore implements TruePatchStore {

  private static final String GET_VERSION = "SELECT * FROM %s WHERE version=:version AND is_insert=:is_insert";
  private static final String GET_VERSION_SUBJECT = "SELECT * FROM %s " +
      "WHERE version=:version " +
      "AND is_insert=:is_insert " +
      "AND subject=:subject";
  private static final String GET_VERSION_SUBJECT_PRED = "SELECT * FROM %s " +
      "WHERE version=:version " +
      "AND is_insert=:is_insert " +
      "AND subject=:subject" +
      "AND predicate=:predicate" +
      "AND is_out=:is_out";
  private static final String TABLE_EXISTS = "SELECT EXISTS (" +
      "SELECT FROM information_schema.tables\n" +
      "WHERE table_schema = 'public'\n" +
      "AND table_name = '%s'\n" +
      ")\n";
  private static final String CREATE_TABLE = "CREATE TABLE %s(\n" +
      "        subject VARCHAR(2048) NOT NULL,\n" +
      "        predicate VARCHAR(2048) NOT NULL,\n" +
      "        language VARCHAR(3),\n" +
      "        type VARCHAR(2048),\n" +
      "        is_out BOOLEAN NOT NULL,\n" +
      "        version INTEGER NOT NULL,\n" +
      "        is_insert BOOLEAN NOT NULL,\n" +
      "        object TEXT NOT NULL\n" +
      ")\n";
  private static final String PUT =
      "INSERT INTO %s VALUES (:subject, :predicate, :language, :type, :is_out, :version, :is_insert, :object)";
  public static final Logger LOG = LoggerFactory.getLogger(PsqlTruePatchStore.class);
  private final Handle handle;
  private final String tableName;
  private int count;

  public PsqlTruePatchStore(
      Jdbi sqlDatabase,
      BdbWrapper<String, String> oldPatchStore,
      String userId,
      String dataSetId
  ) {
    handle = sqlDatabase.open();
    tableName = DigestUtils.md5Hex(String.format("%s_%s_truepatch", userId, dataSetId));

    final boolean tableExists = handle.createQuery(String.format(TABLE_EXISTS, tableName))
                                      .mapTo(Boolean.class)
                                      .findOnly();
    if (!tableExists) {
      handle.execute(String.format(CREATE_TABLE, tableName));
      handle.execute(String.format("CREATE INDEX %s_subject ON %s (subject)", tableName, tableName));
      handle.execute(String.format("CREATE INDEX %s_version ON %s (version)", tableName, tableName));
      handle.execute(String.format("CREATE INDEX %s_version ON %s (version)", tableName, tableName));
    }

    final PreparedBatch batch = handle.prepareBatch(String.format(PUT, tableName));

    try (final Stream<Tuple<String, String>> keysAndValues = oldPatchStore.databaseGetter().getAll().getKeysAndValues(
        oldPatchStore.keyValueConverter(Tuple::tuple))) {
      keysAndValues.forEach(kv -> put(kv.getLeft(), kv.getRight(), batch));
    }
    if (batch.size() > 0) {
      batch.execute();
    }

    oldPatchStore.empty();
  }

  private void put(String key, String value, PreparedBatch batch) {
    String[] keyParts = key.split("\n", 3);
    String[] parts = value.split("\n", 5);
    final Direction direction = parts[1].charAt(0) == '1' ? OUT : IN;
    final String subject = keyParts[0];
    final int currentVersion = Integer.parseInt(keyParts[1]);
    final String predicate = parts[0];
    final boolean isAssertion = Integer.parseInt(keyParts[2]) == 1;
    final String valueType = parts[2].equals("") ? null : parts[2];
    final String language = parts[3].equals("") ? null : parts[3];
    final String object = parts[4];

    batch.bind("subject", subject)
         .bind("predicate", predicate)
         .bind("language", language)
         .bind("type", valueType)
         .bind("is_out", direction == Direction.OUT)
         .bind("version", currentVersion)
         .bind("is_insert", isAssertion)
         .bind("object", object)
         .add();
    count++;
    if (count % 5000 == 0) {
      LOG.info("execute: {}", count);
      batch.execute();
    }

    // put(subject, currentVersion, predicate, direction, isAssertion, object, valueType, language);

  }

  @Override
  public void put(String subject, int currentVersion, String predicate, Direction direction, boolean isAssertion,
                  String object, String valueType, String language) {
    handle.createUpdate(String.format(PUT, tableName))
          .bind("subject", subject)
          .bind("predicate", predicate)
          .bind("language", language)
          .bind("type", valueType)
          .bind("is_out", direction == Direction.OUT)
          .bind("version", currentVersion)
          .bind("is_insert", isAssertion)
          .bind("object", object)
          .execute();
  }

  @Override
  public Stream<CursorQuad> getChangesOfVersion(int version, boolean assertions) {
    return handle.createQuery(String.format(GET_VERSION, tableName))
                 .bind("version", version)
                 .bind("is_insert", assertions)
                 .map(mapToCursorQuad())
                 .stream();
  }

  private RowMapper<CursorQuad> mapToCursorQuad() {
    return (rs, ctx) -> CursorQuad.create(
        rs.getString("subject"),
        rs.getString("predicate"),
        rs.getBoolean("is_out") ? Direction.OUT : Direction.IN,
        rs.getBoolean("is_insert") ? ChangeType.ASSERTED : ChangeType.RETRACTED,
        rs.getString("object"),
        rs.getString("type"),
        rs.getString("language"),
        ""
    );
  }

  @Override
  public Stream<CursorQuad> getChanges(String subject, int version, boolean assertions) {
    return handle.createQuery(String.format(GET_VERSION_SUBJECT, tableName))
                 .bind("version", version)
                 .bind("is_insert", assertions)
                 .bind("subject", subject)
                 .map(mapToCursorQuad())
                 .stream();

  }

  @Override
  public Stream<CursorQuad> getChanges(String subject, String predicate, Direction direction, int version,
                                       boolean assertions) {
    return handle.createQuery(String.format(GET_VERSION_SUBJECT_PRED, tableName))
                 .bind("version", version)
                 .bind("is_insert", assertions)
                 .bind("subject", subject)
                 .bind("predicate", predicate)
                 .bind("is_out", direction == Direction.OUT)
                 .map(mapToCursorQuad())
                 .stream();

  }

  @Override
  public void close() {
    handle.close();
  }

  @Override
  public void commit() {
  }

  @Override
  public void start() {

  }

  @Override
  public boolean isClean() {
    return true;
  }

  @Override
  public void empty() {
    handle.execute(String.format("TRUNCATE TABLE %s", tableName));
  }
}
