package http

import cats.effect.Sync
import cats.syntax.all._
import org.http4s.{HttpRoutes, QueryParamDecoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router
import org.http4s.implicits._
import io.circe.syntax._
import repository.{ElementsRepository, Sort}
import domain.{Element, Error}

class ElementsRoutes[F[_]: Sync](elementRepo: ElementsRepository[F]) extends Http4sDsl[F] {
  private val routesOneElement = HttpRoutes.of[F] {
    case GET -> Root / name => elementRepo.get(name).flatMap(elem => Ok(elem)).handleErrorWith{
      case _: Error.ElementWithThatNameNotExist => NotFound()
    }
    case DELETE -> Root / name => elementRepo.delete(name).flatMap(elem => Ok(elem)).handleErrorWith{
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

  object PageNumberMatcher extends QueryParamDecoderMatcher[Int]("page")
  object PageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
  object SortOrderMatcher extends OptionalQueryParamDecoderMatcher[Sort.Order]("order_by")
  implicit val sortOrderDecoder: QueryParamDecoder[Sort.Order] = QueryParamDecoder[String].map{
    case "asc" => Sort.ASC
    case "desc" => Sort.DESC
  }
  object SortColumnMatcher extends OptionalQueryParamDecoderMatcher[Sort.Column]("sort_by")
  implicit val sortColumnDecoder: QueryParamDecoder[Sort.Column] = QueryParamDecoder[String].map{
    case "name" => Sort.NAME
    case "number" => Sort.NUMBER
  }

  private val routesElements = HttpRoutes.of[F] {
    case GET -> Root :? PageNumberMatcher(pageNumber) +& PageSizeMatcher(optionPageSize) +& SortColumnMatcher(optionColumn) +& SortOrderMatcher(optionOrder) =>
      (for{
        sortBy <- optionColumn.map( column => Sort(column, optionOrder.getOrElse(Sort.ASC)) ).pure[F]
        listElements <- elementRepo.list(optionPageSize.getOrElse(10), pageNumber, sortBy)
        res <- Ok(listElements)
      } yield res).handleErrorWith{
        case _: Error.OutOfPage => NotFound()
      }
  }

  val routes = Router(
    "/element" -> routesOneElement,
    "/elements" -> routesElements
  ).orNotFound
}
