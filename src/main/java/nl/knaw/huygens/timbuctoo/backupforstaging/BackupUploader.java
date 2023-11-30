package nl.knaw.huygens.timbuctoo.backupforstaging;

import nl.knaw.huygens.timbuctoo.backupforstaging.exceptions.BackupUploadException;

import java.io.File;

public interface BackupUploader {
  void storeBackup(File zipfile) throws BackupUploadException;
}
