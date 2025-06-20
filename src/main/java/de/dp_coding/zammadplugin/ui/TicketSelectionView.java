package de.dp_coding.zammadplugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ide.BrowserUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import de.dp_coding.zammadplugin.api.ZammadService;
import de.dp_coding.zammadplugin.exception.ApiException;
import de.dp_coding.zammadplugin.exception.ConfigurationException;
import de.dp_coding.zammadplugin.exception.ErrorHandler;
import de.dp_coding.zammadplugin.exception.FeatureNotEnabledException;
import de.dp_coding.zammadplugin.exception.ZammadException;
import de.dp_coding.zammadplugin.model.Article;
import de.dp_coding.zammadplugin.model.Ticket;
import de.dp_coding.zammadplugin.model.TimeAccountingEntry;
import git4idea.GitUtil;
import git4idea.branch.GitBrancher;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.Disposable;

import javax.swing.*;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * View for selecting a Zammad ticket.
 */
public class TicketSelectionView implements Disposable {
    private static final Logger LOG = Logger.getInstance(TicketSelectionView.class);

    private final JBList<Ticket> ticketList = new JBList<>();
    private final Project project;
    private final DefaultListModel<Ticket> model = new DefaultListModel<>();
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private Consumer<Ticket> ticketSelectedCallback;

    // Split pane components
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel ticketDetailsPanel = new JPanel(new BorderLayout());
    private final JTextPane ticketDetailsTextPane = new JTextPane();

    // Time tracking variables
    private Ticket activeTimeTrackingTicket;
    private Instant timeTrackingStartTime;
    private AnAction startTimeRecordingAction;
    private AnAction stopTimeRecordingAction;

    // Timer components
    private final JLabel timerLabel = new JLabel();
    private Timer timer;


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
                    // Check if this ticket has active time recording
                    boolean isTimeRecordingActive = value.equals(activeTimeTrackingTicket);

                    // Use different style for tickets with active time recording
                    append("#" + value.getId() + ": ", new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null));
                    append(value.getTitle(), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, null));
                    append(" (" + value.getState_id() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);

                    // Add customer name if available
                    String customerInfo = getCustomerName(value);
                    if (customerInfo != null && !customerInfo.isEmpty()) {
                        append(" - " + customerInfo, SimpleTextAttributes.GRAYED_ATTRIBUTES);
                    }

                    if (isTimeRecordingActive) {
                        setBackground(new JBColor(new Color(230, 240, 255), new Color(45, 55, 70)));
                        append(" [RECORDING TIME]", new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, JBColor.BLUE));
                    }
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
                        // createBranchForTicket(selectedTicket);
                        openTicketInBrowser(selectedTicket);
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

        // Add open ticket in browser action
        actionGroup.add(new AnAction("Open in Browser", "Open the selected ticket in browser", AllIcons.General.Web) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Ticket selectedTicket = ticketList.getSelectedValue();
                if (selectedTicket != null) {
                    openTicketInBrowser(selectedTicket);
                } else {
                    Messages.showInfoMessage(project, "Please select a ticket first", "No Ticket Selected");
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(ticketList.getSelectedValue() != null);
            }
        });

        // Add settings action
        actionGroup.add(new AnAction("Settings", "Configure Zammad API connection settings", AllIcons.General.Settings) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ZammadSettingsDialog dialog = new ZammadSettingsDialog(project);
                dialog.show();
            }
        });

        // Add separator before time tracking actions
        actionGroup.addSeparator();

        // Add start time recording action
        startTimeRecordingAction = new AnAction("Start Time Recording", "Start recording time for the selected ticket", AllIcons.Actions.Execute) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Ticket selectedTicket = ticketList.getSelectedValue();
                if (selectedTicket != null) {
                    startTimeRecording(selectedTicket);
                } else {
                    Messages.showInfoMessage(project, "Please select a ticket first", "No Ticket Selected");
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(ticketList.getSelectedValue() != null && activeTimeTrackingTicket == null);
            }
        };
        actionGroup.add(startTimeRecordingAction);

        // Add stop time recording action
        stopTimeRecordingAction = new AnAction("Stop Time Recording", "Stop recording time for the active ticket", AllIcons.Actions.Suspend) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                stopTimeRecording();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(activeTimeTrackingTicket != null);
            }
        };
        actionGroup.add(stopTimeRecordingAction);

        // Add show time entries action
        actionGroup.add(new AnAction("Show Time Entries", "Show time accounting entries for the selected ticket", AllIcons.General.ShowInfos) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                showTimeAccountingEntries();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(ticketList.getSelectedValue() != null);
            }
        });


        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "ZammadToolbar", actionGroup, true);
        toolbar.setTargetComponent(mainPanel);

        // Setup timer label
        timerLabel.setForeground(JBColor.BLUE);
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD));
        timerLabel.setBorder(JBUI.Borders.empty(0, 5));
        timerLabel.setVisible(false);

        // Create a panel for the toolbar and timer
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbar.getComponent(), BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);

        // Setup ticket details panel
        ticketDetailsTextPane.setEditable(false);
        ticketDetailsTextPane.setContentType("text/html");
        ticketDetailsTextPane.setBorder(JBUI.Borders.empty(10));
        ticketDetailsPanel.add(new JBScrollPane(ticketDetailsTextPane), BorderLayout.CENTER);

        // Add hyperlink listener to handle action links
        ticketDetailsTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String url = e.getDescription();
                    Ticket selectedTicket = ticketList.getSelectedValue();

                    if (selectedTicket != null) {
                        if ("action:open".equals(url)) {
                            openTicketInBrowser(selectedTicket);
                        } else if ("action:branch".equals(url)) {
                            createBranchForTicket(selectedTicket);
                        } else if ("action:startTime".equals(url)) {
                            startTimeRecording(selectedTicket);
                        } else if ("action:stopTime".equals(url)) {
                            stopTimeRecording();
                        }
                    }
                }
            }
        });

        // Setup main panel with just the ticket list (no split pane)
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JBScrollPane(ticketList), BorderLayout.CENTER);
        mainPanel.setBorder(JBUI.Borders.empty(5));

        // Add selection listener to update details panel
        ticketList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Ticket selectedTicket = ticketList.getSelectedValue();
                    updateTicketDetails(selectedTicket);
                }
            }
        });

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
            LOG.info("Loading tickets from Zammad");
            List<Ticket> tickets = zammadService.getTicketsForCurrentUser();
            // Sort tickets by ID in descending order
            Collections.sort(tickets, (t1, t2) -> Integer.compare(t2.getId(), t1.getId()));
            for (Ticket ticket : tickets) {
                model.addElement(ticket);
            }
            LOG.info("Loaded " + tickets.size() + " tickets");
            ticketList.setSelectedIndex(tickets.isEmpty() ? -1 : 0);

            // Update details panel for the initially selected ticket
            updateTicketDetails(ticketList.getSelectedValue());
        } catch (ConfigurationException ex) {
            LOG.warn("Configuration error while loading tickets", ex);
            ErrorHandler.handleException(project, ex, "Configuration Error");
        } catch (ApiException ex) {
            LOG.warn("API error while loading tickets", ex);
            ErrorHandler.handleException(project, ex, "API Error");
        } catch (ZammadException ex) {
            LOG.warn("Error while loading tickets", ex);
            ErrorHandler.handleException(project, ex, "Error");
        } catch (Exception ex) {
            LOG.warn("Unexpected error while loading tickets", ex);
            ErrorHandler.handleException(project, ex, "Unexpected Error");
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
     * Handles ticket selection. Since we no longer show ticket details,
     * this method only calls the callback if set.
     *
     * @param ticket The selected ticket, or null if no ticket is selected
     */
    private void updateTicketDetails(@Nullable Ticket ticket) {
        if (ticket == null) {
            return;
        }

        // Call the callback if set
        if (ticketSelectedCallback != null) {
            ticketSelectedCallback.accept(ticket);
        }
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
     * Disposes of this component. If there's an active time recording, it will be stopped
     * and the time will be posted to Zammad.
     */
    @Override
    public void dispose() {
        if (activeTimeTrackingTicket != null && timeTrackingStartTime != null) {
            // Calculate elapsed time
            Duration elapsed = Duration.between(timeTrackingStartTime, Instant.now());
            long hours = elapsed.toHours();
            long minutes = elapsed.toMinutesPart();
            long seconds = elapsed.toSecondsPart();

            // Format the elapsed time
            String elapsedTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            // Stop the timer immediately to ensure it's stopped in all scenarios
            if (timer != null) {
                timer.stop();
                timer = null;
            }

            // Send the time entry to Zammad
            ZammadService zammadService = ZammadService.getInstance();
            try {
                // Use a default note for automatic time entries
                String note = "Automatically recorded time when IDE was closed";

                LOG.info("Recording time for ticket ID: " + activeTimeTrackingTicket.getId() + " with time: " + elapsedTimeStr);
                zammadService.createTimeAccountingEntry(
                    activeTimeTrackingTicket.getId(),
                    elapsedTimeStr
                );

                // Reset the state
                activeTimeTrackingTicket = null;
                timeTrackingStartTime = null;
                LOG.info("Time recording stopped and saved");
            } catch (ZammadException ex) {
                // Log the error but don't show a dialog as the IDE might be shutting down
                LOG.warn("Failed to record time: " + ex.getMessage(), ex);
                ErrorHandler.handleExceptionSilently(ex, "Time recording on dispose");
            }
        }
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
        String branchName = ticket.getId() + "-" + sanitizedTitle;

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

    /**
     * Gets the customer name for a ticket.
     * 
     * @param ticket The ticket to get the customer name for
     * @return The customer name, or the customer ID if the name couldn't be retrieved
     */
    private String getCustomerName(Ticket ticket) {
        String customerId = ticket.getCustomer_id();
        if (customerId == null || customerId.isEmpty()) {
            return "";
        }

        try {
            // Check if the customer field is a numeric ID
            int userId = Integer.parseInt(customerId);

            // Get the user information from the API
            ZammadService zammadService = ZammadService.getInstance();
            try {
                LOG.info("Fetching customer name for user ID: " + userId);
                de.dp_coding.zammadplugin.model.User user = zammadService.getUserById(userId);
                if (user != null) {
                    LOG.info("Found customer name: " + user.getFullName());
                    return user.getFullName();
                }
            } catch (ConfigurationException ex) {
                // If there's an error, just return the customer ID
                LOG.warn("Configuration error while fetching customer name", ex);
                // Don't show error dialog for customer name as it's not critical
            } catch (ApiException ex) {
                // If there's an error, just return the customer ID
                LOG.warn("API error while fetching customer name", ex);
                // Don't show error dialog for customer name as it's not critical
            } catch (ZammadException ex) {
                // If there's an error, just return the customer ID
                LOG.warn("Error while fetching customer name", ex);
                // Don't show error dialog for customer name as it's not critical
            }
        } catch (NumberFormatException e) {
            // If the customer field is not a numeric ID, just return it as is
            LOG.info("Customer ID is not a numeric ID: " + customerId);
            return customerId;
        }

        return customerId;
    }

    /**
     * Opens the selected ticket in the browser.
     *
     * @param ticket The ticket to open in the browser
     */
    private void openTicketInBrowser(Ticket ticket) {
        ZammadService zammadService = ZammadService.getInstance();
        if (!zammadService.isConfigured()) {
            Messages.showErrorDialog(
                project,
                "Zammad service is not configured. Please set the Zammad URL and API token.",
                "Configuration Error"
            );
            return;
        }

        String zammadUrl = zammadService.getZammadUrl();
        if (zammadUrl.endsWith("/")) {
            zammadUrl = zammadUrl.substring(0, zammadUrl.length() - 1);
        }

        // Construct the ticket URL
        String ticketUrl = zammadUrl + "/#ticket/zoom/" + ticket.getId();

        // Open the URL in the browser
        BrowserUtil.browse(ticketUrl);
    }

    /**
     * Starts recording time for the given ticket.
     *
     * @param ticket The ticket to record time for
     */
    private void startTimeRecording(Ticket ticket) {
        if (activeTimeTrackingTicket != null) {
            // Already recording time for another ticket
            int result = Messages.showYesNoDialog(
                project,
                "You are already recording time for ticket #" + activeTimeTrackingTicket.getId() + 
                ". Do you want to stop that and start recording for ticket #" + ticket.getId() + "?",
                "Time Recording Already Active",
                Messages.getQuestionIcon()
            );

            if (result != Messages.YES) {
                return;
            }

            // Stop current recording
            stopTimeRecording();
        }

        // Start recording time for the new ticket
        activeTimeTrackingTicket = ticket;
        timeTrackingStartTime = Instant.now();

        // Start the timer
        startTimer();

        Messages.showInfoMessage(
            project,
            "Started recording time for ticket #" + ticket.getId() + ": " + ticket.getTitle(),
            "Time Recording Started"
        );
    }

    /**
     * Starts the timer to update the elapsed time display.
     */
    private void startTimer() {
        // Initialize the timer label
        updateTimerDisplay();
        timerLabel.setVisible(true);

        // Create and start the timer (updates every second)
        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(1000, e -> updateTimerDisplay());
        timer.start();
    }

    /**
     * Updates the timer display with the current elapsed time.
     */
    private void updateTimerDisplay() {
        if (timeTrackingStartTime == null) {
            return;
        }

        // Calculate elapsed time
        Duration elapsed = Duration.between(timeTrackingStartTime, Instant.now());
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        long seconds = elapsed.toSecondsPart();

        // Format the elapsed time
        String elapsedTimeStr = String.format("Recording: %02d:%02d:%02d", hours, minutes, seconds);

        // Update the label
        timerLabel.setText(elapsedTimeStr);
    }

    /**
     * Stops recording time for the active ticket and sends the time entry to Zammad.
     */
    private void stopTimeRecording() {
        if (activeTimeTrackingTicket == null || timeTrackingStartTime == null) {
            Messages.showWarningDialog(
                project,
                "No active time recording found.",
                "No Time Recording"
            );
            return;
        }

        // Calculate elapsed time
        Duration elapsed = Duration.between(timeTrackingStartTime, Instant.now());
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        long seconds = elapsed.toSecondsPart();

        // Format the elapsed time
        String elapsedTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // Stop the timer immediately to ensure it's stopped in all scenarios
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        timerLabel.setVisible(false);

        // Ask for a note
//        String note = Messages.showInputDialog(
//            project,
//            "Enter a note for this time entry (optional):",
//            "Time Entry Note",
//            Messages.getQuestionIcon()
//        );

        // Send the time entry to Zammad
        ZammadService zammadService = ZammadService.getInstance();
        try {
            LOG.info("Recording time for ticket ID: " + activeTimeTrackingTicket.getId() + " with time: " + elapsedTimeStr);
            TimeAccountingEntry entry = zammadService.createTimeAccountingEntry(
                activeTimeTrackingTicket.getId(),
                elapsedTimeStr
            );

            // Show success message
            LOG.info("Time recording stopped and saved");
            Messages.showInfoMessage(
                project,
                "Recorded " + elapsedTimeStr + " for ticket #" + activeTimeTrackingTicket.getId() + 
                ": " + activeTimeTrackingTicket.getTitle(),
                "Time Recording Stopped"
            );

            // Reset the state
            activeTimeTrackingTicket = null;
            timeTrackingStartTime = null;

            // Refresh the ticket list to show updated time entries
            loadTickets();
        } catch (ConfigurationException ex) {
            LOG.warn("Configuration error while recording time", ex);
            ErrorHandler.handleException(project, ex, "Configuration Error");
        } catch (FeatureNotEnabledException ex) {
            LOG.warn("Feature not enabled error while recording time", ex);
            ErrorHandler.handleException(project, ex, "Feature Not Enabled");
        } catch (ApiException ex) {
            LOG.warn("API error while recording time", ex);
            ErrorHandler.handleException(project, ex, "API Error");
        } catch (ZammadException ex) {
            LOG.warn("Error while recording time", ex);
            ErrorHandler.handleException(project, ex, "Error");
        }
    }

    /**
     * Shows time accounting entries for the selected ticket.
     */
    private void showTimeAccountingEntries() {
        Ticket selectedTicket = ticketList.getSelectedValue();
        if (selectedTicket == null) {
            Messages.showInfoMessage(project, "Please select a ticket first", "No Ticket Selected");
            return;
        }

        ZammadService zammadService = ZammadService.getInstance();
        try {
            LOG.info("Fetching time accounting entries for ticket ID: " + selectedTicket.getId());
            List<TimeAccountingEntry> entries = zammadService.getTimeAccountingEntries(selectedTicket.getId());

            if (entries.isEmpty()) {
                LOG.info("No time entries found for ticket ID: " + selectedTicket.getId());
                Messages.showInfoMessage(
                    project,
                    "No time entries found for ticket #" + selectedTicket.getId() + ": " + selectedTicket.getTitle(),
                    "No Time Entries"
                );
                return;
            }

            // Build a message with all time entries
            StringBuilder message = new StringBuilder();
            message.append("Time entries for ticket #").append(selectedTicket.getId())
                  .append(": ").append(selectedTicket.getTitle()).append("\n\n");

            for (TimeAccountingEntry entry : entries) {
                message.append("Time: ").append(entry.getTime())
                      .append(", Created: ").append(entry.getCreatedAt());

                if (entry.getNote() != null && !entry.getNote().isEmpty()) {
                    message.append("\nNote: ").append(entry.getNote());
                }

                message.append("\n");
            }

            LOG.info("Showing " + entries.size() + " time entries for ticket ID: " + selectedTicket.getId());
            // Show the time entries
            Messages.showInfoMessage(
                project,
                message.toString(),
                "Time Entries"
            );
        } catch (ConfigurationException ex) {
            LOG.warn("Configuration error while fetching time entries", ex);
            ErrorHandler.handleException(project, ex, "Configuration Error");
        } catch (FeatureNotEnabledException ex) {
            LOG.warn("Feature not enabled error while fetching time entries", ex);
            ErrorHandler.handleException(project, ex, "Feature Not Enabled");
        } catch (ApiException ex) {
            LOG.warn("API error while fetching time entries", ex);
            ErrorHandler.handleException(project, ex, "API Error");
        } catch (ZammadException ex) {
            LOG.warn("Error while fetching time entries", ex);
            ErrorHandler.handleException(project, ex, "Error");
        }
    }
}
