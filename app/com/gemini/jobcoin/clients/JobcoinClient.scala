package com.gemini.jobcoin.clients

import javax.inject.Inject
import scala.concurrent.Future
import play.api.libs.ws._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import com.gemini.jobcoin.models.integration._

import play.api.libs.json._

class JobcoinClient @Inject() (ws: WSClient, config:Config) (implicit ec: ExecutionContext){
  private val jobcoinBaseUrl = config.getString("apiBaseUrl")
  private val transactionsUrl = jobcoinBaseUrl + "/transactions"
  private def checkAddressUrl(address: String) :String = jobcoinBaseUrl + s"/addresses/${address}"

  def getAddress(address: String) : Future[AddressInfo] = {
    val request: WSRequest = ws.url(checkAddressUrl(address))
    request.
      get().map{
        resp =>
          val rc: AddressInfo = resp.json.as[AddressInfo]
          rc
      }
  }

  def getTransactions() :Future[Seq[Transaction]] = {
    val request: WSRequest = ws.url(transactionsUrl)
    request.
      get().map{
      resp =>
        val rc: Seq[Transaction] = resp.json.as[Seq[Transaction]]
        rc
    }
  }

  def postTransaction(transactionRequest: TransactionRequest) : Future[Either[TransactionError, TransactionResponse]] = {
    val request: WSRequest = ws.url(transactionsUrl)
    val payload = Json.toJson(transactionRequest)
    request.post(payload).map{
      resp =>
        resp match{
          case rsp if rsp.status == 200 =>
            val rc: TransactionResponse = rsp.json.as[TransactionResponse]
            Right(rc)
          case err =>
            val rc: TransactionError = err.json.as[TransactionError]
            Left(rc)
        }
    }
  }
}
