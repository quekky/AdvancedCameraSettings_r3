package util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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

import de.humatic.dsj.DSFilterInfo;

/**
 * @author Mark Jeronimus
 */
// date 2014/08/05
public class CameraSelectionDialog extends JDialog implements ListSelectionListener, ActionListener {
	private final DSFilterInfo[]	cameras;
	private ArrayList<DSFilterInfo>	camerasToExclude;
	private DSFilterInfo			selectedCamera	= null;

	private JTable					table;
	private JButton					cancelButton	= new JButton("Cancel");
	private JButton					okButton		= new JButton("Ok");

	public CameraSelectionDialog(String title, DSFilterInfo[] cameras, ArrayList<DSFilterInfo> camerasToExclude) {
		super((JFrame)null, title, true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.cameras = cameras;
		this.camerasToExclude = camerasToExclude;

		initComponents();

		setSize(700, 300);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		MutableTableModel tm = new MutableTableModel(new String[]{"Name", "Path"});

		for (DSFilterInfo camera : cameras) {
			tm.add(new Object[]{camera.getName(), camera.getPath()});
		}

		table = new JTable(tm);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		table.getColumnModel().getColumn(0).setPreferredWidth(250);
		table.getColumnModel().getColumn(1).setPreferredWidth(450);

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

		if (cameras.length > 0) {
			for (int i = 0; i < cameras.length; i++) {
				DSFilterInfo camera = cameras[i];
				if (!camerasToExclude.contains(camera)) {
					table.getSelectionModel().setSelectionInterval(i, i);
					break;
				}
			}
		} else {
			okButton.setEnabled(false);
		}

		getRootPane().setDefaultButton(okButton);
	}

	public DSFilterInfo getSelectedCamera() {
		return selectedCamera;
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
			selectedCamera = cameras[table.getSelectedRow()];

		dispose();
	}
}
