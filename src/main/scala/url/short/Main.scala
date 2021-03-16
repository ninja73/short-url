package url.short

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import org.hashids.Hashids
import url.short.config.Config
import url.short.hash.{Hash, HashIds}
import url.short.route.CustomExceptionHandler._
import url.short.route.{ShortRoute, SwaggerUIRoute}
import url.short.store.store._
import url.short.swagger.SwaggerDoc

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters._

object Main extends App with LazyLogging {
  implicit val system: ActorSystem = ActorSystem.create("geo-service")
  implicit val materializer: Materializer = Materializer(system)
  implicit val dispatcher: ExecutionContext = system.dispatcher

  val map: concurrent.Map[String, String] = new ConcurrentHashMap[String, String].asScala
  val hashids: Hashids = new Hashids(Config.webServer.hashidsConfig.salt, Config.webServer.hashidsConfig.size, Config.webServer.hashidsConfig.alphabet)
  implicit val h: Hash[String] = new HashIds(hashids)


  val router = handleExceptions(exceptionHandler)(
    new SwaggerDoc(Config.webServer).routes ~
      SwaggerUIRoute.route ~
      ShortRoute(InMemoryStore(map)).route)

  val init = for {
    bind <- Http().newServerAt(Config.webServer.host, Config.webServer.port).bind(router)
  } yield {
    logger.info(s"Started host: ${Config.webServer.host}, port: ${Config.webServer.port}")
    bind
  }

  Await.ready(system.whenTerminated, Duration.Inf)

  init
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
