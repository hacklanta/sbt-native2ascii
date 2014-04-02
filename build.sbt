sbtPlugin := true

name := "sbt-native2ascii"

organization := "com.hacklanta"

version := "0.1"

pomExtra :=
<url>http://github.com/hacklanta/sbt-native2ascii</url>
<licenses>
  <license>
    <name>MIT</name>
    <url>http://opensource.org/licenses/MIT</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<scm>
  <url>https://github.com/hacklanta/sbt-native2ascii.git</url>
  <connection>https://github.com/hacklanta/sbt-native2ascii.git</connection>
</scm>
<developers>
  <developer>
    <id>riveramj</id>
    <name>Mike Rivera</name>
    <email>rivera.mj@gmail.com</email>
  </developer>
</developers>

scalacOptions += "-deprecation"

