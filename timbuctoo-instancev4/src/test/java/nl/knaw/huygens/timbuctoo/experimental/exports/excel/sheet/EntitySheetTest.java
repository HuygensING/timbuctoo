package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EntitySheetTest {




  private final Vres mappings = HuygensIng.mappings;
  private final Collection collection = mappings.getCollection("wwpersons").get();
  private final String[] relationTypes = null;
  private final String acceptedProp = "wwrelation_accepted";

  @Test
  public void renderToWorkbookRendersAnEmptyExcelSheetToWorkBookWhenPresentedWithAnEmptySetOfVertices() {
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    final Graph graph = newGraph().build();

    given(graphWrapper.getGraph()).willReturn(graph);

    EntitySheet instance = new EntitySheet(collection, workbook, graphWrapper, mappings, relationTypes, acceptedProp);

    instance.renderToWorkbook(graph.traversal().V().toSet());

    SXSSFSheet sheet = workbook.getSheet("wwpersons");
    assertThat(sheet, instanceOf(SXSSFSheet.class));
    assertThat(sheet.getRow(0).getCell(0).getStringCellValue(), equalTo("tim_id"));
    assertThat(sheet.getRow(1).getCell(0).getStringCellValue(), equalTo("uuid"));
    assertThat(sheet.getRow(2).getCell(0), equalTo(null));

    assertThat(sheet.getRow(0).getCell(1), equalTo(null));
  }


  @Test
  public void renderToWorkbookRendersTheVerticesAsExcelCellsToTheSheet() throws IOException {
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    final Graph graph = newGraph()
      .withVertex("v1", v -> {
        v.withProperty("tim_id", "123");
        v.withProperty("types", jsnA(jsn("person"), jsn("wwperson")).toString());
        v.withProperty("wwperson_tempName", "temp name");
        v.withProperty("wwperson_names", jsnO("list", jsnA(
          jsnO("components", jsnA(
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 1")),
            jsnO("type", jsn("SURNAME"), "value", jsn("surName 1"))
          )),
          jsnO("components", jsnA(
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 2")),
            jsnO("type", jsn("SURNAME"), "value", jsn("surName 2"))
          ))
        )).toString());
      })
      .withVertex("v2", v -> {
        v.withProperty("tim_id", "234");
        v.withProperty("types", jsnA(jsn("person"), jsn("wwperson")).toString());
        v.withProperty("wwperson_tempName", "tmp 2");
        v.withOutgoingRelation("isRelatedTo", "v1", r -> {
          r.withIsLatest(true);
          r.withAccepted("wwrelation", true);
          return r;
        });
      })
      .build();
    given(graphWrapper.getGraph()).willReturn(graph);
    EntitySheet instance = new EntitySheet(collection, workbook, graphWrapper, mappings, relationTypes, acceptedProp);

    instance.renderToWorkbook(graph.traversal().V().toSet());
    SXSSFSheet sheet = workbook.getSheet("wwpersons");

    List<String> firstHeaderValues = new ArrayList<>();
    Map<String, Integer> columnIndices = new HashMap<>();
    for (int i = 0; i < 7; i++) {
      SXSSFCell cell = sheet.getRow(0).getCell(i);
      if (cell != null) {
        firstHeaderValues.add(cell.getStringCellValue());
        columnIndices.put(cell.getStringCellValue(), i);
      } else {
        firstHeaderValues.add("");
      }
    }
    assertThat(firstHeaderValues, containsInAnyOrder(Lists.newArrayList(
      equalTo(""), equalTo(""), equalTo(""),
      equalTo("tim_id"), equalTo("tempName"), equalTo("names"), equalTo("isRelatedTo") )));

    for (Map.Entry<String, Integer> entry : columnIndices.entrySet()) {
      switch (entry.getKey()) {
        case "tim_id":
          checkTimIdCells(entry.getValue(), sheet);
          break;
        case "tempName":
          checkTempNameCells(entry.getValue(), sheet);
          break;
        case "names":
          checkNamesCells(entry.getValue(), sheet);
          break;
        case "isRelatedTo":
          checkRelationCells(entry.getValue(), sheet);
          break;
        default:
          break;
      }
    }

  }

  private void checkRelationCells(Integer value, SXSSFSheet sheet) {
    assertThat(sheet.getRow(1).getCell(value).getStringCellValue(), equalTo("relation"));
    assertThat(sheet.getRow(2).getCell(value).getStringCellValue(), equalTo("wwpersons"));
    assertThat(sheet.getRow(5).getCell(value).getStringCellValue(), equalTo("123"));
  }

  private void checkNamesCells(Integer value, SXSSFSheet sheet) {
    assertThat(sheet.getRow(1).getCell(value).getStringCellValue(), equalTo("names"));
    assertThat(sheet.getRow(2).getCell(value).getStringCellValue(), equalTo("1"));
    assertThat(sheet.getRow(2).getCell(value + 2).getStringCellValue(), equalTo("2"));

    assertThat(sheet.getRow(3).getCell(value).getStringCellValue(), equalTo("forename"));
    assertThat(sheet.getRow(3).getCell(value + 1).getStringCellValue(), equalTo("foreName 1"));
    assertThat(sheet.getRow(4).getCell(value).getStringCellValue(), equalTo("surname"));
    assertThat(sheet.getRow(4).getCell(value + 1).getStringCellValue(), equalTo("surName 1"));

    assertThat(sheet.getRow(3).getCell(value + 2).getStringCellValue(), equalTo("forename"));
    assertThat(sheet.getRow(3).getCell(value + 3).getStringCellValue(), equalTo("foreName 2"));

    assertThat(sheet.getRow(4).getCell(value + 2).getStringCellValue(), equalTo("surname"));
    assertThat(sheet.getRow(4).getCell(value + 3).getStringCellValue(), equalTo("surName 2"));
  }

  private void checkTempNameCells(Integer value, SXSSFSheet sheet) {
    assertThat(sheet.getRow(1).getCell(value).getStringCellValue(), equalTo("text"));
    assertThat(sheet.getRow(3).getCell(value).getStringCellValue(), equalTo("temp name"));
    assertThat(sheet.getRow(5).getCell(value).getStringCellValue(), equalTo("tmp 2"));

  }

  private void checkTimIdCells(Integer value, SXSSFSheet sheet) {
    assertThat(sheet.getRow(1).getCell(value).getStringCellValue(), equalTo("uuid"));
    assertThat(sheet.getRow(3).getCell(value).getStringCellValue(), equalTo("123"));
    assertThat(sheet.getRow(5).getCell(value).getStringCellValue(), equalTo("234"));

  }


}
