package http

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.all._
import org.http4s.{HttpRoutes, QueryParamDecoder, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router
import org.http4s.implicits._

import repository.{ElementsRepository, Sort}
import domain.{Element, Error}

class ElementsRoutes[F[_]: Sync: ContextShift](elementRepo: ElementsRepository[F], blocker: Blocker) extends Http4sDsl[F] {
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
  object PageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("page_size")
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

  private val routeSwagger = HttpRoutes.of[F] {
    case request @ GET -> Root / "swagger" => StaticFile.fromResource("swagger-ui.html", blocker, Some(request)).getOrElseF(NotFound())
    case request @ GET -> Root / "swagger.yaml" => StaticFile.fromResource("swagger.yaml", blocker, Some(request)).getOrElseF(NotFound())
  }

  val routes = (Router(
    "/element" -> routesOneElement,
    "/elements" -> routesElements
  ) <+> routeSwagger).orNotFound
}
