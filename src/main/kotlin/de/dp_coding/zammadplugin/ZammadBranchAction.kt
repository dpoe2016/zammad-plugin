package de.dp_coding.zammadplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import de.dp_coding.zammadplugin.api.ZammadService
import de.dp_coding.zammadplugin.model.Ticket
import de.dp_coding.zammadplugin.ui.TicketSelectionDialog
import de.dp_coding.zammadplugin.ui.ZammadSettingsDialog
import git4idea.GitUtil
import git4idea.branch.GitBranchUtil
import git4idea.branch.GitBrancher
import git4idea.repo.GitRepository

/**
 * Action for creating a new Git branch based on a selected Zammad ticket.
 */
class ZammadBranchAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Check if the project has Git enabled
        val gitRepository = getGitRepository(project)
        if (gitRepository == null) {
            Messages.showErrorDialog(
                project,
                "This project is not under Git version control.",
                "Cannot Create Branch"
            )
            return
        }

        // Check if Zammad service is configured
        val zammadService = ZammadService.getInstance()
        if (!zammadService.isConfigured()) {
            val result = Messages.showYesNoDialog(
                project,
                "Zammad API is not configured. Would you like to configure it now?",
                "Zammad Configuration Required",
                "Configure",
                "Cancel",
                Messages.getQuestionIcon()
            )

            if (result == Messages.YES) {
                // Show settings dialog
                val settingsDialog = ZammadSettingsDialog(project)
                if (!settingsDialog.showAndGet()) {
                    return // User cancelled settings
                }
            } else {
                return // User cancelled
            }
        }

        // Get tickets from Zammad
        try {
            val tickets = zammadService.getTicketsForCurrentUser()

            if (tickets.isEmpty()) {
                Messages.showInfoMessage(
                    project,
                    "No open tickets found for the current user.",
                    "No Open Tickets Available"
                )
                return
            }

            // Show ticket selection dialog
            val dialog = TicketSelectionDialog(project, tickets)
            if (dialog.showAndGet()) {
                val selectedTicket = dialog.selectedTicket
                if (selectedTicket != null) {
                    createBranchForTicket(project, gitRepository, selectedTicket)
                }
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to fetch tickets: ${ex.message}",
                "Error"
            )
        }
    }

    private fun getGitRepository(project: Project): GitRepository? {
        val repositories = GitUtil.getRepositoryManager(project).repositories
        return if (repositories.isEmpty()) null else repositories[0]
    }

    private fun createBranchForTicket(project: Project, repository: GitRepository, ticket: Ticket) {
        // Create a sanitized branch name from the ticket
        val sanitizedTitle = ticket.title.replace(Regex("[^a-zA-Z0-9-]"), "-").lowercase()
        val branchName = "feature/${ticket.id}-$sanitizedTitle"

        // Get the current branch
        val currentBranch = repository.currentBranch
        if (currentBranch == null) {
            Messages.showErrorDialog(
                project,
                "Could not determine the current branch.",
                "Cannot Create Branch"
            )
            return
        }

        // Create the branch
        val brancher = GitBrancher.getInstance(project)
        brancher.checkoutNewBranch(branchName, listOf(repository))

        Messages.showInfoMessage(
            project,
            "Created and checked out branch '$branchName' for ticket #${ticket.id}: ${ticket.title}",
            "Branch Created"
        )
    }
}
