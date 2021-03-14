package url.short.store

import scala.collection.concurrent.Map
import scala.concurrent.{ExecutionContext, Future}

object store {

  type Short = String
  type TargetURL = String

  final case class InMemoryStore(var data: Map[Short, TargetURL])

  trait Store[F[_]] {
    def lookup(short: Short): F[_]

    def add(short: Short, targetUrl: TargetURL): F[_]
  }

  object Store {
    def apply[F[_], S](implicit DS: Store[F]): Store[F] = DS
  }


  implicit class StoreFutureOps(state: InMemoryStore)(implicit ec: ExecutionContext) extends Store[Future] {
    override def lookup(short: Short): Future[Option[TargetURL]] = {
      Future(state.data.get(short))
    }

    override def add(short: Short, targetUrl: TargetURL): Future[Option[TargetURL]] = {
      Future(state.data.putIfAbsent(short, targetUrl))
    }
  }

}

