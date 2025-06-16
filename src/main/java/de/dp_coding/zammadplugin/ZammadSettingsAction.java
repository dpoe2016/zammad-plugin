package de.dp_coding.zammadplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import de.dp_coding.zammadplugin.ui.ZammadSettingsDialog;
import org.jetbrains.annotations.NotNull;

/**
 * Action for configuring Zammad API settings.
 */
public class ZammadSettingsAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        ZammadSettingsDialog dialog = new ZammadSettingsDialog(project);
        dialog.show();
    }
}