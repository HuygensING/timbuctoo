package nl.knaw.huygens.timbuctoo.experimental.exports;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.ExcelExportService;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExcelExportServiceTest {


  @Test
  public void exportOutputsAStreamingWorkbook() {

    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(newGraph().build());


    ExcelExportService instance = new ExcelExportService(HuygensIng.mappings, graphWrapper);

    Workbook result = instance.searchResultToExcel(Lists.<Vertex>newArrayList(), "wwperson", 1);

    assertThat(result, instanceOf(SXSSFWorkbook.class));
  }

}
