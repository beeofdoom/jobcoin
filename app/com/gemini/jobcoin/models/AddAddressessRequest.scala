package com.gemini.jobcoin.models

import play.api.libs.json._


case class AddAddressesRequest(
  addresses: Seq[String]
)

object AddAddressesRequest{

  implicit object reqReads extends Reads[AddAddressesRequest] {

    def reads(json:JsValue): JsResult[AddAddressesRequest] =  json match{
      case jsval: JsValue => {
        val addresses = (jsval \ "addresses").asOpt[List[String]]
        if(addresses.isEmpty || (addresses.isDefined && addresses.get.length <1))
          return JsError(Seq(JsPath() -> Seq(JsonValidationError("addresses field must be present and contain at least 1 address"))))
        val reqSuccess = AddAddressesRequest(addresses.get)
        JsSuccess(reqSuccess)
      }
    }
  }

  implicit val reads: Reads[AddAddressesRequest] = reqReads
}
