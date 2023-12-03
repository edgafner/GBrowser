package com.github.gbrowser.reports

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Consumer
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

internal class GBrowserPluginErrorReportSubmitter : ErrorReportSubmitter() {

  private val reportURL: @NonNls String = "https://github.com/edgafner/GBrowser/issues/new?assignees=Jonatha1983&labels=bug&projects=&template=bug_report.md"

  override fun getReportActionText(): String {
    return "Report Exception"
  }

  override fun submit(events: Array<IdeaLoggingEvent>,
                      additionalInfo: String?,
                      parentComponent: Component,
                      consumer: Consumer<in SubmittedReportInfo?>): Boolean {
    val event = events.first()
    val throwableText = event.throwableText
    val throwableTextTitle = throwableText.substring(0, throwableText.length.coerceAtMost(100))
    val reportStringBuilder = buildReportUrl(event, throwableTextTitle, additionalInfo)

    BrowserUtil.browse(reportStringBuilder.toString())
    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
    return true
  }

  private fun buildReportUrl(event: IdeaLoggingEvent, throwableTextTitle: String, additionalInfo: String?): StringBuilder {
    return StringBuilder(reportURL).apply {
      appendEncoded(StringUtil.splitByLines(event.throwableText)[0])
      appendOptionalThrowableTitle(event, throwableTextTitle)
      appendEncoded("\n\n### Description\n")
      appendEncoded(StringUtil.defaultIfEmpty(additionalInfo, ""))
      appendEncoded("\n\n### Steps to Reproduce\nPlease provide code sample if applicable")
      appendEncoded("\n\n### Message\n")
      appendEncoded(StringUtil.defaultIfEmpty(event.message, ""))
      appendEncoded("\n\n### Runtime Information\n")
      appendRuntimeInformation()
      appendEncoded("\n\n### Stacktrace\n```\n")
      appendEncoded(event.throwableText.takeLast(1600))
      appendEncoded("```\n")
    }
  }

  private fun StringBuilder.appendEncoded(text: String) {
    append(URLEncoder.encode(text, StandardCharsets.UTF_8))
  }

  private fun StringBuilder.appendOptionalThrowableTitle(event: IdeaLoggingEvent, throwableTextTitle: String) {
    Optional.ofNullable(event.throwable)
      .map(Throwable::message)
      .orElseGet { StringUtil.splitByLines(throwableTextTitle)[0] }
      .let { title ->
        appendEncoded("&title=[BUG]: $title")
      }
  }

  private fun StringBuilder.appendRuntimeInformation() {
    val descriptor = PluginManagerCore.getPlugin(pluginDescriptor.pluginId)!!
    appendEncoded("Plugin version: ${descriptor.version}\n")
    appendEncoded("IDE: ${ApplicationInfo.getInstance().fullApplicationName} " +
                  "(${ApplicationInfo.getInstance().build.asString()})\n")
    appendEncoded("OS: ${SystemInfo.getOsNameAndVersion()}")
  }


}
