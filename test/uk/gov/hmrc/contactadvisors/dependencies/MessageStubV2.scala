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

/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.contactadvisors.dependencies

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.apache.commons.codec.binary.Base64
import play.api.http.Status
import uk.gov.hmrc.contactadvisors.domain.AdviceV2

import java.time.LocalDate

trait MessageStubV2 {
  val messageEndpoint = "/messages"

  val duplicatedMessage =
    (
      Status.CONFLICT,
      s"""
         |{
         |  "reason": "Duplicated message content or external reference ID"
         |}
     """.stripMargin
    )

  val successfulResponse: (Int, String) =
    (
      Status.CREATED,
      s"""
         |{
         |  "id" : "507f1f77bcf86cd799439011"
         |}
     """.stripMargin
    )

  def givenMessageRespondsWith(advice: AdviceV2, response: (Int, String)): StubMapping =
    givenThat(
      post(urlEqualTo(messageEndpoint))
        .withRequestBody(
          equalToJson(
            s"""
               |{"recipient":
               | {"taxIdentifier":
               |  {
               |   "name":"${advice.recipientTaxidentifierName}",
               |   "value":"${advice.recipientTaxidentifierValue}"
               |  },
               |  "name":
               |  {"line1":"${advice.recipientNameLine1}"},
               |  "email":"${advice.recipientEmail}"},
               | "externalRef":
               | {
               |  "source":"sees"
               | },
               | "messageType":"${advice.messageType}",
               | "subject":"${advice.subject}",
               | "content":"${new String(Base64.encodeBase64(advice.content.getBytes("UTF-8")))}",
               | "validFrom":"${LocalDate.now()}",
               | "alertQueue":"PRIORITY"
               |}
         """.stripMargin,
            true,
            true
          )
        )
        .willReturn(aResponse().withStatus(response._1).withBody(response._2))
    )
}
