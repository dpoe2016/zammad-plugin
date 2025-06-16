package de.dp_coding.zammadplugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import de.dp_coding.zammadplugin.model.Ticket;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for selecting a Zammad ticket.
 */
public class TicketSelectionDialog extends DialogWrapper {
    private final JBList<Ticket> ticketList = new JBList<>();
    private final List<Ticket> tickets;
    private Ticket selectedTicket;

    public TicketSelectionDialog(Project project, List<Ticket> tickets) {
        super(project);
        this.tickets = tickets;
        setTitle("Select Zammad Ticket");
        init();

        // Set the list model to contain the tickets
        ticketList.setListData(tickets.toArray(new Ticket[0]));
        ticketList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketList.setSelectedIndex(tickets.isEmpty() ? -1 : 0);

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
                append("#" + value.getNumber() + ": ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                append(value.getTitle(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                append(" (" + value.getState() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBScrollPane scrollPane = new JBScrollPane(ticketList);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        return scrollPane;
    }

    @Override
    protected void doOKAction() {
        Object selectedValue = ticketList.getSelectedValue();
        if (selectedValue instanceof Ticket) {
            selectedTicket = (Ticket) selectedValue;
        }
        super.doOKAction();
    }

    @Nullable
    public Ticket getSelectedTicket() {
        return selectedTicket;
    }
}