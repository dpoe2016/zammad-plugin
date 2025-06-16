package de.dp_coding.zammadplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import de.dp_coding.zammadplugin.api.ZammadService;
import de.dp_coding.zammadplugin.model.Ticket;
import de.dp_coding.zammadplugin.ui.TicketSelectionDialog;
import de.dp_coding.zammadplugin.ui.ZammadSettingsDialog;
import git4idea.GitUtil;
import git4idea.branch.GitBrancher;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Action for creating a new Git branch based on a selected Zammad ticket.
 */
public class ZammadBranchAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

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

        // Check if Zammad service is configured
        ZammadService zammadService = ZammadService.getInstance();
        if (!zammadService.isConfigured()) {
            int result = Messages.showYesNoDialog(
                project,
                "Zammad API is not configured. Would you like to configure it now?",
                "Zammad Configuration Required",
                "Configure",
                "Cancel",
                Messages.getQuestionIcon()
            );

            if (result == Messages.YES) {
                // Show settings dialog
                ZammadSettingsDialog settingsDialog = new ZammadSettingsDialog(project);
                if (!settingsDialog.showAndGet()) {
                    return; // User cancelled settings
                }
            } else {
                return; // User cancelled
            }
        }

        // Get tickets from Zammad
        try {
            List<Ticket> tickets = zammadService.getTicketsForCurrentUser();

            if (tickets.isEmpty()) {
                Messages.showInfoMessage(
                    project,
                    "No open tickets found for the current user.",
                    "No Open Tickets Available"
                );
                return;
            }

            // Show ticket selection dialog
            TicketSelectionDialog dialog = new TicketSelectionDialog(project, tickets);
            if (dialog.showAndGet()) {
                Ticket selectedTicket = dialog.getSelectedTicket();
                if (selectedTicket != null) {
                    createBranchForTicket(project, gitRepository, selectedTicket);
                }
            }
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

    @Nullable
    private GitRepository getGitRepository(Project project) {
        List<GitRepository> repositories = GitUtil.getRepositoryManager(project).getRepositories();
        return repositories.isEmpty() ? null : repositories.get(0);
    }

    private void createBranchForTicket(Project project, GitRepository repository, Ticket ticket) {
        // Create a sanitized branch name from the ticket
        String sanitizedTitle = Pattern.compile("[^a-zA-Z0-9-]").matcher(ticket.getTitle()).replaceAll("-").toLowerCase();
        String branchName = "feature/" + ticket.getId() + "-" + sanitizedTitle;

        // Check if we have a current branch
        if (repository.getCurrentBranch() == null) {
            Messages.showErrorDialog(
                project,
                "Could not determine the current branch.",
                "Cannot Create Branch"
            );
            return;
        }

        // Create the branch
        GitBrancher brancher = GitBrancher.getInstance(project);
        brancher.checkoutNewBranch(branchName, Collections.singletonList(repository));

        Messages.showInfoMessage(
            project,
            "Created and checked out branch '" + branchName + "' for ticket #" + ticket.getId() + ": " + ticket.getTitle(),
            "Branch Created"
        );
    }
}
