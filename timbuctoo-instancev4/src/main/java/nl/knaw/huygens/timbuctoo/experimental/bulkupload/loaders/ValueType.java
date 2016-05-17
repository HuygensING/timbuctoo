package nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders;

//Enum because we can't let the typechecker verify correctness
public enum ValueType {
  PROPERTYNAME,
  VALUE,
  //PROPERTYTYPE,
  //INPUTFIELDNAME,
  IDENTITY_PROPERTYNAME,
  RELATIONSPECIFICATION,
  NONE
}
