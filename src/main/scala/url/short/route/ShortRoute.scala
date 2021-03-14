package url.short.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import url.short.store.store.InMemoryState

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.implicitConversions

final case class TargetURL(url: String)

class ShortRoute(storage: InMemoryState)(implicit actorSystem: ActorSystem)
  extends Directives with LazyLogging {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  val route: Route =
    get {
      println(storage.add("url", "test1"))
      println(storage.add("url", "test"))
      println(storage.lookup("url"))
      complete("result")
    } ~
      post {
        entity(as[TargetURL]) { targetURL =>
          complete("result")
        }
      }
}

object ShortRoute {
  def apply(storage: InMemoryState)(implicit system: ActorSystem): ShortRoute = new ShortRoute(storage)
}
