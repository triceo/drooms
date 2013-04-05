package org.drooms.gui.swing.util

import java.io.File
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import java.io.FileOutputStream
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import java.io.FileInputStream
import java.net.URL

object IOUtils {

  /**
   * Creates new jar file in specified path and included all the files in basedir, including
   * subdirectories.
   */
  def createJarFromDir(jar: File, basedir: File): Unit = {
    // very ugly workaround for jar caching issues, the url is not important at all, but the method is not
    // static so an instance is needed 
    new URL("http://localhost/").openConnection().setDefaultUseCaches(false)
    def getEntryName(basedir: File, file: File): String = {
      val filePath = file.getAbsolutePath()
      val basedirPathLen = basedir.getAbsolutePath().length()
      // strip the basedir name from the file name
      val name = filePath.substring(basedirPathLen)
      // remove possible leading slash
      if (name.startsWith("/")) {
        name.substring(1)
      } else {
        name
      }
    }

    def addFileToArchive(archive: JarArchiveOutputStream, entry: JarArchiveEntry, file: File): Unit = {
      archive.putArchiveEntry(entry)
      // write the content of the file to the archive
      val in = new FileInputStream(file)
      // transfer bytes from the file to the JAR archive
      var buf = new Array[Byte](10000)
      var len = in.read(buf)
      while (len > 0) {
        archive.write(buf, 0, len)
        len = in.read(buf)
      }
      in.close();
      // complete the entry
      archive.closeArchiveEntry()
    }

    /**
     * @param basedir for the archive used to determine the path in the jar for the dir that will be stored
     * @param dir contents of directory that needs to be added to jar archive
     */
    def addDirToJarArchive(archive: JarArchiveOutputStream, basedir: File, dir: File): Unit = {
      if (!dir.isDirectory()) {
        throw new IllegalArgumentException("Specified instance of File is not a directory!")
      }
      for (file <- dir.listFiles()) {
        if (file.isDirectory()) {
          // directory entries has to end with "/"
          archive.putArchiveEntry(new JarArchiveEntry(getEntryName(basedir, file) + "/"))
          archive.closeArchiveEntry()
          addDirToJarArchive(archive, basedir, file)
        } else {
          // file is regular file, just add it
          addFileToArchive(archive, new JarArchiveEntry(getEntryName(basedir, file)), file)
        }
      }
    }
    val archive = new JarArchiveOutputStream(new FileOutputStream(jar))
    addDirToJarArchive(archive, basedir, basedir)
    archive.close()
  }
}