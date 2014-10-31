package org.droidplanner.desktop.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.droidplanner.core.parameters.Parameter;

public class ParametersDialog extends JDialog {
	private static final long serialVersionUID = -1114571232968256393L;

	private JTable table;

	private DefaultTableModel tableModel;
	private static final String col[] = { "Name", "Value" };

	public ParametersDialog(ArrayList<Parameter> parameterList) {
		JFrame frame = new JFrame("Parameters");

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		tableModel = new DefaultTableModel(col, 0);
		table = new JTable(tableModel);

		JScrollPane tableContainer = new JScrollPane(table);

		panel.add(tableContainer, BorderLayout.CENTER);
		frame.getContentPane().add(panel);

		frame.pack();
		frame.setVisible(true);

		if (parameterList != null) {

			for (Parameter parameter : parameterList) {
				tableModel.addRow(new String[] { parameter.name, parameter.getValue() });
			}
		}
	}
}
