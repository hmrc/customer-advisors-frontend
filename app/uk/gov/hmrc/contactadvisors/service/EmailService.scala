/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.contactadvisors.service

import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.libs.json.{ JsObject, JsString, JsValue }
import play.api.mvc.Result
import play.api.mvc.Results.{ BadRequest, Forbidden }
import uk.gov.hmrc.contactadvisors.connectors.EmailConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }
@Singleton
class EmailService @Inject()(emailConnector: EmailConnector, conf: Configuration) {

  lazy val clientIds = conf
    .getOptional[Seq[String]](s"email.clientIds")
    .getOrElse(List())
    .toSet

  def doSendEmail(payload: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    if (!validParameters(payload)) {
      Future.successful(BadRequest("""{"error": "invalid parameters"}"""))
    } else if (!validClientId(payload)) {
      Future.successful(Forbidden("""{"error": "invalid clientId"}"""))
    } else {
      emailConnector.send(payload)
    }

  private def validParameters(payload: JsValue): Boolean =
    (payload \ "parameters").toOption
      .collect {
        case x: JsObject if x.keys == Set("name") => true
      }
      .getOrElse(false)

  private def validClientId(payload: JsValue): Boolean =
    (payload \ "clientId").toOption
      .collect {
        case x: JsString if clientIds.contains(x.value) => true
      }
      .getOrElse(false)

}
