package config

import cats.effect.Sync
import cats.implicits._
import pureconfig._
import pureconfig.generic.auto._

import domain.Error

case class Server(port: Int)

object Config {
  def apply[F[_]: Sync](): F[Server] = Sync[F].fromEither(ConfigSource.default.at("server").load[Server].leftMap(Error.ErrorLoadConfig))
}