package com.kadekmarek.finch.jwt.circe

import cats.data.Xor
import com.twitter.util.Future
import io.catbird.util.Rerunnable
import io.circe.{Decoder, Json}
import io.finch.{Endpoint, Input, Output, _}
import pdi.jwt.{JwtCirce, JwtClaim}
import pdi.jwt.algorithms.JwtHmacAlgorithm
import io.circe.parser._

import scala.util.{Failure, Success}

object JwtAuthFailed extends Exception {
  override def getMessage: String = "Invalid JWT"
}

final case class JwtAuth(key: String, algorithm: JwtHmacAlgorithm, authHeader: String) {

  // todo: does decode check token expiration?

  def auth: Endpoint[JwtClaim] =
    header(authHeader).map(x => JwtCirce.decode(x, key, Seq(algorithm))).mapOutput {
      case Success(x) =>
        Ok(x)
      case Failure(_) =>
        Unauthorized(JwtAuthFailed)
    }

  def authAs[A: Decoder]: Endpoint[A] = auth.mapOutput { claim =>
    decode[A](claim.content) match {
      case Xor.Right(x) => Ok(x)
      case Xor.Left(e)  => BadRequest(e)
    }
  }

  // -- another way, deprecate?

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
          input.request.headerMap.get(authHeader).map(validate).getOrElse(Future.False))

    private[this] def validate(headerContent: String): Future[Boolean] =
      JwtCirce.decodeJson(headerContent, key, Seq(algorithm)) match {
        case Success(_) =>
          Future.True
        case Failure(_) =>
          Future.False
      }
  }
}
