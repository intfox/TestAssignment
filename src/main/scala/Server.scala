import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import config.Config
import repository.ElementsRepository
import http.ElementsRoutes

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = (for {
    blocker <- Blocker[IO]
    config <- Resource.liftF(Config[IO]())
    elementsRepository = new ElementsRepository[IO]
    routes = new ElementsRoutes[IO](elementsRepository, blocker)
    server <- BlazeServerBuilder[IO].bindHttp(config.port, "0.0.0.0").withHttpApp(routes.routes).resource
  } yield server).use(_ => IO.never).as(ExitCode.Success)
}
