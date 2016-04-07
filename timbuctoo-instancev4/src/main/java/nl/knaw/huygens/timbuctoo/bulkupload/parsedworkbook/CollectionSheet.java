package nl.knaw.huygens.timbuctoo.bulkupload.parsedworkbook;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CollectionSheet {
  public final LinkedHashMap<String, PropertyColumns> properties = new LinkedHashMap<>();

  public void asWorkSheet(XSSFSheet sheet, CreationHelper createHelper) {
    XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);

    sheet.createRow(0); //for the names
    sheet.createRow(1); //for the subFields
    int curCol = 0;
    for (Map.Entry<String, PropertyColumns> property : this.properties.entrySet()) {
      PropertyColumns prop = property.getValue();
      int addedCols = prop.addHeader(sheet, curCol, property.getKey(), createHelper);
      int propCol = curCol;
      for (Optional<XSSFDataValidationConstraint> optionalConstraint : prop.getValidValuesPerColumn(dvHelper)) {
        if (optionalConstraint.isPresent()) {
          XSSFDataValidationConstraint dvConstraint = optionalConstraint.get();
          CellRangeAddressList addressList = new CellRangeAddressList(2, 10000, propCol, propCol);
          XSSFDataValidation validation = (XSSFDataValidation)dvHelper.createValidation(dvConstraint, addressList);
          validation.setShowErrorBox(true);
          sheet.addValidationData(validation);
        }
        propCol++;
      }
      curCol += addedCols;
    }

    //autoSizeColumn
  }


  public PropertyColumns withProperty(String key, LocalProperty property) {
    if (properties.containsKey(key)) {
      throw new IllegalArgumentException("Same key passed twice " + key);
    }

    PropertyColumns propertyColumns = new PropertyColumns(property);
    properties.put(key, propertyColumns);
    return propertyColumns;
  }

}
