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

package uk.gov.hmrc.contactadvisors.controllers

import play.api.Logging
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.contactadvisors.connectors.models.ExternalReferenceV2
import uk.gov.hmrc.contactadvisors.domain._
import uk.gov.hmrc.contactadvisors.service.SecureMessageService
import uk.gov.hmrc.contactadvisors.views.html.secureMessage._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.EventKeys
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{ DataEvent, EventTypes }
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SecureMessageController @Inject() (
  controllerComponents: MessagesControllerComponents,
  customerAdviceAudit: CustomerAdviceAudit,
  secureMessageService: SecureMessageService,
  inboxPage: Inbox,
  inboxPageV2: InboxV2,
  successPage: Success,
  successPageV2: SuccessV2,
  duplicatePage: Duplicate,
  duplicatePageV2: DuplicateV2,
  notPaperlessPage: Not_paperless,
  unknownPage: Unknown,
  unexpectedPage: Unexpected,
  unexpectedV2Page: UnexpectedV2
)(implicit val appConfig: uk.gov.hmrc.contactadvisors.FrontendAppConfig, ec: ExecutionContext)
    extends FrontendController(controllerComponents) with I18nSupport with Logging {

  def inbox(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(
        inboxPage(
          utr,
          adviceForm.fill(Advice("Response to your enquiry from HMRC customer services", ""))
        )
      )
    )
  }

  def inboxV2 = Action.async { implicit request =>
    Future.successful(
      Ok(
        inboxPageV2(adviceFormV2.fill(AdviceV2("", "", "", "", "", "", "")))
      )
    )
  }

  def submit(utr: String) = Action.async { implicit request =>
    adviceForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(inboxPage(utr, formWithErrors))
          ),
        advice => {
          val result = secureMessageService.createMessage(advice, SaUtr(utr))
          customerAdviceAudit.auditAdvice(result, SaUtr(utr))
          result.map {
            handleStorageResult(utr)
          }
        }
      )
  }

  def submitV2() =
    Action.async { implicit request =>
      adviceFormV2
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful {
              BadRequest(inboxPageV2(formWithErrors))
            },
          advice => {
            val externalReference = ExternalReferenceV2(secureMessageService.generateExternalRefID)
            val result = secureMessageService.createMessageV2(advice, externalReference)
            customerAdviceAudit.auditAdviceV2(result, advice, externalReference)
            result.map {
              handleStorageResultV2(advice.recipientTaxidentifierValue, externalReference.id)
            }
          }
        )
    }

  def success(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(successPage(utr))
    )
  }

  def successV2() = Action.async { implicit request =>
    Future.successful(
      Ok(successPageV2())
    )
  }

  def duplicate(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(duplicatePage(utr))
    )
  }

  def duplicateV2() = Action.async { implicit request =>
    Future.successful(
      Ok(duplicatePageV2())
    )
  }

  def unexpected(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(unexpectedPage(utr))
    )
  }

  def unexpectedV2() = Action.async { implicit request =>
    Future.successful(
      Ok(unexpectedV2Page())
    )
  }

  def unknown(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(unknownPage(utr))
    )
  }

  def notPaperless(utr: String) = Action.async { implicit request =>
    Future.successful(
      Ok(notPaperlessPage(utr))
    )
  }

  def adviceForm = Form[Advice](
    mapping(
      "subject" -> nonEmptyText,
      "message" -> nonEmptyText
    )(Advice.apply)(Advice.unapply)
  )

  def adviceFormV2 = Form[AdviceV2](
    mapping(
      "subject"                     -> nonEmptyText,
      "content"                     -> nonEmptyText,
      "recipientTaxidentifierName"  -> nonEmptyText,
      "recipientTaxidentifierValue" -> nonEmptyText,
      "recipientEmail"              -> nonEmptyText,
      "recipientNameLine1"          -> nonEmptyText,
      "messageType"                 -> nonEmptyText
    )(AdviceV2.apply)(AdviceV2.unapply)
  )

  private def handleStorageResult(utr: String): StorageResult => Result = {
    case AdviceStored(_)     => Redirect(routes.SecureMessageController.success(utr))
    case AdviceAlreadyExists => Redirect(routes.SecureMessageController.duplicate(utr))
    case UnknownTaxId        => Redirect(routes.SecureMessageController.unknown(utr))
    case UserIsNotPaperless  => Redirect(routes.SecureMessageController.notPaperless(utr))
    case UnexpectedError(_)  => Redirect(routes.SecureMessageController.unexpected(utr))
  }

  private def handleStorageResultV2(
    recipientTaxIdentifierValue: String,
    externalRef: String
  ): StorageResult => Result = {
    case AdviceStored(messageId) =>
      Redirect(routes.SecureMessageController.successV2())
        .flashing(
          "taxid"       -> s"$recipientTaxIdentifierValue",
          "messageId"   -> s"$messageId",
          "externalRef" -> s"$externalRef"
        )
    case AdviceAlreadyExists =>
      Redirect(routes.SecureMessageController.duplicateV2()).flashing("taxid" -> s"$recipientTaxIdentifierValue")
    case _ =>
      Redirect(routes.SecureMessageController.unexpectedV2()).flashing("taxid" -> s"$recipientTaxIdentifierValue")
  }
}

@Singleton
class CustomerAdviceAudit @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  def auditSource: String = "customer-advisors-frontend"

  def auditAdvice(result: Future[StorageResult], taxId: SaUtr)(implicit hc: HeaderCarrier): Unit = {
    def createEvent(messageInfo: Map[String, String], auditType: String, transactionName: String) =
      DataEvent(
        auditSource = auditSource,
        auditType = auditType,
        tags = Map(EventKeys.TransactionName -> transactionName),
        detail = Map(
          taxId.name -> taxId.value
        ) ++ messageInfo
      )

    result.onComplete { res1 =>
      res1
        .map {
          case AdviceStored(messageId) =>
            createEvent(
              Map("secureMessageId" -> messageId, "messageId" -> messageId),
              EventTypes.Succeeded,
              "Message Stored"
            )
          case AdviceAlreadyExists =>
            createEvent(Map("reason" -> "Duplicate Message Found"), EventTypes.Failed, "Message Not Stored")
          case UnknownTaxId => createEvent(Map("reason" -> "Unknown Tax Id"), EventTypes.Failed, "Message Not Stored")
          case UserIsNotPaperless =>
            createEvent(Map("reason" -> "User is not paperless"), EventTypes.Failed, "Message Not Stored")
          case UnexpectedError(errorMessage) =>
            createEvent(Map("reason" -> s"Unexpected Error: $errorMessage"), EventTypes.Failed, "Message Not Stored")
        }
        .recover { case ex =>
          createEvent(Map("reason" -> s"Unexpected Error: ${ex.getMessage}"), EventTypes.Failed, "Message Not Stored")
        }
        .foreach { ev =>
          auditConnector.sendEvent(ev)
        }
    }
  }

  def auditAdviceV2(result: Future[StorageResult], advice: AdviceV2, externalReference: ExternalReferenceV2)(implicit
    hc: HeaderCarrier
  ): Unit = {
    def createEvent(messageInfo: Map[String, String], auditType: String, transactionName: String) =
      DataEvent(
        auditSource = auditSource,
        auditType = auditType,
        tags = Map(EventKeys.TransactionName -> transactionName),
        detail = Map(
        ) ++ messageInfo
      )

    result.onComplete { res1 =>
      res1
        .map {
          case AdviceStored(messageId) =>
            createEvent(
              Map(
                "messageType" -> advice.messageType,
                "messageId"   -> messageId,
                "externalRef" -> externalReference.id,
                "fhddsRef"    -> advice.recipientTaxidentifierValue
              ),
              EventTypes.Succeeded,
              "Message Created"
            )
          case AdviceAlreadyExists =>
            createEvent(
              Map(
                "messageType" -> advice.messageType,
                "messageId"   -> "",
                "externalRef" -> externalReference.id,
                "fhddsRef"    -> advice.recipientTaxidentifierValue
              ),
              EventTypes.Succeeded,
              "Message Duplicate Request"
            )
          case UnexpectedError(errorMessage) =>
            createEvent(
              Map(
                "messageType" -> advice.messageType,
                "messageId"   -> "",
                "externalRef" -> externalReference.id,
                "fhddsRef"    -> advice.recipientTaxidentifierValue,
                "reason"      -> s"$errorMessage"
              ),
              EventTypes.Failed,
              "Message Not Created"
            )
          case _ => createEvent(Map("reason" -> s"Unexpected Error"), EventTypes.Failed, "Message Not Stored")
        }
        .recover { case ex =>
          createEvent(Map("reason" -> s"Unexpected Error: ${ex.getMessage}"), EventTypes.Failed, "Message Not Stored")
        }
        .foreach { ev =>
          auditConnector.sendEvent(ev)
        }
    }
  }
}
