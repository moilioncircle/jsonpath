name := "jsonpath"

version := "0.0.1"

scalaVersion := "2.11.6"

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

import CoverallsPlugin.CoverallsKeys._

coverallsToken := "3v3maIxCBLyDlp1EQoGjdiiXNuMckO6Bz"

encoding := "ISO-8859-1"