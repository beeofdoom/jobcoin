package com.gemini.jobcoin.controllers

import play.api.libs.json._
import play.api.mvc.BodyParser
import play.api.mvc.Results.BadRequest

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.{Success, Try}

import com.gemini.jobcoin.models.ErrorResponse


trait JsonBodyParser{
  implicit class JsonBodyParser(jsonBodyParser: BodyParser[JsValue]){
    def as[T: Reads: ClassTag] (implicit ec: ExecutionContext): BodyParser[T] = {
      jsonBodyParser.validate{  jsValue =>
        Try(jsValue.validate[T]) match{
          case Success(JsSuccess(parsedObj, _)) => Right(parsedObj)
          case Success(JsError(errors)) =>
            val errorMsg = errors.head._2.head.messages.mkString("{", ", ", "}")
            val errorResponse:ErrorResponse = ErrorResponse("requestParsing", 400, errorMsg)
          Left(BadRequest(Json.toJson(errorResponse)))
          case _ =>
            Left(BadRequest("invalid json"))
        }
      }
    }
  }
}
