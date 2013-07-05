package nl.knaw.huygens.repository.model.atlg;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Archiver;

@DocumentTypeName("atlgarchiver")
public class ATLGArchiver extends Archiver {

  /** Migration: Name of source file */
  public String origFilename;

}
