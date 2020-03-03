package http

import cats.effect.Sync
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router
import org.http4s.implicits._
import io.circe.syntax._

import repository.ElementsRepository
import domain.{Element, Error}

class ElementsRoutes[F[_]: Sync](elementRepo: ElementsRepository[F]) extends Http4sDsl[F] {
  private val routesOneElement = HttpRoutes.of[F] {
    case GET -> Root / name => elementRepo.get(name).flatMap(elem => Ok(elem.asJson)).handleErrorWith{
      case _: Error.ElementWithThatNameNotExist => NotFound()
    }
    case DELETE -> Root / name => elementRepo.delete(name).flatMap(elem => Ok(elem.asJson)).handleErrorWith{
      case _: Error.ElementWithThatNameNotExist => NotFound()
    }
    case req @ POST -> Root => (for {
      element <- req.as[Element]
      _ <- elementRepo.create(element)
      ok <- Ok()
    } yield ok).handleErrorWith{
      case _: Error.ElementWithThatNameExist => Conflict()
    }
  }

  val routes = Router(
    "/element" -> routesOneElement
  ).orNotFound
}
