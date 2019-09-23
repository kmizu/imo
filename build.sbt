organization := "com.github.kmizu"

name := "imo"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.10"

assemblyJarName in assembly := "imo.jar"

mainClass in assembly := Some("com.github.kmizu.imo.Main")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

pomExtra := (
  <url>https://github.com/kmizu/imo</url>
  <licenses>
    <license>
    <name>Apache</name>
    <url>http://www.opensource.org/licenses/Apache-2.0</url>
    <distribution>repo</distribution>
  </license>
  </licenses>
  <scm>
    <url>git@github.com:kmizu/imo.git</url>
    <connection>scm:git:git@github.com:kmizu/imo.git</connection>
  </scm>
  <developers>
    <developer>
      <id>kmizu</id>
      <name>Kota Mizushima</name>
      <url>https://github.com/kmizu</url>
    </developer>
  </developers>
)

publishTo := {
  val v = version.value
  val nexus = "https://oss.sonatype.org/"
  if (v.endsWith("-SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials ++= {
  val sonatype = ("Sonatype Nexus Repository Manager", "oss.sonatype.org")
  def loadMavenCredentials(file: java.io.File) : Seq[Credentials] = {
    xml.XML.loadFile(file) \ "servers" \ "server" map (s => {
        val host = (s \ "id").text
        val realm = if (host == sonatype._2) sonatype._1 else "Unknown"
        Credentials(realm, host, (s \ "username").text, (s \ "password").text)
     })
  }
  val ivyCredentials   = Path.userHome / ".ivy2" / ".credentials"
  val mavenCredentials = Path.userHome / ".m2"   / "settings.xml"
  (ivyCredentials.asFile, mavenCredentials.asFile) match {
    case (ivy, _) if ivy.canRead => Credentials(ivy) :: Nil
    case (_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
    case _ => Nil
  }
}

publishMavenStyle := true

scalacOptions ++= Seq("-deprecation","-unchecked")

initialCommands in console += {
  Iterator().map("import "+).mkString("\n")
}
