package com.github.gbrowser.settings.antidetection

import com.github.gbrowser.services.GBrowserService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class GBrowserAntiDetectionSitesTableComponent(private val project: Project) : JPanel(BorderLayout()) {
  private val settings = project.service<GBrowserService>()
  private val listModel = DefaultListModel<String>()
  private val sitesList = JBList(listModel)

  init {
    initializeList()
    createUI()
  }

  private fun initializeList() {
    settings.antiDetectionSites.forEach { site ->
      listModel.addElement(site)
    }
    sitesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
  }

  private fun createUI() {
    val decorator = ToolbarDecorator.createDecorator(sitesList)
      .setAddAction {
        val dialog = AddSiteDialog(project)
        if (dialog.showAndGet()) {
          dialog.site?.let { site ->
            if (site.isNotEmpty() && !listModel.contains(site)) {
              listModel.addElement(site)
              updateSettings()
            }
          }
        }
      }
      .setRemoveAction {
        val selectedIndex = sitesList.selectedIndex
        if (selectedIndex >= 0) {
          listModel.remove(selectedIndex)
          updateSettings()
        }
      }
      .setEditAction {
        val selectedIndex = sitesList.selectedIndex
        if (selectedIndex >= 0) {
          val currentSite = listModel.getElementAt(selectedIndex)
          val dialog = EditSiteDialog(project, currentSite)
          if (dialog.showAndGet()) {
            dialog.site?.let { newSite ->
              if (newSite.isNotEmpty() && newSite != currentSite) {
                listModel.setElementAt(newSite, selectedIndex)
                updateSettings()
              }
            }
          }
        }
      }
      .disableUpDownActions()
      .createPanel()

    add(decorator, BorderLayout.CENTER)
    preferredSize = Dimension(400, 200)
  }

  private fun updateSettings() {
    val sites = mutableSetOf<String>()
    for (i in 0 until listModel.size) {
      sites.add(listModel.getElementAt(i))
    }
    settings.antiDetectionSites = sites
  }

  fun reset() {
    listModel.clear()
    settings.antiDetectionSites.forEach { site ->
      listModel.addElement(site)
    }
  }

  fun apply() {
    updateSettings()
  }

  fun isModified(): Boolean {
    val currentSites = mutableSetOf<String>()
    for (i in 0 until listModel.size) {
      currentSites.add(listModel.getElementAt(i))
    }
    return currentSites != settings.antiDetectionSites
  }

  private class AddSiteDialog(project: Project) : DialogWrapper(project) {
    private val siteField = JTextField(30)
    var site: String? = null

    init {
      title = "Add Anti-Detection Site"
      init()
    }

    override fun createCenterPanel(): JComponent {
      val panel = JPanel(BorderLayout())
      val inputPanel = JPanel()
      inputPanel.layout = BoxLayout(inputPanel, BoxLayout.Y_AXIS)

      val labelPanel = JPanel(BorderLayout())
      labelPanel.add(JLabel("Enter domain (e.g., example.com):"), BorderLayout.WEST)
      inputPanel.add(labelPanel)
      inputPanel.add(Box.createVerticalStrut(5))
      inputPanel.add(siteField)

      panel.add(inputPanel, BorderLayout.NORTH)
      panel.border = JBUI.Borders.empty(10)
      return panel
    }

    override fun doOKAction() {
      site = siteField.text.trim()
      if (site.isNullOrEmpty()) {
        Messages.showErrorDialog("Please enter a valid domain", "Invalid Input")
        return
      }
      super.doOKAction()
    }
  }

  private class EditSiteDialog(project: Project, currentSite: String) : DialogWrapper(project) {
    private val siteField = JTextField(30)
    var site: String? = null

    init {
      title = "Edit Anti-Detection Site"
      siteField.text = currentSite
      init()
    }

    override fun createCenterPanel(): JComponent {
      val panel = JPanel(BorderLayout())
      val inputPanel = JPanel()
      inputPanel.layout = BoxLayout(inputPanel, BoxLayout.Y_AXIS)

      val labelPanel = JPanel(BorderLayout())
      labelPanel.add(JLabel("Edit domain:"), BorderLayout.WEST)
      inputPanel.add(labelPanel)
      inputPanel.add(Box.createVerticalStrut(5))
      inputPanel.add(siteField)

      panel.add(inputPanel, BorderLayout.NORTH)
      panel.border = JBUI.Borders.empty(10)
      return panel
    }

    override fun doOKAction() {
      site = siteField.text.trim()
      if (site.isNullOrEmpty()) {
        Messages.showErrorDialog("Please enter a valid domain", "Invalid Input")
        return
      }
      super.doOKAction()
    }
  }
}