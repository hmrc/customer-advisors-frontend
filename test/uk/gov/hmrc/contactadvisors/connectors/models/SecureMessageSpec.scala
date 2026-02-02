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

package uk.gov.hmrc.contactadvisors.connectors.models

import play.api.libs.json.{ JsResultException, Json }
import uk.gov.hmrc.utils.SpecBase
import uk.gov.hmrc.utils.TestData.*

class SecureMessageSpec extends SpecBase {

  "SecureMessage.format" should {
    import SecureMessage.formats

    "read the json correctly" in new Setup {
      Json.toJson(secureMessage) must be(Json.parse(secureMessageJsonString))
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(secureMessageInvalidJsonString).as[SecureMessage]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(secureMessage) mustBe Json.parse(secureMessageJsonString)
    }
  }

  "Details.formats" should {
    import Details.formats

    "read the json correctly" in new Setup {
      Json.parse(detailsJsonString).as[Details] mustBe details
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(detailsInvalidJsonString).as[Details]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(details) mustBe Json.parse(detailsJsonString)
    }
  }

  "ExternalReference.formats" should {
    import ExternalReference.formats

    "read the json correctly" in new Setup {
      Json.parse(externalReferenceJsonString).as[ExternalReference] mustBe externalReference
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(externalReferenceInvalidJsonString).as[ExternalReference]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(externalReference) mustBe Json.parse(externalReferenceJsonString)
    }
  }

  trait Setup {
    val details: Details =
      Details(formId = TEST_FORM_ID, statutory = true, paperSent = false, batchId = Some(TEST_BATCH_ID))

    val externalReference: ExternalReference = ExternalReference(id = TEST_ID, source = TEST_SOURCE)
    val fHDDSTaxIdentifier: FHDDSTaxIdentifier =
      FHDDSTaxIdentifier(value = TEST_IDENTIFIER_VALUE, name = TEST_IDENTIFIER_NAME)

    val taxpayerName: TaxpayerName = TaxpayerName(TEST_TAXPAYER_LINE1)
    val recipient: Recipient = Recipient(taxIdentifier = TEST_SA_UTR)
    val recipient2: RecipientV2 =
      RecipientV2(taxIdentifier = fHDDSTaxIdentifier, name = taxpayerName, email = TEST_EMAIL_VALUE)

    val externalReferenceV2: ExternalReferenceV2 = ExternalReferenceV2(id = TEST_ID, source = TEST_SOURCE)

    val secureMessageV2: SecureMessageV2 = SecureMessageV2(
      recipient = recipient2,
      externalRef = externalReferenceV2,
      messageType = TEST_MSG_TYPE,
      subject = TEST_SUBJECT,
      content = TEST_CONTENT,
      validFrom = TEST_LOCAL_DATE
    )

    val secureMessage: SecureMessage = SecureMessage(
      recipient = Recipient(TEST_SA_UTR),
      externalRef = ExternalReference("123412342314", "customer-advisor"),
      messageType = TEST_MSG_TYPE,
      subject = TEST_SUBJECT,
      content = TEST_CONTENT,
      validFrom = TEST_LOCAL_DATE,
      details = Details(formId = TEST_FORM_ID, statutory = true, paperSent = false, batchId = None)
    )

    val detailsJsonString: String =
      """{"formId":"CA001","statutory":true,"paperSent":false,"batchId":"45689256"}""".stripMargin

    val detailsInvalidJsonString: String =
      """{"statutory":true,"paperSent":false,"batchId":"45689256"}""".stripMargin

    val externalReferenceJsonString: String = """{"id":"test_id","source":"mdtp"}""".stripMargin
    val externalReferenceInvalidJsonString: String = """{"source":"mdtp"}""".stripMargin

    val recipientJsonString: String = """{}""".stripMargin
    val recipientInvalidJsonString: String = """{}""".stripMargin

    val secureMessageV2JsonString: String = """{}""".stripMargin
    val secureMessageV2InvalidJsonString: String = """{}""".stripMargin

    val fHDDSTaxIdentifierJsonString: String = """{}""".stripMargin
    val fHDDSTaxIdentifierInvalidJsonString: String = """{}""".stripMargin

    val recipientV2JsonString: String = """{}""".stripMargin
    val recipientV2InvalidJsonString: String = """{}""".stripMargin

    val taxpayerNameJsonString: String = """{}""".stripMargin
    val taxpayerNameInvalidJsonString: String = """{}""".stripMargin

    val externalReferenceV2JsonString: String = """{}""".stripMargin
    val externalReferenceV2InvalidJsonString: String = """{}""".stripMargin

    val secureMessageJsonString: String =
      s"""{
         |  "recipient": {
         |    "taxIdentifier": {
         |      "name": "sautr",
         |      "value": "123456789"
         |    }
         |  },
         |  "externalRef": {
         |    "id": "123412342314",
         |    "source": "customer-advisor"
         |  },
         |  "messageType": "advisor-reply",
         |  "subject": "This is a response to your HMRC request",
         |  "content": "This is the content of the secure message",
         |  "validFrom": "2026-02-10",
         |  "details": {
         |    "formId": "CA001",
         |    "statutory": true,
         |    "paperSent": false
         |  }
         |}""".stripMargin

    val secureMessageInvalidJsonString: String =
      s"""{
         |  "externalRef": {
         |    "id": "123412342314",
         |    "source": "customer-advisor"
         |  },
         |  "messageType": "advisor-reply",
         |  "subject": "This is a response to your HMRC request",
         |  "content": "This is the content of the secure message",
         |  "validFrom": "2026-02-10",
         |  "details": {
         |    "formId": "CA001",
         |    "statutory": true,
         |    "paperSent": false
         |  }
         |}""".stripMargin
  }
}
