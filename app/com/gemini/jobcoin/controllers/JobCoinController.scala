package com.gemini.jobcoin.controllers

import akka.actor.ActorSystem

import javax.inject._
import play.api._
import play.api.mvc._
import com.gemini.jobcoin.models._
import com.gemini.jobcoin.services.JobcoinMixer

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.concurrent.Akka

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class JobCoinController @Inject()(cc: ControllerComponents,jobCoinMixer: JobcoinMixer) extends AbstractController(cc) with JsonBodyParser {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def addAddresses() = Action.async(parse.json.as[AddAddressesRequest]) {
    implicit request =>
      val body: AddAddressesRequest = request.body
      val address = jobCoinMixer.addAddress(body.addresses)
      Future(Ok(s"deposit address:${address}"))
  }

  def testMixer() = Action.async{
      jobCoinMixer.mixTransactions()
      Future(Ok(s"mixed"))
  }
}
