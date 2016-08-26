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
  override def getMessage: String = "JWT is invalid"
}

// TODO: support multiple algorithms
final case class JwtAuth(key: String, algorithm: JwtHmacAlgorithm, authHeader: String) {

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
}
