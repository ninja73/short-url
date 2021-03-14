package url

package object short {

  sealed trait AppError extends Throwable {
    def message: String
  }

  case class ConflictUrl[T](url: T) extends AppError {
    def message: String = s"Conflict url $url"
  }

  case class NotFound[T](short: T) extends AppError {
    def message: String = s"Short $short not found"
  }
}
