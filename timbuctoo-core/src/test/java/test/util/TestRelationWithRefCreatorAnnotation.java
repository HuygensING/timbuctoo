package test.util;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.util.RefCreatorAnnotation;

@RefCreatorAnnotation(CustomRelationRefCreator.class)
public class TestRelationWithRefCreatorAnnotation extends Relation {

}
