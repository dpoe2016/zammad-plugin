package de.dp_coding.zammadplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating the Zammad tool window.
 */
public class ZammadToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create the tool window content
        TicketSelectionView ticketSelectionView = new TicketSelectionView(project);

        // Register the view with the service
        ZammadToolWindowService.getInstance().registerView(project, ticketSelectionView);

        // Add the content to the tool window
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(
                ticketSelectionView.getContent(),
                "Tickets",
                false);
        toolWindow.getContentManager().addContent(content);
    }
}
