package com.gemini.jobcoin.models

import play.api.libs.json._

case class ErrorResponse(
                          method: String,
                          errorCode: Int,
                          errorMessage: String
                        )

object ErrorResponse{
  implicit val OFormat: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}
