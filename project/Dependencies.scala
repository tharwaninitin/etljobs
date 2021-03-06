import sbt._

object Dependencies {
  val SparkVersion = "2.4.4"
  val SparkBQVersion = "0.19.0"
  val GcpBqVersion = "1.80.0"
  val GcpDpVersion = "1.1.2"
  val GcpGcsVersion = "1.108.0"
  val GcpPubSubVersion = "1.108.1"
  val HadoopGCSVersion = "1.6.1-hadoop2"
  val HadoopS3Version = "2.10.0"
  val AwsS3Version = "2.13.23"

  val ZioVersion = "1.0.4"
  val ZioCatsInteropVersion = "2.1.4.0"
  val CalibanVersion = "0.9.5"

  val K8sClientVersion = "0.5.0"
  val CatsCoreVersion = "2.1.1"
  val CatsEffectVersion = "2.1.4"
  val Fs2Version = "2.4.4"
  val CirceVersion = "0.13.0"
  val CirceConfigVersion = "0.8.0"
  val DoobieVersion = "0.9.0"
  val SkunkVersion = "0.0.18"
  val Http4sVersion = "0.21.19"

  val FlywayVersion = "6.4.1"
  val ScoptVersion = "4.0.0"
  val LogbackVersion = "1.2.3"
  val PgVersion = "42.2.19"
  val RedisVersion = "3.30"
  val ScalajHttpVersion = "2.4.2"
  val mailVersion = "1.6.2"

  val ScalaTestVersion = "3.0.5"
  val TestContainerVersion = "1.11.2"

  lazy val googleCloudLibs = List(
    "com.google.cloud" % "google-cloud-bigquery" % GcpBqVersion,
    "com.google.cloud" % "google-cloud-dataproc" % GcpDpVersion,
    "com.google.cloud" % "google-cloud-storage" % GcpGcsVersion,
    "com.google.cloud" % "google-cloud-pubsub" % GcpPubSubVersion,
  )

  lazy val catsLibs = List(
    "org.typelevel" %% "cats-core" % CatsCoreVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
  )

  lazy val streamingLibs = List(
    "co.fs2" %% "fs2-core" % Fs2Version,
    "co.fs2" %% "fs2-io" % Fs2Version,
    "org.tpolecat" %% "skunk-core" % SkunkVersion,
    "com.permutive" %% "fs2-google-pubsub-grpc" % "0.16.0",
    "com.github.fs2-blobstore" %% "gcs" % "0.7.3",
    "com.github.fs2-blobstore" %% "s3" % "0.7.3",
  )

  lazy val jsonLibs = List(
    "io.circe" %% "circe-core" % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser" % CirceVersion,
    "io.circe" %% "circe-optics" % CirceVersion,
    "io.circe" %% "circe-config" % CirceConfigVersion,
    "org.json4s" %% "json4s-jackson" % "3.5.3",
  )

  lazy val awsLibs = List(
    "software.amazon.awssdk" % "s3" % AwsS3Version
  )

  lazy val sparkLibs = List(
    "org.apache.spark" %% "spark-sql" % SparkVersion
  )

  lazy val redis = List(
    "net.debasishg" %% "redisclient" % RedisVersion
  )

  lazy val scalajHttp = List(
    "org.scalaj" %% "scalaj-http" % ScalajHttpVersion
  )

  lazy val mail = List(
    "javax.mail" % "javax.mail-api" % mailVersion,
    "com.sun.mail" % "javax.mail"   % mailVersion
  )

  lazy val kubernetes = List(
    "com.goyeau" %% "kubernetes-client" % K8sClientVersion
  )

  lazy val dbLibs = List(
    "org.tpolecat" %% "doobie-core"     % DoobieVersion,
    "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
    "org.tpolecat" %% "doobie-hikari"   % DoobieVersion,
    "org.tpolecat" %% "doobie-quill"    % DoobieVersion,
    "org.flywaydb" % "flyway-core"      % FlywayVersion
  )

  lazy val zioLibs = List(
    "dev.zio" %% "zio" % ZioVersion,
    "dev.zio" %% "zio-interop-cats" % ZioCatsInteropVersion
  )

  lazy val miscLibs = List(
    "com.github.scopt" %% "scopt" % ScoptVersion,
    "org.slf4j" % "slf4j-api" % "1.7.30",
  )

  lazy val caliban = List(
    "com.github.ghostdogpr" %% "caliban" % CalibanVersion,
    "com.github.ghostdogpr" %% "caliban-http4s" % CalibanVersion,
    "org.http4s" %% "http4s-prometheus-metrics" % Http4sVersion,
    "eu.timepit" %% "fs2-cron-core" % "0.2.2",
    "com.github.cb372" %% "scalacache-caffeine" % "0.28.0",
    "com.github.cb372" %% "scalacache-cats-effect" % "0.28.0"
  )

  lazy val http4sclient = List(
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion
  )

  lazy val jwt = List(
    "com.pauldijou" %% "jwt-core" % "4.2.0"
  )

  lazy val coreTestLibs = List(
    "org.scalatest" %% "scalatest" % ScalaTestVersion,
    "org.testcontainers" % "postgresql" % TestContainerVersion,
    "dev.zio" %% "zio-test"     % ZioVersion,
    "dev.zio" %% "zio-test-sbt" % ZioVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "org.postgresql" % "postgresql" % PgVersion
  ).map(_ % Test)

  lazy val cloudTestLibs = List(
    "com.google.cloud.bigdataoss" % "gcs-connector" % HadoopGCSVersion,
    "org.apache.hadoop" % "hadoop-aws" % HadoopS3Version,
    "org.apache.hadoop" % "hadoop-common" % HadoopS3Version,
  ).map(_ % Test)

  lazy val sparkTestLibs = List(
    "com.google.cloud.spark" %% "spark-bigquery-with-dependencies" % SparkBQVersion,
  ).map(_ % Test)
}
