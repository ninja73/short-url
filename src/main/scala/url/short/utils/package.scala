package url.short

import java.net.URL

import org.hashids.Hashids
import url.short.config.Config

import scala.util.Try


package object utils {

  private val hashids: Hashids = new Hashids(Config.webServer.hashidsConfig.salt, Config.webServer.hashidsConfig.size, Config.webServer.hashidsConfig.alphabet)

  def hashUrlEncoder(in: String): String = hashids.encodeHex(in)

  def hashUrlDecoder(short: String): Option[String] = {
    val result = hashids.decodeHex(short)
    Option(result)
  }

  object HttpUtils {
    def validateUrl(url: String): Try[String] = Try {
      val _ = new URL(url)
      url
    }
  }

}
