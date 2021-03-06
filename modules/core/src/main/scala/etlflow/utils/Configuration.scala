package etlflow.utils

import cats.data._
import cats.implicits._
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._

trait Configuration {
  class Error(msg: String) extends RuntimeException(msg)

  private val configEN: EitherNec[Throwable, Config] = Either
    .catchNonFatal(ConfigFactory.load())
    .toEitherNec
    .flatMap(c => io.circe.config.parser
      .decodeAccumulating[Config](c)
      .toEither
      .leftMap(NonEmptyChain.fromNonEmptyList)
    )

  final lazy val config: Config = configEN match {
    case Left(errors) =>
      throw new Error(errors.map(x => x.getMessage).toList.mkString("\n","\n","\n"))
    case Right(value) => value
  }
}
