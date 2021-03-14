package url.short.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import url.short.store.store.Store

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.implicitConversions

final case class TargetURL(url: String)

class ShortRoute()(implicit actorSystem: ActorSystem, storage: Store[Future])
  extends Directives with LazyLogging {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  val route: Route =
    get {
      complete("result")
    } ~
      post {
        entity(as[TargetURL]) { targetURL =>
          complete("result")
        }
      }
}

object ShortRoute {
  def apply()(implicit system: ActorSystem, storage: Store[Future]): ShortRoute = new ShortRoute()
}
