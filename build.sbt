name := "jsonpath"

organization := "com.moilioncircle"

version := "0.4.2"

scalaVersion := "2.11.7"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.5"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

import org.scoverage.coveralls.Imports.CoverallsKeys._

coverallsToken := Some("3v3maIxCBLyDlp1EQoGjdiiXNuMckO6Bz")

coverallsEncoding := "UTF-8"

