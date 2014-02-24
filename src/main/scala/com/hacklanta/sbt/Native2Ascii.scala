package com.hacklanta { package sbt {
  import _root_.sbt._
  import Keys._
  import java.io._
  import org.apache.tools.ant.taskdefs.optional.{Native2Ascii => ApacheNative2Ascii, _}
  import org.apache.tools.ant.Project

  object Native2Ascii extends Plugin {
    
    val convertTranslations = TaskKey[Unit]("convert-translations")

    val sourceTranslationDirectory = SettingKey[File]("source-translation-directory")
    val destinationTranslationDirectory = SettingKey[File]("destination-translation-directory")

    def doConversion(sourceDirectory: File, destinationDirectory: File) = {
      val project = new Project()
      val native2ascii = new ApacheNative2Ascii()

      native2ascii.setProject(project)
      native2ascii.setEncoding("utf-8")
      native2ascii.setSrc(sourceDirectory)
      native2ascii.setDest(destinationDirectory)

      native2ascii.execute()
    }

    val native2AsciiSettings = Seq(
      convertTranslations <<= (sourceTranslationDirectory, destinationTranslationDirectory) map doConversion _
    )

  }   
}}
