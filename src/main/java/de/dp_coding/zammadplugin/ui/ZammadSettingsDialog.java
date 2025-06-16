package de.dp_coding.zammadplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import de.dp_coding.zammadplugin.api.ZammadService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Dialog for configuring Zammad API settings.
 */
public class ZammadSettingsDialog extends DialogWrapper {
    
    private final ZammadService zammadService = ZammadService.getInstance();
    private final JBTextField urlField;
    private final JBTextField tokenField;
    
    public ZammadSettingsDialog(Project project) {
        super(project);
        urlField = new JBTextField(zammadService.getZammadUrl(), 30);
        tokenField = new JBTextField(zammadService.getApiToken(), 30);
        setTitle("Zammad API Settings");
        init();
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        FormBuilder formBuilder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Zammad URL:", urlField)
            .addLabeledComponent("API Token:", tokenField)
            .addComponent(new JBLabel("Enter the URL of your Zammad instance and your API token."))
            .addComponent(new JBLabel("Example URL: https://your-zammad-instance.com/"));
        
        return formBuilder.getPanel();
    }
    
    @Override
    protected void doOKAction() {
        String url = urlField.getText().trim();
        String token = tokenField.getText().trim();
        
        if (url.isEmpty() || token.isEmpty()) {
            return;
        }
        
        // Ensure URL ends with a slash
        String normalizedUrl = url.endsWith("/") ? url : url + "/";
        
        // Save settings
        zammadService.initialize(normalizedUrl, token);
        
        super.doOKAction();
    }
}