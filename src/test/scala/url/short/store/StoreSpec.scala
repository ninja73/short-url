package url.short.store

import org.hashids.Hashids
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import url.short.hash.{Hash, HashIds}
import url.short.store.store.InMemoryStore

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.jdk.CollectionConverters._

class StoreSpec extends AnyWordSpec with Matchers with ScalaFutures {
  val hashids: Hashids = new Hashids("qwerty", 10, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_")
  implicit val h: Hash[String] = new HashIds(hashids)

  val map: concurrent.Map[String, String] = new ConcurrentHashMap[String, String].asScala
  val store: InMemoryStore = InMemoryStore(map)
  val targetUrl = "http://some"
  "Store" should {
    "Get target url" in {
      whenReady(store.add(targetUrl)) {
        case Right(short) =>
          whenReady(store.get(short)) {
            case Some(url) => url should ===(targetUrl)
            case None => fail("not found target url")
          }
        case Left(e) => fail(e)
      }
    }
    "Add target url" in {
      whenReady(store.add(targetUrl)) {
        case Right(short) =>
          short should ===("aQavv4Px63")
          map.get(short) should ===(Some(targetUrl))
        case Left(e) => fail(e)
      }
    }
    "Get url not found" in {
      whenReady(store.get("test")) {
        case Some(url) => fail(s"found target url $url")
        case None => succeed
      }
    }
  }
}
