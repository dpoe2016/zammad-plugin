package de.dp_coding.zammadplugin.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import de.dp_coding.zammadplugin.api.ZammadService;
import de.dp_coding.zammadplugin.model.Ticket;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.DefaultListModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * View for selecting a Zammad ticket.
 */
public class TicketSelectionView {
    private final JBList<Ticket> ticketList = new JBList<>();
    private final Project project;
    private final DefaultListModel<Ticket> model = new DefaultListModel<>();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private Consumer<Ticket> ticketSelectedCallback;

    public TicketSelectionView(Project project) {
        this.project = project;

        // Setup the list
        ticketList.setModel(model);
        ticketList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set custom cell renderer to display ticket information
        ticketList.setCellRenderer(new ColoredListCellRenderer<Ticket>() {
            @Override
            protected void customizeCellRenderer(
                JList<? extends Ticket> list,
                Ticket value,
                int index,
                boolean selected,
                boolean hasFocus
            ) {
                if (value != null) {
                    append("#" + value.getNumber() + ": ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    append(value.getTitle(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    append(" (" + value.getState() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }
            }
        });

        // Add double-click listener
        ticketList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ticketSelectedCallback != null) {
                Ticket selectedTicket = ticketList.getSelectedValue();
                if (selectedTicket != null) {
                    ticketSelectedCallback.accept(selectedTicket);
                }
            }
        });

        // Create toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        // TODO: Add refresh action if needed

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "ZammadToolbar", actionGroup, true);
        toolbar.setTargetComponent(mainPanel);

        // Setup main panel
        mainPanel.add(toolbar.getComponent(), BorderLayout.NORTH);
        mainPanel.add(new JBScrollPane(ticketList), BorderLayout.CENTER);
        mainPanel.setBorder(JBUI.Borders.empty(5));

        // Load tickets initially
        loadTickets();
    }

    /**
     * Loads tickets from the Zammad service.
     */
    public void loadTickets() {
        model.clear();

        ZammadService zammadService = ZammadService.getInstance();
        if (!zammadService.isConfigured()) {
            // Show message in the list instead
            return;
        }

        try {
            List<Ticket> tickets = zammadService.getTicketsForCurrentUser();
            for (Ticket ticket : tickets) {
                model.addElement(ticket);
            }
            ticketList.setSelectedIndex(tickets.isEmpty() ? -1 : 0);
        } catch (IOException ex) {
            Messages.showErrorDialog(
                project,
                "Failed to fetch tickets: " + ex.getMessage(),
                "Error"
            );
        } catch (Exception ex) {
            Messages.showErrorDialog(
                project,
                "An unexpected error occurred: " + ex.getMessage(),
                "Error"
            );
        }
    }

    /**
     * Sets the callback to be called when a ticket is selected.
     *
     * @param callback Consumer that will be called with the selected ticket
     */
    public void setTicketSelectedCallback(@NotNull Consumer<Ticket> callback) {
        this.ticketSelectedCallback = callback;
    }

    /**
     * Gets the main panel for this view.
     *
     * @return The main panel
     */
    public JComponent getContent() {
        return mainPanel;
    }
}
