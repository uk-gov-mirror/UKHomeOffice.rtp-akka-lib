import sbt._
import Keys._

val moduleName = "rtp-akka-lib"

val root = Project(id = moduleName, base = file("."))
  .enablePlugins(GitVersioning)
  .settings(
    name := moduleName,
    organization := "uk.gov.homeoffice",
    scalaVersion := "3.3.5",
    crossScalaVersions := Seq("2.13.16"),
    scalacOptions ++= Seq(
      "-feature",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:reflectiveCalls",
      "-language:postfixOps",
      "-Yrangepos"
    ),
    resolvers ++= Seq(
      "ACPArtifactory Lib Snapshot" at "https://artifactory.digital.homeoffice.gov.uk/artifactory/libs-snapshot-local/",
      "ACPArtifactory Lib Release" at "https://artifactory.digital.homeoffice.gov.uk/artifactory/libs-release-local/",
      "ACPArtifactory Ext Release" at "https://artifactory.digital.homeoffice.gov.uk/artifactory/ext-release-local/",
    ),
    javaOptions in Test += "-Dconfig.resource=application.test.conf",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor" % "1.1.5" withSources(),
      "org.apache.pekko" %% "pekko-stream" % "1.1.5" withSources(),
      "org.apache.pekko" %% "pekko-testkit" % "1.1.5",
      "org.apache.pekko" %% "pekko-http" % "1.2.0",
      "org.apache.pekko" %% "pekko-http-testkit" % "1.2.0",
      "uk.gov.homeoffice" %% "rtp-io-lib" % "2.2.28-ge2e7d1e" withSources(),
      "uk.gov.homeoffice" %% "rtp-io-lib" % "2.2.28-ge2e7d1e" % Test classifier "tests" withSources(),
      "uk.gov.homeoffice" %% "rtp-test-lib" % "1.6.37-g813af7a" withSources(),
      "uk.gov.homeoffice" %% "rtp-test-lib" % "1.6.37-g813af7a" % Test classifier "tests" withSources()
  ))

publishTo := {
  val artifactory = sys.env.get("ARTIFACTORY_SERVER").getOrElse("https://artifactory.registered-traveller.homeoffice.gov.uk/")
  Some("release"  at artifactory + "artifactory/libs-release-local")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishArtifact in (Test, packageBin) := false
publishArtifact in (Test, packageDoc) := false
publishArtifact in (Test, packageSrc) := false

test in publishArtifact := false
test in assembly := {}

git.useGitDescribe := true
git.gitDescribePatterns := Seq("v*.*")
git.gitTagToVersionNumber := { tag :String =>

val branchTag = if (git.gitCurrentBranch.value == "master") "" else "-" + git.gitCurrentBranch.value
val uncommit = if (git.gitUncommittedChanges.value) "-U" else ""

tag match {
  case v if v.matches("v\\d+.\\d+") => Some(s"$v.0${uncommit}".drop(1))
  case v if v.matches("v\\d+.\\d+-.*") => Some(s"${v.replaceFirst("-",".")}${branchTag}${uncommit}".drop(1))
  case _ => None
}}
