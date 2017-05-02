package util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.digitalmodular.utilities.ConfigManager;
import com.digitalmodular.utilities.swing.TableLayout;

import de.humatic.dsj.CaptureDeviceControls;

/**
 * @author Mark Jeronimus
 */
// date 2014/08/05
public class CameraSettingsPanel extends JTabbedPane implements ChangeListener, ActionListener, Runnable {
	private String[]				tabs				= {"Color", "Image", "Mechanical", "Sound", "CAMCONTROL_", "VC_", "LT_"};
	private String[][]				propertyStrings		= {
			{"EXPOSURE", "GAIN", "BRIGHTNESS", "CONTRAST", "SATURATION", "HUE", "COLORENABLE", "WHITEBALANCE", "GAMMA"},
			{"BACKLIGHTCOMPENSATION", "SHARPNESS", "INPUT_LEVEL", "INPUT_SELECT"},
			{"FOCUS", "IRIS", "ZOOM", "PAN", "TILT", "ROLL"},
			{"MASTER_VOL", "MASTER_PAN", "TREBLE", "BASS", "BALANCE"},
			{"CAMCONTROL_ABSOLUTE", "CAMCONTROL_AUTO", "CAMCONTROL_MANUAL", "CAMCONTROL_RELATIVE"},
			{"VC_FLIP_HOR", "VC_FLIP_VER", "VC_TRIGGER", "VC_TRIGGER_ENABLE"},
			{"LT_DIGITAL_PAN", "LT_DIGITAL_PANTILTZOOM", "LT_DIGITAL_TILT", "LT_DIGITAL_ZOOM", "LT_EXPOSURE_TIME",
			"LT_FACE_TRACKING", "LT_FINDFACE", "LT_LED"}};
	private int[][]					propertyKeys		= {
			{CaptureDeviceControls.EXPOSURE, CaptureDeviceControls.GAIN, CaptureDeviceControls.BRIGHTNESS,
			CaptureDeviceControls.CONTRAST, CaptureDeviceControls.SATURATION, CaptureDeviceControls.HUE,
			CaptureDeviceControls.COLORENABLE, CaptureDeviceControls.WHITEBALANCE, CaptureDeviceControls.GAMMA},
			{CaptureDeviceControls.BACKLIGHTCOMPENSATION, CaptureDeviceControls.SHARPNESS, CaptureDeviceControls.INPUT_LEVEL,
			CaptureDeviceControls.INPUT_SELECT},
			{CaptureDeviceControls.FOCUS, CaptureDeviceControls.IRIS, CaptureDeviceControls.ZOOM, CaptureDeviceControls.PAN,
			CaptureDeviceControls.TILT, CaptureDeviceControls.ROLL},
			{CaptureDeviceControls.MASTER_VOL, CaptureDeviceControls.MASTER_PAN, CaptureDeviceControls.TREBLE,
			CaptureDeviceControls.BASS, CaptureDeviceControls.BALANCE},
			{CaptureDeviceControls.CAMCONTROL_ABSOLUTE, CaptureDeviceControls.CAMCONTROL_AUTO,
			CaptureDeviceControls.CAMCONTROL_MANUAL, CaptureDeviceControls.CAMCONTROL_RELATIVE},
			{CaptureDeviceControls.VC_FLIP_HOR, CaptureDeviceControls.VC_FLIP_VER, CaptureDeviceControls.VC_TRIGGER,
			CaptureDeviceControls.VC_TRIGGER_ENABLE},
			{CaptureDeviceControls.LT_DIGITAL_PAN, CaptureDeviceControls.LT_DIGITAL_PANTILTZOOM,
			CaptureDeviceControls.LT_DIGITAL_TILT, CaptureDeviceControls.LT_DIGITAL_ZOOM,
			CaptureDeviceControls.LT_EXPOSURE_TIME, CaptureDeviceControls.LT_FACE_TRACKING, CaptureDeviceControls.LT_FINDFACE,
			CaptureDeviceControls.LT_LED}				};

	private final DirectShowCamera	camera;

	private final int[][][]			ranges				= new int[propertyStrings.length][][];
	private final int[][]			originalValues		= new int[propertyStrings.length][];
	private final boolean[][]		originalAutos		= new boolean[propertyStrings.length][];
	private final JSlider[][]		valueSlider			= new JSlider[propertyStrings.length][];
	private final JTextField[][]	valueField			= new JTextField[propertyStrings.length][];
	private final JCheckBox[][]		autoCheckbox		= new JCheckBox[propertyStrings.length][];

	private final JButton			defaultButton		= new JButton("Reset defaults");
	private final JButton			disableAutoButton	= new JButton("Disable all auto");
	private final JButton			loadButton			= new JButton("Load settings");
	private final JButton			saveButton			= new JButton("Save settings");
	private final JButton			revertButton		= new JButton("Revert settings");

	private int						machineEvent		= 0;

	public CameraSettingsPanel(DirectShowCamera camera) {
		this.camera = camera;

		initComponents();
		camera.setPreferredFrameRate(10);

		new Thread(this, CameraSettingsPanel.class.getSimpleName()).start();
	}

	private void initComponents() {
		for (int j = 0; j < propertyStrings.length; j++) {
			JPanel p2 = new JPanel(new BorderLayout());

			{
				JPanel p = new JPanel(new TableLayout(4, 4, 6, 10, 2, 1));
				ranges[j] = new int[propertyStrings[j].length][];
				originalValues[j] = new int[propertyStrings[j].length];
				originalAutos[j] = new boolean[propertyStrings[j].length];
				valueSlider[j] = new JSlider[propertyStrings[j].length];
				valueField[j] = new JTextField[propertyStrings[j].length];
				autoCheckbox[j] = new JCheckBox[propertyStrings[j].length];
				for (int i = 0; i < propertyStrings[j].length; i++) {
					p.add(new JLabel(propertyStrings[j][i]));

					ranges[j][i] = camera.getParameterRange(propertyKeys[j][i]);
					int value = camera.getCurrentValue(propertyKeys[j][i]);
					if (ranges[j][i][0] > ranges[j][i][1]) {
						int temp = ranges[j][i][0];
						ranges[j][i][0] = ranges[j][i][1];
						ranges[j][i][1] = temp;
					}
					boolean invalid = ranges[j][i][0] == ranges[j][i][1] || ranges[j][i][2] == 0;

					System.err.println(Arrays.toString(new int[]{invalid? 0 : ranges[j][i][0], invalid? 1 : ranges[j][i][1],
							invalid? 0 : value}));
					valueSlider[j][i] = new JSlider(invalid? 0 : ranges[j][i][0], invalid? 1 : ranges[j][i][1], invalid	? 0
																														: value);
					valueSlider[j][i].addChangeListener(this);
					p.add(valueSlider[j][i]);

					valueField[j][i] = new JTextField(Integer.toString(value));
					valueField[j][i].addActionListener(this);
					p.add(valueField[j][i]);

					boolean auto = camera.getAuto(propertyKeys[j][i]);

					autoCheckbox[j][i] = new JCheckBox("", auto);
					autoCheckbox[j][i].addActionListener(this);
					p.add(autoCheckbox[j][i]);

					if (!invalid) {
						originalValues[j][i] = value;
						originalAutos[j][i] = auto;
					} else {
						ranges[j][i] = null;
						valueSlider[j][i].setEnabled(false);
						valueField[j][i].setEnabled(false);
						autoCheckbox[j][i].setEnabled(false);
					}
				}
				p2.add(p, BorderLayout.NORTH);
			}

			add(p2, tabs[j]);
		}

		JPanel p2 = new JPanel(new BorderLayout());

		{
			JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));

			defaultButton.addActionListener(this);
			p.add(defaultButton);
			disableAutoButton.addActionListener(this);
			p.add(disableAutoButton);
			loadButton.addActionListener(this);
			p.add(loadButton);
			saveButton.addActionListener(this);
			p.add(saveButton);
			revertButton.addActionListener(this);
			p.add(revertButton);

			p2.add(p, BorderLayout.CENTER);
		}

		add(p2, "<Actions>");
	}

	@Override
	public synchronized void stateChanged(ChangeEvent e) {
		if (machineEvent > 0)
			return;
		try {
			machineEvent++;

			Object o = e.getSource();
			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (o == valueSlider[j][i]) {
						int value = valueSlider[j][i].getValue();
						if (ranges[j][i][2] > 2)
							value = value / ranges[j][i][2] * ranges[j][i][2];

						camera.setParameterValue(propertyKeys[j][i], value);
						valueSlider[j][i].setValue(value);
						valueField[j][i].setText(Integer.toString(value));
						return;
					}
				}
			}
		}
		finally {
			machineEvent--;
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if (machineEvent > 0)
			return;
		try {
			machineEvent++;

			Object o = e.getSource();
			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (o == valueField[j][i]) {
						int value = Integer.parseInt(valueField[j][i].getText());
						if (ranges[j][i][2] > 2)
							value = value / ranges[j][i][2] * ranges[j][i][2];

						camera.setParameterValue(propertyKeys[j][i], value);
						valueSlider[j][i].setValue(value);
						valueField[j][i].setText(Integer.toString(value));
						return;
					} else if (o == autoCheckbox[j][i]) {
						boolean auto = autoCheckbox[j][i].isSelected();

						camera.setAuto(propertyKeys[j][i], auto);
						return;
					}
				}
			}
			if (o == defaultButton) {
				resetDefaults();
			} else if (o == disableAutoButton) {
				disableAuto();
			} else if (o == loadButton) {
				load();
			} else if (o == saveButton) {
				save();
			} else if (o == revertButton) {
				revert();
			}
		}
		finally {
			machineEvent--;
		}
	}

	@Override
	public void run() {
		try {
			while (isVisible()) {
				Thread.sleep(200);
				getCameraValues();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private synchronized void getCameraValues() {
		try {
			machineEvent++;

			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (ranges[j][i] == null)
						continue;

					int value = camera.getCurrentValue(propertyKeys[j][i]);

					valueSlider[j][i].setValue(value);
					if (((JFrame)getTopLevelAncestor()).getFocusOwner() != valueField[j][i])
						valueField[j][i].setText(Integer.toString(value));
				}
			}
		}
		finally {
			machineEvent--;
		}
	}

	private synchronized void resetDefaults() {
		try {
			machineEvent++;

			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (ranges[j][i] == null)
						continue;

					int value = ranges[j][i][3];

					camera.setParameterValue(propertyKeys[j][i], value);
					valueSlider[j][i].setValue(value);
					valueField[j][i].setText(Integer.toString(value));
				}
			}
		}
		finally {
			machineEvent--;
		}
	}

	private synchronized void disableAuto() {
		try {
			machineEvent++;

			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (ranges[j][i] == null)
						continue;

					camera.setAuto(propertyKeys[j][i], false);
					autoCheckbox[j][i].setSelected(false);
				}
			}
		}
		finally {
			machineEvent--;
		}
	}

	private synchronized void load() {
		ConfigManager.revert();

		try {
			machineEvent++;

			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (ranges[j][i] == null)
						continue;

					int value = ConfigManager.getIntValue(camera.getPath() + "?" + propertyKeys[j][i], originalValues[j][i]);
					boolean auto = ConfigManager.getBoolValue(camera.getPath() + "&" + propertyKeys[j][i], originalAutos[j][i]);

					camera.setParameterValue(propertyKeys[j][i], value);
					camera.setAuto(propertyKeys[j][i], auto);
					valueSlider[j][i].setValue(value);
					valueField[j][i].setText(Integer.toString(value));
					autoCheckbox[j][i].setSelected(auto);
				}
			}
		}
		finally {
			machineEvent--;
		}
	}

	private synchronized void save() {
		for (int j = 0; j < propertyStrings.length; j++) {
			for (int i = 0; i < propertyStrings[j].length; i++) {
				if (ranges[j][i] == null)
					continue;

				ConfigManager.setIntValue(camera.getPath() + "?" + propertyKeys[j][i], valueSlider[j][i].getValue());
				ConfigManager.setBoolValue(camera.getPath() + "&" + propertyKeys[j][i], autoCheckbox[j][i].isSelected());
			}
		}

		ConfigManager.save();
	}

	private synchronized void revert() {
		try {
			machineEvent++;

			for (int j = 0; j < propertyStrings.length; j++) {
				for (int i = 0; i < propertyStrings[j].length; i++) {
					if (ranges[j][i] == null)
						continue;

					int value = originalValues[j][i];
					boolean auto = originalAutos[j][i];

					valueSlider[j][i].setValue(value);
					valueField[j][i].setText(Integer.toString(value));
					camera.setParameterValue(propertyKeys[j][i], value);
					camera.setAuto(propertyKeys[j][i], false);
					autoCheckbox[j][i].setSelected(auto);
				}
			}
		}
		finally {
			machineEvent--;
		}
	}
}
