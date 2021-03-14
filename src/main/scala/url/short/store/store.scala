package url.short.store

import scala.collection.concurrent.Map

object store {

  type Short = String
  type TargetURL = String

  trait State

  final case class InMemoryState(var data: Map[Short, TargetURL]) extends State

  trait Store[F[_], S <: State] {
    def lookup(state: S, short: Short): F[String]

    def add(state: S, short: Short, targetUrl: TargetURL): F[_]
  }

  object Store {
    def apply[F[_], S <: State](implicit DS: Store[F, S]): Store[F, S] = DS
  }

  implicit class StoreOps[F[_], S <: State](s: S) {
    def lookup(short: Short)(implicit DS: Store[F, S]): F[_] = DS.lookup(s, short)

    def add(short: Short, targetUrl: TargetURL)(implicit DS: Store[F, S]): F[_] = DS.add(s, short, targetUrl)
  }

  implicit object ShortSourceOption extends Store[Option, InMemoryState] {
    override def lookup(state: InMemoryState, short: Short): Option[TargetURL] = {
      state.data.get(short)
    }

    override def add(state: InMemoryState, short: Short, targetUrl: TargetURL): Option[TargetURL] = {
      state.data.putIfAbsent(short, targetUrl)
    }
  }

}

