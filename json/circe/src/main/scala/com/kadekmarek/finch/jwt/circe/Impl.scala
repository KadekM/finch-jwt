package com.kadekmarek.finch.jwt.circe

import cats.data.Xor
import io.circe._
import io.finch._
import pdi.jwt._
import pdi.jwt.algorithms.JwtHmacAlgorithm
import io.circe.parser._

import scala.util.{Failure, Success}

object JwtAuthFailed extends Exception {
  override def getMessage: String = "JWT is invalid"
}

object HeaderMissing extends Exception {
  override def getMessage: String = "Authentication header was not present"
}

// TODO: support multiple algorithms

sealed trait JwtAuth {
  val key: String
  val algorithm: JwtHmacAlgorithm

  protected def by(e: Endpoint[Option[String]]): Endpoint[JwtClaim] =
    e.map(_.map(JwtCirce.decode(_, key, Seq(algorithm)))).mapOutput {
      case Some(result) =>
        result match {
          case Success(x) => Ok(x)
          case Failure(_) => Unauthorized(JwtAuthFailed)
        }
      case None => Unauthorized(HeaderMissing)
    }

  protected def byAs[A: Decoder](e: Endpoint[Option[String]]): Endpoint[A] =
    by(e).mapOutput { claim =>
      decode[A](claim.content) match {
        case Xor.Right(x) => Ok(x)
        case Xor.Left(er) => BadRequest(er)
      }
    }
}

final case class HeaderJwtAuth(key: String,
                               algorithm: JwtHmacAlgorithm,
                               headerName: String)
    extends JwtAuth {
  def auth: Endpoint[JwtClaim] = by(headerOption(headerName))
  def authAs[A: Decoder]       = byAs(headerOption(headerName))
}

final case class UrlJwtAuth(key: String, algorithm: JwtHmacAlgorithm) extends JwtAuth {
  def auth: Endpoint[JwtClaim] = by(string.map(Option.apply))
  def authAs[A: Decoder]       = byAs(string.map(Option.apply))
}
