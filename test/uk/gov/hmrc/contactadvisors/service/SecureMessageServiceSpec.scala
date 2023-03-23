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

package uk.gov.hmrc.contactadvisors.service

import org.apache.commons.codec.binary.Base64
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ never, verify, when }
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.contactadvisors.connectors.models.SecureMessage
import uk.gov.hmrc.contactadvisors.connectors.{ EntityResolverConnector, MessageConnector, PaperlessPreference }
import uk.gov.hmrc.contactadvisors.domain._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SecureMessageServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  "createMessage" should {

    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val hc = HeaderCarrier()

    "handle failure in creating the message" in new TestCase {

      when(entityResolverConnectorMock.validPaperlessUserWith(validSaUtr)).thenReturn(Future.successful(Some(PaperlessPreference(true))))
      when(messageConnectorMock.create(messageCaptor.capture())(any())).thenReturn(Future.successful(AdviceAlreadyExists))

      val storageResult = secureMessageService.createMessage(advice, validSaUtr).futureValue

      storageResult must be(AdviceAlreadyExists)
    }

    "handle case when taxpayer is not paperless" in new TestCase {

      when(entityResolverConnectorMock.validPaperlessUserWith(validSaUtr)).thenReturn(Future.successful(Some(PaperlessPreference(false))))

      val storageResult = secureMessageService.createMessage(advice, validSaUtr).futureValue

      verify(messageConnectorMock, never()).create(any())(any())
      storageResult must be(UserIsNotPaperless)
    }

    "handle case when taxpayer is not found" in new TestCase {

      when(entityResolverConnectorMock.validPaperlessUserWith(validSaUtr)).thenReturn(Future.successful(None))

      val storageResult = secureMessageService.createMessage(advice, validSaUtr).futureValue

      verify(messageConnectorMock, never()).create(any())(any())
      storageResult must be(UnknownTaxId)
    }

    "handle case when paperless check fails" in new TestCase {

      when(entityResolverConnectorMock.validPaperlessUserWith(validSaUtr))
        .thenReturn(Future.failed(UnexpectedFailure("Could not determine if user with utr is paperless")))

      val storageResult = secureMessageService.createMessage(advice, validSaUtr).futureValue

      verify(messageConnectorMock, never()).create(any())(any())
      storageResult must be(UnexpectedError("Creation of the advice failed. Reason: Could not determine if user with utr is paperless"))
    }
  }

  trait TestCase {

    val plainTextAdviceBody = "Advice message body"
    val encodedAdviceBody = new String(Base64.encodeBase64(plainTextAdviceBody.getBytes("UTF-8")))

    val advice = Advice("Advice subject", plainTextAdviceBody)
    val validSaUtr = SaUtr("1234")

    val messageCaptor = ArgumentCaptor.forClass(classOf[SecureMessage])

    val messageConnectorMock: MessageConnector = mock[MessageConnector]
    val entityResolverConnectorMock: EntityResolverConnector = mock[EntityResolverConnector]

    val secureMessageService = new SecureMessageService(messageConnectorMock, entityResolverConnectorMock)
  }

}
