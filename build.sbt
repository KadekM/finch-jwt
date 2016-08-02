scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

val jwtVersion   = "0.8.0"
val finchVersion = "0.11.0-M2"

val baseSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.marekkadek",

  resolvers := Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "twitter-repo" at "https://maven.twttr.com",
    "Atlassian Releases" at "https://maven.atlassian.com/public/"
  ),

  scalacOptions := Seq(
    "-encoding",
    "UTF-8",
    "-Xlint",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Yno-adapted-args",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import",
    "-Ywarn-unused",
    "-Ywarn-numeric-widen"
  ),

  libraryDependencies := Seq("com.github.finagle" %% "finch-core"  % finchVersion)
)

val publishSettings = Seq(
  homepage := Some(url("https://github.com/KadekM/finch-jwt")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <licenses>
      <license>
        <name>MIT license</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:kadekm/finch-jwt.git</url>
      <connection>scm:git:git@github.com:kadekm/finch-jwt.git</connection>
    </scm>
      <developers>
        <developer>
          <id>kadekm</id>
          <name>Marek Kadek</name>
          <url>https://github.com/KadekM</url>
        </developer>
      </developers>)
)

lazy val jwtScala = project.in(file("."))
  .settings(baseSettings)
  .settings(
    name := "finch-jwt"
  )
  .aggregate(jwtCirce)
  .dependsOn(jwtCirce)


lazy val jwtCirce = project.in(file("json/circe"))
  .settings(baseSettings)
  .settings(
    name := "finch-jwt-circe",
    libraryDependencies += "com.pauldijou"  %% "jwt-circe"  % jwtVersion
  )
