package nl.knaw.huygens.timbuctoo.experimental.exports;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class ExcelExportServiceTest {


  @Test
  public void exportOutputsAStreamingWorkbook() {
    ExcelExportService instance = new ExcelExportService(HuygensIng.mappings);

    Workbook result = instance.export(Lists.<Vertex>newArrayList().iterator(), 0, Optional.empty());

    assertThat(result, instanceOf(SXSSFWorkbook.class));
  }

}
