package url.short

import java.net.URL
import scala.util.Try

package object utils {

  object HttpUtils {
    def validateUrl(url: String): Try[String] = Try {
      val _ = new URL(url)
      url
    }
  }

}
