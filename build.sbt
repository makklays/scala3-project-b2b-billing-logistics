ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "scala3-project-b2b-billing-logistics",
    idePackagePrefix := Some("com.techmatrix18")
  )
