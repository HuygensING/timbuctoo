package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.bulkupload.BulkUploadService;
import nl.knaw.huygens.timbuctoo.bulkupload.InvalidFileException;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.Loader;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.ThrowingErrorHandler;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BulkUploadedDataSourceTest {

  @Test
  public void testExpressions() throws Exception {
    TinkerPopGraphManager graph = newGraph().wrap();
    Vres vres = new VresBuilder().build();
    BulkUploadService bulkUploadService = new BulkUploadService(vres, graph, 200);
    bulkUploadService.saveToDb("myVre", new StaticLoader(), new ArrayList<>(), "myVre", s -> { });


    Map<String, String> expressions = ImmutableMap.of(
      "special", "name + (age == null ? \"\" : \" \" + age)"
    );
    BulkUploadedDataSource dataSource = new BulkUploadedDataSource("myVre", "collection", expressions, graph);
    Iterator<Row> rows = dataSource.getRows(new ThrowingErrorHandler());
    assertThat(rows.next().get("special"), is("john 12"));
    assertThat(rows.next().get("special"), is("bert"));
  }

  private class StaticLoader implements Loader {

    @Override
    public void loadData(List<Tuple<String, File>> files, Importer importer) throws InvalidFileException, IOException {
      importer.startCollection("collection");
      importer.registerPropertyName(1, "name");
      importer.registerPropertyName(2, "age");
      importer.startEntity();
      importer.setValue(1, "john");
      importer.setValue(2, "12");
      importer.finishEntity();
      importer.startEntity();
      importer.setValue(1, "bert");
      importer.finishEntity();
      importer.finishCollection();
    }
  }
}
