package nl.knaw.huygens.timbuctoo.rest.providers;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationSearchable;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Generates a Microsoft Excel representation for a reception search.
 */
@Provider
@Produces(XLSProvider.EXCEL_TYPE_STRING)
@Singleton
public class XLSProvider implements MessageBodyWriter<RelationSearchable> {

  public static final String EXCEL_TYPE_STRING = "application/vnd.ms-excel";
  public static final MediaType EXCEL_TYPE = new MediaType("application", "vnd.ms-excel");

  /**
   * Length cannot be determined in advance.
   */
  private static final long UNKNOWN_LENGTH = -1;

  private static final Logger LOG = LoggerFactory.getLogger(XLSProvider.class);

  private final Repository repository;

  @Inject
  public XLSProvider(Repository repository) {
    this.repository = repository;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return EXCEL_TYPE.equals(mediaType) && RelationSearchable.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(RelationSearchable dto, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return UNKNOWN_LENGTH;
  }

  @Override
  public void writeTo(RelationSearchable dto, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out)
    throws IOException {

    List<RelationDTO> refs = dto.getRefs();
    if (refs != null && refs.size() > 0) {
      HSSFWorkbook workbook = new HSSFWorkbook();
      HSSFSheet sheet = workbook.createSheet("Reception Data");

      RelationDTO first = refs.get(0);
      String name = first.getRelationName();
      RelationType relationType = repository.getRelationTypeByName(name, false);
      if (relationType == null) {
        LOG.error("No relation type with name {}", name);
        return;
      }
      String sourceTypeName = relationType.getSourceTypeName();
      if (!"person".equals(sourceTypeName) && !"document".equals(sourceTypeName)) {
        LOG.error("Illegal source type of relation: {}", sourceTypeName);
        LOG.error("Allowed values are: 'person' and 'document'");
        return;
      }

      // header
      int index = 0;
      HSSFRow row = sheet.createRow(index++);
      addHeaderToRow(row, first);

      // content
      for (RelationDTO ref : refs) {
        row = sheet.createRow(index++);
        addDataToRow(row, ref);
      }

      workbook.write(out);
    }
  }

  private void addHeaderToRow(HSSFRow row, RelationDTO dto) {
    int index = 0;
    for (Map.Entry<String, ? extends Object> entry : dto.getSourceData().entrySet()) {
      index = addCell(row, index, "src-" + entry.getKey());
    }
    index = addCell(row, index, "relationType");
    for (Map.Entry<String, ? extends Object> entry : dto.getTargetData().entrySet()) {
      index = addCell(row, index, "dst-" + entry.getKey());
    }
  }

  private void addDataToRow(HSSFRow row, RelationDTO dto) {
    int index = 0;
    for (Map.Entry<String, ? extends Object> entry : dto.getSourceData().entrySet()) {
      index = addCell(row, index, getValue(entry));
    }
    index = addCell(row, index, dto.getRelationName());
    for (Map.Entry<String, ? extends Object> entry : dto.getTargetData().entrySet()) {
      index = addCell(row, index, getValue(entry));
    }
  }

  private String getValue(Map.Entry<String, ? extends Object> entry) {
    return entry.getValue() == null ? "" : "" + entry.getValue();
  }

  private int addCell(HSSFRow row, int index, String value) {
    HSSFCell cell = row.createCell(index, HSSFCell.CELL_TYPE_STRING);
    cell.setCellValue(value);
    return index + 1;
  }

}
