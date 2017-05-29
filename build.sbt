
name := "aviatorsbot-runway-import"

version := "1.0"

scalaVersion := "2.12.2"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("public")

lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

// https://mvnrepository.com/artifact/org.reactivemongo/reactivemongo_2.12
libraryDependencies += "org.reactivemongo" % "reactivemongo_2.12" % "0.12.3"// https://mvnrepository.com/artifact/org.scala-lang/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"


// Magnetic Deviation
libraryDependencies += "org.orekit" % "orekit" % "8.0"

enablePlugins(JavaAppPackaging)
enablePlugins(DebianPlugin)

mainClass in Compile := Some("com.vektorraum.aviatorsbot.runway.RunwayImporter")

maintainer in Linux := "Fabian Valka <fvalka@vektorraum.com>"
packageSummary in Linux := "AviatorsBot runway import"
packageDescription := "Imports runway information from the OpenAIP"

mappings in Universal <+= (packageBin in Compile, baseDirectory ) map { (_, base) =>
  val conf = base / "data" / "WMM.COF"
  conf -> "data/WMM.COF"
}

javaOptions in Universal ++= Seq(
  // -J params will be added as jvm parameters
  "-J-Xmx1024m"
)
