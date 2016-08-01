name := "finch-jwt"
scalaVersion := "2.11.8"
organization := "com.marekkadek"

resolvers := Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "twitter-repo" at "https://maven.twttr.com",
  "Atlassian Releases" at "https://maven.atlassian.com/public/"
)

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
)

scalafmtConfig := Some(file(".scalafmt"))

val finchVersion = "0.11.0-M2"
val jwtVersion   = "0.8.0"
libraryDependencies := Seq(
  "com.github.finagle" %% "finch-core"  % finchVersion,
  "com.pauldijou"      %% "jwt-circe"   % jwtVersion
)
