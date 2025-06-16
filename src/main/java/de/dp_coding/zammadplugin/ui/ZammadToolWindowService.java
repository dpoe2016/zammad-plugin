package de.dp_coding.zammadplugin.ui;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import de.dp_coding.zammadplugin.model.Ticket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Service for managing Zammad tool windows.
 */
@Service
public final class ZammadToolWindowService {
    private static final String TOOL_WINDOW_ID = "Zammad Tickets";
    private final Map<Project, TicketSelectionView> viewMap = new HashMap<>();

    /**
     * Gets the instance of the service.
     *
     * @return The service instance
     */
    public static ZammadToolWindowService getInstance() {
        return ServiceManager.getService(ZammadToolWindowService.class);
    }

    /**
     * Registers a view for a project.
     *
     * @param project The project
     * @param view The view
     */
    public void registerView(@NotNull Project project, @NotNull TicketSelectionView view) {
        viewMap.put(project, view);
    }

    /**
     * Gets the view for a project.
     *
     * @param project The project
     * @return The view, or null if not found
     */
    @Nullable
    public TicketSelectionView getView(@NotNull Project project) {
        return viewMap.get(project);
    }

    /**
     * Shows the tool window for a project and sets a callback for when a ticket is selected.
     *
     * @param project The project
     * @param ticketSelectedCallback The callback to call when a ticket is selected
     * @return True if the tool window was shown, false otherwise
     */
    public boolean showToolWindow(@NotNull Project project, @NotNull Consumer<Ticket> ticketSelectedCallback) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return false;
        }

        TicketSelectionView view = getView(project);
        if (view == null) {
            return false;
        }

        view.setTicketSelectedCallback(ticketSelectedCallback);
        view.loadTickets(); // Refresh tickets
        toolWindow.show();
        return true;
    }
}