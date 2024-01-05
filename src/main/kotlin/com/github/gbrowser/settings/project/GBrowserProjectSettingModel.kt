package com.github.gbrowser.settings.project

import com.github.gbrowser.settings.dao.GBrowserHistoryDelete
import com.github.gbrowser.settings.GBrowserSetting
import kotlin.jvm.internal.Intrinsics
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class GBrowserProjectSettingModel(private var checkbox: Boolean,
                                  private var historyCount: Int,
                                  @NotNull private val historyDeleteOptions: List<String>,
                                  @NotNull private var historyDeleteSelected: String) {


  // Secondary constructor
  //constructor() : this(false, 0, DefaultSettings.INSTANCE.getHistoryDeleteOptions().map { it.displayText },
  //                     DefaultSettings.INSTANCE.getHistoryDeleteOptionDefault().displayText)

  fun getCheckbox(): Boolean = checkbox

  fun setCheckbox(value: Boolean) {
    checkbox = value
  }

  fun getHistoryCount(): Int = historyCount

  fun setHistoryCount(value: Int) {
    historyCount = value
  }

  @NotNull
  fun getHistoryDeleteOptions(): List<String> = historyDeleteOptions

  @NotNull
  fun getHistoryDeleteSelected(): String = historyDeleteSelected

  fun setHistoryDeleteSelected(@NotNull value: String) {
    Intrinsics.checkNotNullParameter(value, "<set-?>")
    historyDeleteSelected = value
  }

  @Nullable
  fun getSelectedHistoryOption(): GBrowserHistoryDelete? {
    return GBrowserSetting.instance().historyDeleteOptions.firstOrNull { it.displayText == historyDeleteSelected }
  }

  // componentN functions for destructuring declarations
  fun component1(): Boolean = checkbox
  fun component2(): Int = historyCount
  fun component3(): List<String> = historyDeleteOptions
  fun component4(): String = historyDeleteSelected

  @NotNull
  fun copy(checkbox: Boolean = this.checkbox,
           historyCount: Int = this.historyCount,
           historyDeleteOptions: List<String> = this.historyDeleteOptions,
           historyDeleteSelected: String = this.historyDeleteSelected): GBrowserProjectSettingModel {
    return GBrowserProjectSettingModel(checkbox, historyCount, historyDeleteOptions, historyDeleteSelected)
  }

  override fun toString(): String {
    return "GBrowserProjectSettingModel(checkbox=$checkbox, historyCount=$historyCount, historyDeleteOptions=$historyDeleteOptions, historyDeleteSelected='$historyDeleteSelected')"
  }

  override fun hashCode(): Int {
    var result = checkbox.hashCode()
    result = 31 * result + historyCount.hashCode()
    result = 31 * result + historyDeleteOptions.hashCode()
    result = 31 * result + historyDeleteSelected.hashCode()
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GBrowserProjectSettingModel) return false

    if (checkbox != other.checkbox) return false
    if (historyCount != other.historyCount) return false
    if (historyDeleteOptions != other.historyDeleteOptions) return false
    if (historyDeleteSelected != other.historyDeleteSelected) return false

    return true
  }
}
