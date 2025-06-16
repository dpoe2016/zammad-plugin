package de.dp_coding.zammadplugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import de.dp_coding.zammadplugin.api.ZammadService;
import de.dp_coding.zammadplugin.model.Ticket;
import git4idea.GitUtil;
import git4idea.branch.GitBrancher;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.DefaultListModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

        // Add mouse listener for double-click
        ticketList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Ticket selectedTicket = ticketList.getSelectedValue();
                    if (selectedTicket != null) {
                        createBranchForTicket(selectedTicket);
                    }
                }
            }
        });

        // Create toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        // Add refresh action
        actionGroup.add(new AnAction("Refresh Tickets", "Refresh the list of tickets", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                loadTickets();
            }
        });

        // Add create branch action
        actionGroup.add(new AnAction("Create Branch", "Create a Git branch for the selected ticket", AllIcons.Vcs.Branch) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Ticket selectedTicket = ticketList.getSelectedValue();
                if (selectedTicket != null) {
                    createBranchForTicket(selectedTicket);
                } else {
                    Messages.showInfoMessage(project, "Please select a ticket first", "No Ticket Selected");
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(ticketList.getSelectedValue() != null);
            }
        });

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

    /**
     * Creates a Git branch for the given ticket.
     *
     * @param ticket The ticket to create a branch for
     */
    private void createBranchForTicket(Ticket ticket) {
        // Check if the project has Git enabled
        GitRepository gitRepository = getGitRepository(project);
        if (gitRepository == null) {
            Messages.showErrorDialog(
                project,
                "This project is not under Git version control.",
                "Cannot Create Branch"
            );
            return;
        }

        // Create a sanitized branch name from the ticket
        String sanitizedTitle = Pattern.compile("[^a-zA-Z0-9-]").matcher(ticket.getTitle()).replaceAll("-").toLowerCase();
        String branchName = "feature/" + ticket.getId() + "-" + sanitizedTitle;

        // Check if we have a current branch
        if (gitRepository.getCurrentBranch() == null) {
            Messages.showErrorDialog(
                project,
                "Could not determine the current branch.",
                "Cannot Create Branch"
            );
            return;
        }

        // Create the branch
        GitBrancher brancher = GitBrancher.getInstance(project);
        brancher.checkoutNewBranch(branchName, Collections.singletonList(gitRepository));

        Messages.showInfoMessage(
            project,
            "Created and checked out branch '" + branchName + "' for ticket #" + ticket.getId() + ": " + ticket.getTitle(),
            "Branch Created"
        );
    }

    /**
     * Gets the Git repository for the project.
     *
     * @param project The project
     * @return The Git repository, or null if not found
     */
    @Nullable
    private GitRepository getGitRepository(Project project) {
        List<GitRepository> repositories = GitUtil.getRepositoryManager(project).getRepositories();
        return repositories.isEmpty() ? null : repositories.get(0);
    }
}
