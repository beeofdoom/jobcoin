package com.gemini.jobcoin.services

import com.gemini.jobcoin.clients.JobcoinClient
import com.gemini.jobcoin.models.integration.Transaction
import org.joda.time.DateTime
import org.scalatestplus.play._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.{ExecutionContext, Future}

class JobcointMixerSpec extends PlaySpec with MockitoSugar {
  class BaseFixture {

    implicit lazy val ec: ExecutionContext = ExecutionContext.Implicits.global
    val client: JobcoinClient = mock[JobcoinClient]
    val jobcoinMixer: JobcoinMixer = new JobcoinMixer(client)
  }

  def getTransactions(address: String): Seq[Transaction] = {
    val t1: Transaction = Transaction(DateTime.now().toString, address, "1")
    val t2: Transaction = Transaction(DateTime.now().toString, address, "2")
    val t3: Transaction = Transaction(DateTime.now().toString, address, "3")
    Seq(t1, t2, t3)
  }

  def get2Transactions(address:String, oldTransactions: Seq[Transaction]): Seq[Transaction] = {
    val t1: Transaction = Transaction(DateTime.now().toString, address, "1")
    val t2: Transaction = Transaction(DateTime.now().toString, address, "2")
    oldTransactions ++ Seq(t1, t2)
}


  "addAddress" must {
    "return a value" in {
      val f = new BaseFixture
      import f._
      val rc = jobcoinMixer.addAddress(Seq("one","two","three"))
      rc must not be empty
    }
  }

  "getNewTransaction" must {
    "return only new transactions" in {
      val f = new BaseFixture
      import f._
      val address = jobcoinMixer.addAddress(Seq("one","two","three"))
      when(client.getTransactions).thenReturn(Future(getTransactions(address)))
      val firstBatch = await(jobcoinMixer.getNewTransactions)
      firstBatch must have length(3)
      when(client.getTransactions).thenReturn(Future(get2Transactions(address, firstBatch)))
      Thread.sleep(1000)
      val secondBatch = await(jobcoinMixer.getNewTransactions)
      secondBatch must have length(2)
    }
  }
}
