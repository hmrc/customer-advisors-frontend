/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.contactadvisors.connectors

import play.api.libs.json.{ Json, OFormat }
import play.mvc.Http.Status
import uk.gov.hmrc.contactadvisors.connectors.models.{ SecureMessage, SecureMessageV2 }
import uk.gov.hmrc.contactadvisors.domain._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, UpstreamErrorResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import java.net.{ URI, URL }
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessageConnector @Inject() (http: HttpClientV2, servicesConfig: ServicesConfig)(implicit ec: ExecutionContext)
    extends Status {

  lazy val serviceUrl: String = servicesConfig.baseUrl("message")
  lazy val createMessageAPIurl: URL = new URI(s"$serviceUrl/messages").toURL
  implicit val messageFormats: OFormat[MessageResponse] = MessageResponse.formats

  def create(secureMessage: SecureMessage)(implicit hc: HeaderCarrier): Future[StorageResult] =
    http
      .post(url = createMessageAPIurl)
      .withBody(Json.toJson(secureMessage))
      .execute[MessageResponse]
      .map { case MessageResponse(messageId) =>
        AdviceStored(messageId)
      }
      .recover {
        case UpstreamErrorResponse(_, Status.CONFLICT, _, _) => AdviceAlreadyExists
        case ex                                              => UnexpectedError(ex.getMessage)
      }

  def createV2(secureMessage: SecureMessageV2)(implicit hc: HeaderCarrier): Future[StorageResult] =
    http
      .post(url = createMessageAPIurl)
      .withBody(Json.toJson(secureMessage))
      .execute[MessageResponse]
      .map { case MessageResponse(messageId) =>
        AdviceStored(messageId)
      }
      .recover {
        case UpstreamErrorResponse(_, Status.CONFLICT, _, _) => AdviceAlreadyExists
        case ex                                              => UnexpectedError(ex.getMessage)
      }

}

case class MessageResponse(id: String)

object MessageResponse {
  implicit val formats: OFormat[MessageResponse] = Json.format[MessageResponse]
}
