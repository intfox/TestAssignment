import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import repository.ElementsRepository
import http.ElementsRoutes

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val elementsRepository = new ElementsRepository[IO]
    val routes = new ElementsRoutes[IO](elementsRepository)
    BlazeServerBuilder[IO].bindHttp(9000, "0.0.0.0").withHttpApp(routes.routes).serve.compile.drain.as(ExitCode.Success)
  }
}
