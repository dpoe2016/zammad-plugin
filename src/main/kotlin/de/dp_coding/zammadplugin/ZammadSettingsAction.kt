package de.dp_coding.zammadplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import de.dp_coding.zammadplugin.ui.ZammadSettingsDialog

/**
 * Action for configuring Zammad API settings.
 */
class ZammadSettingsAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = ZammadSettingsDialog(project)
        dialog.show()
    }
}