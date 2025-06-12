package de.dp_coding.zammadplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import de.dp_coding.zammadplugin.model.Ticket
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel

/**
 * Dialog for selecting a Zammad ticket.
 */
class TicketSelectionDialog(
    project: Project,
    private val tickets: List<Ticket>
) : DialogWrapper(project) {

    private val ticketList = JBList<Ticket>()
    var selectedTicket: Ticket? = null

    init {
        title = "Select Zammad Ticket"
        init()

        // Set the list model to contain the tickets
        ticketList.setListData(tickets.toTypedArray())
        ticketList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        ticketList.selectedIndex = if (tickets.isNotEmpty()) 0 else -1

        // Set custom cell renderer to display ticket information
        ticketList.cellRenderer = object : ColoredListCellRenderer<Ticket>() {
            override fun customizeCellRenderer(
                list: JList<out Ticket>,
                value: Ticket,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                append("#${value.number}: ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                append(value.title, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                append(" (${value.state})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val scrollPane = JBScrollPane(ticketList)
        scrollPane.preferredSize = Dimension(500, 300)
        return scrollPane
    }

    override fun doOKAction() {
        selectedTicket = ticketList.selectedValue as? Ticket
        super.doOKAction()
    }
}
