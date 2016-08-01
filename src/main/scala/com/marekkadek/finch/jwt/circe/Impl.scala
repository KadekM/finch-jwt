package com.marekkadek.finch.jwt.circe

import com.twitter.util.Future
import io.catbird.util.Rerunnable
import io.finch.{Endpoint, Input, Output, _}
import pdi.jwt.JwtCirce
import pdi.jwt.algorithms.JwtHmacAlgorithm

import scala.util.{Failure, Success}

object JwtAuthFailed extends Exception {
  override def getMessage: String = "Invalid JWT"
}

final case class JwtAuth(key: String, algorithm: JwtHmacAlgorithm, header: String) {
  def apply[A](e: Endpoint[A]): Endpoint[A] = new Endpoint[A] {
    private[this] val unauthorized = new Rerunnable[Output[A]] {
      override def run = Future.value(Unauthorized(JwtAuthFailed))
    }

    override def apply(input: Input): Endpoint.Result[A] =
      e(input).map {
        case (input, output) =>
          input -> authenticated(input).flatMap(if (_) output else unauthorized)
      }

    private[this] def authenticated(input: Input): Rerunnable[Boolean] =
      Rerunnable.fromFuture(
          input.request.headerMap.get(header).map(validate).getOrElse(Future.False))

    private[this] def validate(headerContent: String): Future[Boolean] =
      JwtCirce.decodeJson(headerContent, key, Seq(algorithm)) match {
        case Success(x) =>
          Future.True
        case Failure(e) =>
          Future.False
      }
  }
}
