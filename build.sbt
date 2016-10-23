scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

val jwtVersion   = "0.9.0"
val finchVersion = "0.11.0-M4"

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
  libraryDependencies := Seq("com.github.finagle" %% "finch-core" % finchVersion)
)

val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val publishSettings = Seq(
  homepage := Some(url("https://github.com/KadekM/finch-jwt")),
  organizationHomepage := Some(url("https://github.com/KadekM/finch-jwt")),
  licenses += ("MIT license", url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra :=
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

lazy val jwtScala = project
  .in(file("."))
  .settings(baseSettings ++ noPublishSettings)
  .settings(
    name := "finch-jwt"
  )
  .aggregate(jwtCirce)
  .dependsOn(jwtCirce)

lazy val jwtCirce = project
  .in(file("json/circe"))
  .settings(baseSettings ++ publishSettings)
  .settings(
    name := "finch-jwt-circe",
    libraryDependencies += "com.pauldijou" %% "jwt-circe" % jwtVersion
  )
