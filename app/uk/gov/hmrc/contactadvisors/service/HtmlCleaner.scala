/*
 * Copyright 2017 HM Revenue & Customs
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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.safety.Whitelist


trait HtmlCleaner {
  def cleanHtml(dirtyHtml: String): String = {
    val settings = new OutputSettings().prettyPrint(false)
    return Jsoup.clean(dirtyHtml, "", relaxedWhitelistWithClassAttributes(), settings)
  }

  private def relaxedWhitelistWithClassAttributes(): Whitelist = {
    // We want to allow "class" for all allowed tags.  Unfortunately there is no way to do
    // getTags() on a Whitelist, so I have copied the list of tags from the Whitelist.relaxed()
    // implementation here.  Obviously this is not ideal, but there isn't a way around it.
    val allTags = List("a", "b", "blockquote", "br", "caption", "cite", "code", "col",
      "colgroup", "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6",
      "i", "img", "li", "ol", "p", "pre", "q", "small", "span", "strike", "strong",
      "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u",
      "ul")

    allTags.foldLeft(Whitelist.relaxed()) {
      (whitelist, tag) => whitelist.addAttributes(tag, "class")
    }
  }
}

object HtmlCleaner extends HtmlCleaner
