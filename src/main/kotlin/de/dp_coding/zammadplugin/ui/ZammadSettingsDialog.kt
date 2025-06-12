package de.dp_coding.zammadplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import de.dp_coding.zammadplugin.api.ZammadService
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Dialog for configuring Zammad API settings.
 */
class ZammadSettingsDialog(project: Project) : DialogWrapper(project) {
    
    private val zammadService = ZammadService.getInstance()
    private val urlField = JBTextField(zammadService.getZammadUrl(), 30)
    private val tokenField = JBTextField(zammadService.getApiToken(), 30)
    
    init {
        title = "Zammad API Settings"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Zammad URL:", urlField)
            .addLabeledComponent("API Token:", tokenField)
            .addComponent(JBLabel("Enter the URL of your Zammad instance and your API token."))
            .addComponent(JBLabel("Example URL: https://your-zammad-instance.com/"))
        
        return formBuilder.panel
    }
    
    override fun doOKAction() {
        val url = urlField.text.trim()
        val token = tokenField.text.trim()
        
        if (url.isEmpty() || token.isEmpty()) {
            return
        }
        
        // Ensure URL ends with a slash
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        
        // Save settings
        zammadService.initialize(normalizedUrl, token)
        
        super.doOKAction()
    }
}