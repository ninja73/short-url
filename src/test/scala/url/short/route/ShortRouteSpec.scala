package url.short.route

import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.circe.Json
import org.hashids.Hashids
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import url.short.hash.{Hash, HashIds}
import url.short.store.store.InMemoryStore

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.jdk.CollectionConverters._
import scala.util.hashing.MurmurHash3


class ShortRouteSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  val hashids: Hashids = new Hashids("qwerty", 10, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_")
  implicit val h: Hash[String] = new HashIds(hashids)

  val map: concurrent.Map[String, String] = new ConcurrentHashMap[String, String].asScala
  val store: InMemoryStore = InMemoryStore(map)
  val targetUrl = "https://www.google.com/"

  val shortUrl: String = h.hashUrlEncoder(MurmurHash3.stringHash(targetUrl).toHexString)
  map.putIfAbsent(shortUrl, targetUrl)

  val conflictTargetUrl = "https://ya.ru/"
  map.putIfAbsent(h.hashUrlEncoder(MurmurHash3.stringHash(conflictTargetUrl).toHexString), "https://yandex.ru/search")

  val shortRoute: ShortRoute = ShortRoute(store)

  "Short routes" should {
    "Create short url" in {
      val targetURlJson: String = Json.fromString(s"""{"url": "$targetUrl"}""").asString.getOrElse("{}")
      val request: HttpRequest = Post("/").withEntity(ContentTypes.`application/json`, targetURlJson)

      request ~> shortRoute.create ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(s""""$shortUrl"""")
      }
    }
    "Conflict short hash" in {
      val targetURlJson: String = Json.fromString(s"""{"url": "$conflictTargetUrl"}""").asString.getOrElse("{}")
      val request: HttpRequest = Post("/").withEntity(ContentTypes.`application/json`, targetURlJson)

      request ~> shortRoute.create ~> check {
        status should ===(StatusCodes.Conflict)
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===(""""Conflict url https://yandex.ru/search"""")
      }
    }
    "Redirect" in {
      Get(s"/$shortUrl") ~> shortRoute.lookup ~> check {
        status should ===(StatusCodes.PermanentRedirect)
        contentType should ===(ContentTypes.`text/html(UTF-8)`)
      }
    }
    "Not found" in {
      Get(s"/test") ~> shortRoute.lookup ~> check {
        status should ===(StatusCodes.NotFound)
        contentType should ===(ContentTypes.`application/json`)
      }
    }
  }
}
