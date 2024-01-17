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

import play.api.libs.functional.syntax._
import play.api.libs.json.{ Json, OFormat, _ }
import uk.gov.hmrc.domain.SaUtr
import java.time.LocalDate

final case class Details(formId: String, statutory: Boolean, paperSent: Boolean, batchId: Option[String])
object Details {
  implicit val formats: OFormat[Details] = Json.format[Details]
}

final case class ExternalReference(id: String, source: String)
object ExternalReference {
  implicit val formats: OFormat[ExternalReference] = Json.format[ExternalReference]
}

final case class Recipient(taxIdentifier: SaUtr)
object Recipient {

  implicit val taxIdWrites: Format[SaUtr] = (
    (__ \ "name").format[String] and
      (__ \ "value").format[String]
  )((_, value) => SaUtr(value), (m => (m.name, m.value)))

  implicit val formats: OFormat[Recipient] = Json.format[Recipient]
}

case class SecureMessage(
  recipient: Recipient,
  externalRef: ExternalReference,
  messageType: String,
  subject: String,
  content: String,
  validFrom: LocalDate,
  details: Details)

object SecureMessage {
  implicit val dateFormatDefault: Format[LocalDate] = Format(Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites)
  implicit val formats: OFormat[SecureMessage] = Json.format[SecureMessage]
}

case class SecureMessageV2(
  recipient: RecipientV2,
  externalRef: ExternalReferenceV2,
  messageType: String,
  subject: String,
  content: String,
  validFrom: LocalDate,
  alertQueue: String = "PRIORITY")

case class FHDDSTaxIdentifier(value: String, name: String)

final case class RecipientV2(taxIdentifier: FHDDSTaxIdentifier, name: TaxpayerName, email: String)

object RecipientV2 {

  implicit val fhddsTaxIdformats: OFormat[FHDDSTaxIdentifier] = Json.format[FHDDSTaxIdentifier]
  implicit val taxpayerFormat: OFormat[TaxpayerName] = Json.format[TaxpayerName]
  implicit val formats: OFormat[RecipientV2] = Json.format[RecipientV2]
}

case class TaxpayerName(line1: String)

object TaxpayerName {}

final case class ExternalReferenceV2(id: String, source: String = "sees")

object ExternalReferenceV2 {
  implicit val formats: OFormat[ExternalReferenceV2] = Json.format[ExternalReferenceV2]
}

object SecureMessageV2 {
  implicit val dateFormatDefault: Format[LocalDate] = Format(Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites)
  implicit val formats: OFormat[SecureMessageV2] = Json.format[SecureMessageV2]
}
