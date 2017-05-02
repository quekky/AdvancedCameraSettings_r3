package util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.digitalmodular.utilities.swing.table.MutableTableModel;

import de.humatic.dsj.DSMediaType;

/**
 * @author Mark Jeronimus
 */
// date 2014/08/05
public class CameraFormatSelectionDialog extends JDialog implements ListSelectionListener, ActionListener {
	private final DSMediaType[]	formats;
	private DSMediaType			selectedFormat	= null;

	private JTable				table;
	private JButton				cancelButton	= new JButton("Cancel");
	private JButton				okButton		= new JButton("Ok");

	public CameraFormatSelectionDialog(String title, DSMediaType[] formats) {
		super((JFrame)null, title, true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.formats = formats;

		initComponents();

		setSize(700, 300);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		MutableTableModel tm = new MutableTableModel(new String[]{"Format"});

		for (DSMediaType format : formats) {
			tm.add(new Object[]{format.toString()});
		}

		table = new JTable(tm);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(650);

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
		{
			JPanel p = new JPanel(new BorderLayout());
			p.setBorder(new EmptyBorder(6, 6, 6, 6));

			{
				JPanel p2 = new JPanel(new GridLayout(1, 2, 4, 4));

				cancelButton.addActionListener(this);
				p2.add(cancelButton);
				okButton.addActionListener(this);
				p2.add(okButton);

				p.add(p2, BorderLayout.EAST);
			}

			add(p, BorderLayout.SOUTH);
		}

		if (formats.length > 0)
			table.getSelectionModel().setSelectionInterval(0, 0);
		else
			okButton.setEnabled(false);

		getRootPane().setDefaultButton(okButton);
	}

	public DSMediaType getSelectedFormat() {
		return selectedFormat;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		okButton.setEnabled(!table.getSelectionModel().isSelectionEmpty());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton)
			selectedFormat = formats[table.getSelectedRow()];

		dispose();
	}
}
