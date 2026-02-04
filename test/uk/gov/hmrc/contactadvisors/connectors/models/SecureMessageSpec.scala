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

  "FHDDSTaxIdentifier.formats" should {
    import RecipientV2.fhddsTaxIdformats

    "read the json correctly" in new Setup {
      Json.parse(fHDDSTaxIdentifierJsonString).as[FHDDSTaxIdentifier] mustBe fHDDSTaxIdentifier
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(fHDDSTaxIdentifierInvalidJsonString).as[FHDDSTaxIdentifier]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(fHDDSTaxIdentifier) mustBe Json.parse(fHDDSTaxIdentifierJsonString)
    }
  }

  "TaxpayerName.formats" should {
    import RecipientV2.taxpayerFormat

    "read the json correctly" in new Setup {
      Json.parse(taxpayerNameJsonString).as[TaxpayerName] mustBe taxpayerName
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(taxpayerNameInvalidJsonString).as[TaxpayerName]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(taxpayerName) mustBe Json.parse(taxpayerNameJsonString)
    }
  }

  "Recipient.formats" should {
    import Recipient.formats

    "read the json correctly" in new Setup {
      Json.parse(recipientJsonString).as[Recipient] mustBe recipient
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(recipientInvalidJsonString).as[Recipient]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(recipient) mustBe Json.parse(recipientJsonString)
    }
  }

  "RecipientV2.formats" should {
    import RecipientV2.formats

    "read the json correctly" in new Setup {
      Json.parse(recipientV2JsonString).as[RecipientV2] mustBe recipientV2
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(recipientV2InvalidJsonString).as[RecipientV2]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(recipientV2) mustBe Json.parse(recipientV2JsonString)
    }
  }

  "ExternalReferenceV2.formats" should {
    import ExternalReferenceV2.formats

    "read the json correctly" in new Setup {
      Json.parse(externalReferenceV2JsonString).as[ExternalReferenceV2] mustBe externalReferenceV2
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(externalReferenceV2InvalidJsonString).as[ExternalReferenceV2]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(externalReferenceV2) mustBe Json.parse(externalReferenceV2JsonString)
    }
  }

  "SecureMessageV2.formats" should {
    import SecureMessageV2.formats

    "read the json correctly" in new Setup {
      Json.parse(secureMessageV2JsonString).as[SecureMessageV2] mustBe secureMessageV2
    }

    "throw exception for invalid json" in new Setup {
      intercept[JsResultException] {
        Json.parse(secureMessageV2InvalidJsonString).as[SecureMessageV2]
      }
    }

    "write the object correctly" in new Setup {
      Json.toJson(secureMessageV2) mustBe Json.parse(secureMessageV2JsonString)
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
    val recipientV2: RecipientV2 =
      RecipientV2(taxIdentifier = fHDDSTaxIdentifier, name = taxpayerName, email = TEST_EMAIL_VALUE)

    val externalReferenceV2: ExternalReferenceV2 = ExternalReferenceV2(id = TEST_ID, source = TEST_SOURCE)

    val secureMessageV2: SecureMessageV2 = SecureMessageV2(
      recipient = recipientV2,
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

    val recipientJsonString: String = """{"taxIdentifier":{"name":"sautr","value":"123456789"}}""".stripMargin
    val recipientInvalidJsonString: String = """{}""".stripMargin

    val fHDDSTaxIdentifierJsonString: String = """{"value":"GB744638982000","name":"EORINumber"}""".stripMargin
    val fHDDSTaxIdentifierInvalidJsonString: String = """{"name":"EORINumber"}""".stripMargin

    val recipientV2JsonString: String =
      """{
        |"taxIdentifier":{"value":"GB744638982000","name":"EORINumber"},
        |"name":{"line1":"test_line1"},
        |"email":"test@test.com"
        |}""".stripMargin

    val recipientV2InvalidJsonString: String =
      """{
        |"name":{"line1":"test_line1"},
        |"email":"test@test.com"
        |}""".stripMargin

    val taxpayerNameJsonString: String = """{"line1":"test_line1"}""".stripMargin
    val taxpayerNameInvalidJsonString: String = """{}""".stripMargin

    val externalReferenceV2JsonString: String = """{"id":"test_id","source":"mdtp"}""".stripMargin
    val externalReferenceV2InvalidJsonString: String = """{"source":"mdtp"}""".stripMargin

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

    val secureMessageV2JsonString: String =
      """{
        |"recipient":
        |{"taxIdentifier":{
        |"value":"GB744638982000","name":"EORINumber"},"name":{"line1":"test_line1"},"email":"test@test.com"},
        |"externalRef":{"id":"test_id","source":"mdtp"},
        |"messageType":"advisor-reply",
        |"subject":"This is a response to your HMRC request",
        |"content":"This is the content of the secure message",
        |"validFrom":"2026-02-10","alertQueue":"PRIORITY"
        |}""".stripMargin

    val secureMessageV2InvalidJsonString: String =
      """{
        |"externalRef":{"id":"test_id","source":"mdtp"},
        |"messageType":"advisor-reply",
        |"subject":"This is a response to your HMRC request",
        |"content":"This is the content of the secure message",
        |"validFrom":"2026-02-10",
        |"alertQueue":"PRIORITY"
        |}""".stripMargin
  }
}
