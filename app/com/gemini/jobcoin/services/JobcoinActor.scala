package com.gemini.jobcoin.services

import akka.actor.{Actor, Props}
import com.gemini.jobcoin.services.JobcoinMixer

class JobcoinActor(mixer: JobcoinMixer) extends Actor{
  import JobcoinActor._
  def receive() = {
    case "startMix" =>
      print("starting TransactionMix\n")
      mixer.mixTransactions()
    case _      =>
      print("received unknown message")
  }

}

object JobcoinActor{
  def props = Props[JobcoinActor]
}
