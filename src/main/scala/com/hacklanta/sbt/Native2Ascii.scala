package com.hacklanta { package sbt {
import _root_.sbt._
import Keys._
import java.io._

object Native2Ascii extends Plugin {

  val AsciiConvert = config("convert")
  
  val convertTranslations = TaskKey[Unit]("convert-translations")

  val sourceTranslationDirectory = SettingKey[File]("source-translation-directory")
  val destinationTranslationDirectory = SettingKey[File]("destination-translation-directory")
  val translationEncoding = SettingKey[String]("translation-encoding")

  def removeTempExtension(newFiles: List[File], tempFileExtension: String) = {
    newFiles.map { file => 
      val restoredFileName = file.toString.stripSuffix(tempFileExtension) 

      file.renameTo(new File(restoredFileName))
    }.reduceLeft( _ & _)
  }

  def cleanSourceDirectory(streams: TaskStreams, sourceDirectory: File, tempFileExtension: String) = {
    val (newFiles, originalFiles) = sourceDirectory.listFiles.partition { file =>
      file.getName.endsWith(tempFileExtension)
    } 
    
    originalFiles.foreach(_.delete())

    val renameSuccess = removeTempExtension(newFiles.toList, tempFileExtension)
    
    if (!renameSuccess)
    streams.log.error(s"Failed to rename translated files. Some files may end in '$tempFileExtension'.")      
  }

  def setupConversion(streams: TaskStreams, sourceDirectory: File, destinationDirectory: File, encoding: String) = {
    streams.log.info("Begin converting files")

    val tempFileExtension = ".conversion"

    val convertInPlace_? = (sourceDirectory == destinationDirectory)
    val sourceFiles = sourceDirectory.listFiles.toList
      
    convertInPlace_? match {
      case true => 
        sourceFiles.foreach { file => 
          doConversion(file.toString, file.toString.concat(tempFileExtension), encoding)
        }
        cleanSourceDirectory(streams, sourceDirectory, tempFileExtension)       
      case false => 
        sourceFiles.foreach { file => 
          doConversion(file.toString, destinationDirectory.toString.concat(file.getName), encoding)
        }
    }
 }

  def doConversion(sourceFile: String, destinationFile: String, encoding: String) = {
    scala.sys.process.Process(s"native2Ascii -encoding $encoding $sourceFile $destinationFile").!
  }

  val native2AsciiSettings = Seq(
    translationEncoding in AsciiConvert := "utf-8",
    sourceTranslationDirectory in AsciiConvert <<= (baseDirectory) { _ / "src/main/resources/i18n" },
    destinationTranslationDirectory in AsciiConvert <<= (baseDirectory) { _ / "src/main/resources/i18n" },
    convertTranslations in AsciiConvert <<= (streams, sourceTranslationDirectory in AsciiConvert, destinationTranslationDirectory in AsciiConvert, translationEncoding in AsciiConvert) map setupConversion _
  )

}   
}}
