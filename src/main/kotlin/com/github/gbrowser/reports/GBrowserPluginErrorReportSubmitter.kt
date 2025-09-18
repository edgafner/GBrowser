package com.github.gbrowser.reports

import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Consumer
import com.intellij.util.system.OS
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
    val throwableTextTitle = throwableText.take(throwableText.length.coerceAtMost(100))
    val reportStringBuilder = buildReportUrl(event, throwableTextTitle, additionalInfo)

    BrowserUtil.browse(reportStringBuilder.toString())
    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
    return true
  }

  private fun buildReportUrl(event: IdeaLoggingEvent, throwableTextTitle: String, additionalInfo: String?): StringBuilder {
    return StringBuilder(reportURL).apply {
      appendOptionalThrowableTitle(event, throwableTextTitle)
      append("&body=")
      appendEncoded("**Describe the bug**\n")
      appendEncoded(StringUtil.defaultIfEmpty(additionalInfo, "Auto-generated error report"))
      appendEncoded("\n\n**To Reproduce**\n")
      appendEncoded("Steps to reproduce the behavior:\n1. Go to '...'\n2. Click on '....'\n3. Scroll down to '....'\n4. See error")
      appendEncoded("\n\n**Expected behavior**\n")
      appendEncoded("A clear and concise description of what you expected to happen.")
      appendEncoded("\n\n**Screenshots**\n")
      appendEncoded("If applicable, add screenshots to help explain your problem.")
      appendEncoded("\n\n**Desktop (please complete the following information):**\n")
      appendRuntimeInformation()
      appendEncoded("\n\n**Browser [e.g. chrome, safari]**\n")
      appendEncoded("- Browser: [e.g. Chrome, Safari, Firefox]\n")
      appendEncoded("- Version: [e.g. 22]")
      appendEncoded("\n\n**Additional context**\n")
      appendEncoded("Add any other context about the problem here.")
      appendEncoded("\n\n**Error Details**\n")
      appendEncoded("```\n")
      appendEncoded(event.throwableText.take(1600))
      appendEncoded("\n```")
      appendEncoded("\n\n**Error Message**\n")
      appendEncoded(StringUtil.defaultIfEmpty(event.message, "No message available"))
    }
  }

  private fun StringBuilder.appendEncoded(text: String) {
    append(URLEncoder.encode(text, StandardCharsets.UTF_8))
  }

  private fun StringBuilder.appendOptionalThrowableTitle(event: IdeaLoggingEvent, throwableTextTitle: String) {
    val title = Optional.ofNullable(event.throwable)
      .map(Throwable::message)
      .filter { !it.isNullOrBlank() }
      .orElseGet {
        val lines = StringUtil.splitByLines(throwableTextTitle)
        if (lines.isNotEmpty()) lines[0] else "Plugin Error"
      }
    append("&title=")
    appendEncoded("[BUG]: $title")
  }

  private fun StringBuilder.appendRuntimeInformation() {
    val descriptor = PluginManagerCore.getPlugin(pluginDescriptor.pluginId)!!
    appendEncoded("- **OS:** ${OS.CURRENT}\n")
    appendEncoded("- **Plugin Version:** ${descriptor.version}\n")
    appendEncoded("- **IDE:** ${ApplicationInfo.getInstance().fullApplicationName}\n")
    appendEncoded("- **IDE Version:** ${ApplicationInfo.getInstance().build.asString()}")
  }


}
