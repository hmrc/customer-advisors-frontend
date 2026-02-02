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

package uk.gov.hmrc.utils

import uk.gov.hmrc.domain.SaUtr

import java.time.LocalDate

object TestData {
  val TEST_FORM_ID = "CA001"
  val TEST_BATCH_ID = "45689256"
  val TEST_ID = "test_id"
  val TEST_SOURCE = "mdtp"
  val TEST_IDENTIFIER_NAME = "EORINumber"
  val TEST_IDENTIFIER_VALUE = "GB744638982000"
  val TEST_EMAIL_VALUE = "test@test.com"
  val TEST_TAXPAYER_LINE1 = "test_line1"
  val TEST_MSG_TYPE = "advisor-reply"
  val TEST_SUBJECT = "This is a response to your HMRC request"
  val TEST_CONTENT = "This is the content of the secure message"

  val TEST_DAY = 10
  val TEST_MONTH = 2
  val TEST_YEAR = 2026
  val TEST_LOCAL_DATE: LocalDate = LocalDate.of(TEST_YEAR, TEST_MONTH, TEST_DAY)

  val TEST_SA_UTR_VALUE = "123456789"
  val TEST_SA_UTR: SaUtr = SaUtr(TEST_SA_UTR_VALUE)
}
