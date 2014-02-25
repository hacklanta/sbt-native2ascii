package com.hacklanta { package sbt {
  import _root_.sbt._
  import Keys._
  import java.io._
  import org.apache.tools.ant.taskdefs.optional.{Native2Ascii => ApacheNative2Ascii}
  import org.apache.tools.ant.util._
  import org.apache.tools.ant.Project

  object Native2Ascii extends Plugin {

    val AsciiConvert = config("convert")
    
    val convertTranslations = TaskKey[Unit]("convert-translations")

    val sourceTranslationDirectory = SettingKey[File]("source-translation-directory")
    val destinationTranslationDirectory = SettingKey[File]("destination-translation-directory")
    val translationEncoding = SettingKey[String]("translation-encoding")

    def renameNewFiles(newFiles: List[File], tempFileExtension: String, originalFileExtension: String) = {
      newFiles.map { file => 
        val restoredFileName = file.getName.stripSuffix(tempFileExtension) + "." + originalFileExtension
        val restoredFilePath = file.getParent

        file.renameTo(new File(restoredFilePath + "/" + restoredFileName))
      }.reduceLeft( _ & _)
    }

    def findOriginalFileExtension(sourceDirectory: File) = {
      val fileName = sourceDirectory.listFiles.headOption

      fileName.map { file => 
        val fileName = file.getName
        fileName.substring(fileName.lastIndexOf('.') +1)
      }.getOrElse("")
    }

    def removeDuplicateFiles(streams: TaskStreams, sourceDirectory: File, tempFileExtension: String, originalFileExtension: String) = {
      val (newFiles, originalFiles) = sourceDirectory.listFiles.partition { file =>
        file.getName.endsWith(tempFileExtension)
      }        

      originalFiles.foreach(_.delete())

      val renameSuccess = renameNewFiles(newFiles.toList, tempFileExtension, originalFileExtension)

      if (!renameSuccess)
      streams.log.error(s"Failed to rename translated files. Some files may end in '$tempFileExtension'.")      
    }

    def doConversion(streams: TaskStreams, sourceDirectory: File, destinationDirectory: File, encoding: String) = {
      streams.log.info("Begin converting files")
      
      val project = new Project()
      val native2ascii = new ApacheNative2Ascii()
      
      native2ascii.setProject(project)
      native2ascii.setEncoding(encoding)

      val tempFileExtension = ".conversion"

      val convertInPlace_? = (sourceDirectory == destinationDirectory)
      
      val originalFileExtension = findOriginalFileExtension(sourceDirectory) 
        
      if (convertInPlace_?) {
        native2ascii.setExt(tempFileExtension)
      }

      native2ascii.setSrc(sourceDirectory)

      val sourceFiles = sourceDirectory.listFiles.toList

      if (sourceFiles.isEmpty)
        streams.log.info("Did not find any files to convert.")  

      native2ascii.setDest(destinationDirectory)
      native2ascii.execute()

      if (convertInPlace_?) {
       removeDuplicateFiles(streams, sourceDirectory, tempFileExtension, originalFileExtension)       
     }
   }

    val native2AsciiSettings = Seq(
      translationEncoding := "utf-8",
      sourceTranslationDirectory in AsciiConvert <<= (baseDirectory) { _ / "src/main/resources/i18n" },
      destinationTranslationDirectory in AsciiConvert <<= (baseDirectory) { _ / "src/main/resources/i18n" },
      convertTranslations in AsciiConvert <<= (streams, sourceTranslationDirectory in AsciiConvert, destinationTranslationDirectory in AsciiConvert, translationEncoding in AsciiConvert) map doConversion _
    )

  }   
}}
