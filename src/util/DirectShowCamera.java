package util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import de.humatic.dsj.CaptureDeviceControls;
import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMediaType;

/**
 * @author Mark Jeronimus
 * @version 1.0
 * @since 1.0
 * @date 2012/04/02
 */
public class DirectShowCamera implements PropertyChangeListener {
	private final DSFilterInfo			camera;

	private ArrayList<FrameListener>	listeners	= new ArrayList<>();

	private DSPinInfo					pin;
	private DSMediaType					format;
	private DSCapture					capturer	= null;

	public static DSFilterInfo[] getCameras() {
		DSFilterInfo[] cameras = DSCapture.queryDevices(DSCapture.RESOLVE_OUTPUTS | DSCapture.SKIP_AUDIO | DSCapture.SKIP_XBARS
				| DSCapture.SKIP_BDA)[0];
		return Arrays.copyOf(cameras, cameras.length - 1);
	}

	public static void dumpCameras() {
		DSFilterInfo[] cameras = getCameras();

		System.out.println("Found " + (cameras.length - 1) + " cameras:");
		for (int i = 0; i < cameras.length - 1; i++) {
			DSFilterInfo device = cameras[i];
			System.out.println(i + "\t" + device.getName());
		}
	}

	public DirectShowCamera(DSFilterInfo camera) {
		this.camera = camera;
	}

	public String getName() {
		return camera.getName();
	}

	public String getPath() {
		return camera.getPath();
	}

	public static DSMediaType[] getFormats(DSFilterInfo camera) {
		ArrayList<DSMediaType> allFormats = new ArrayList<DSMediaType>();

		DSPinInfo[] pins = camera.getDownstreamPins();
		for (DSPinInfo pin : pins) {
			// Only output pins
			if (pin.getDirection() != DSPinInfo.PINDIR_OUTPUT)
				continue;

			// Only capture or unnamed pins
			if (!("Capture".equals(pin.getName()) || "".equals(pin.getName())))
				continue;

			DSMediaType[] formats = pin.getFormats();
			for (DSMediaType format : formats) {
				// Only ??? formats
				if (format.getFormatType() != 0)
					continue;

				allFormats.add(format);
			}
		}

		return allFormats.toArray(new DSMediaType[allFormats.size()]);
	}

	public void dumpFormats() {
		int count = 0;

		DSPinInfo[] pins = camera.getDownstreamPins();
		for (DSPinInfo pin : pins) {
			// Only output pins
			if (pin.getDirection() != DSPinInfo.PINDIR_OUTPUT)
				continue;

			// Only capture or unnamed pins
			if (!("Capture".equals(pin.getName()) || "".equals(pin.getName())))
				continue;

			System.out.println("Pin \"" + pin.getName() + "\"");

			DSMediaType[] formats = pin.getFormats();
			for (DSMediaType format : formats) {
				// Only ??? formats
				if (format.getFormatType() != 0)
					continue;

				System.out.println(count + ": " + format.toString());
				count++;
			}
		}

		if (count == 0)
			throw new IllegalArgumentException("Camera has no recognized formats.");
	}

	public void selectFormat(DSMediaType formatToSelect) {
		DSPinInfo[] pins = camera.getDownstreamPins();
		for (DSPinInfo pin : pins) {
			// Only output pins
			if (pin.getDirection() != DSPinInfo.PINDIR_OUTPUT)
				continue;

			// Only capture or unnamed pins
			if (!("Capture".equals(pin.getName()) || "".equals(pin.getName())))
				continue;

			DSMediaType[] formats = pin.getFormats();
			for (int i = 0; i < formats.length; i++) {
				DSMediaType format = formats[i];
				// Only ??? formats
				if (format.getFormatType() != 0)
					continue;

				if (format.equals(formatToSelect)) {
					pin.setPreferredFormat(i);
					this.pin = pin;
					this.format = formats[i];
					return;
				}
			}
		}
	}

	public void selectFormat(int formatID) {
		if (isInitialized())
			throw new IllegalArgumentException("Cannot change format while recording");

		int count = 0;

		DSPinInfo[] pins = camera.getDownstreamPins();
		for (DSPinInfo pin : pins) {
			// Only output pins
			if (pin.getDirection() != DSPinInfo.PINDIR_OUTPUT)
				continue;

			// Only capture or unnamed pins
			if (!("Capture".equals(pin.getName()) || "".equals(pin.getName())))
				continue;

			DSMediaType[] formats = pin.getFormats();
			for (int i = 0; i < formats.length; i++) {
				DSMediaType format = formats[i];

				// Only ??? formats
				if (format.getFormatType() != 0)
					continue;

				if (formatID == count) {
					pin.setPreferredFormat(i);
					this.pin = pin;
					this.format = formats[i];
					return;
				}
				count++;
			}
		}

		throw new IllegalArgumentException("Invalid format specified.");
	}

	public DSMediaType getFormat() {
		return format;
	}

	public boolean isInitialized() {
		return capturer != null;
	}

	public void start() {
		capturer = new DSCapture(DSFiltergraph.FRAME_CALLBACK, camera, false, null, this);
	}

	public void dumpParameters() {
		System.out.println("EXPOSURE   = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.EXPOSURE));
		System.out.println("GAIN       = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.GAIN));
		System.out.println("BRIGHTNESS = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.BRIGHTNESS));
		System.out.println("CONTRAST   = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.CONTRAST));
		System.out.println("SATURATION = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.SATURATION));
		System.out.println("WB         = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.WHITEBALANCE));
		System.out.println("SHARPNESS  = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.SHARPNESS));
		System.out.println("ZOOM       = "
				+ capturer.getActiveVideoDevice().getControls().getCurrentValue(CaptureDeviceControls.ZOOM));
		System.out.println("---");
	}

	public void stop() {
		capturer.stop();
		capturer.dispose();
		capturer = null;
	}

	public void addFrameListener(FrameListener listener) {
		listeners.add(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (DSJUtils.getEventType(evt)) {
			case DSFiltergraph.FRAME_NOTIFY:
				byte[] image = capturer.getData();

				for (FrameListener listener : listeners)
					listener.newFrame(this, image);

				break;
			case DSFiltergraph.ACTIVATING:
			case DSFiltergraph.INITIALIZED:
			case DSFiltergraph.TRANSPORT:
			case DSFiltergraph.GRAPH_EVENT:
			case DSFiltergraph.CLOSING:
			case DSFiltergraph.GRAPH_ERROR:
			case DSFiltergraph.CLOSED:
				break;
			default:
				System.out.println("Unknown DSJ event type: " + DSJUtils.getEventType(evt));
		}
	}

	public void showParameterDialog() {
		capturer.getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE);
	}

	/** 100..255 (default=100) */
	public void setZoomValue(int zoom) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.ZOOM, zoom, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0..-7 */
	public void setExposureValue(int exposure) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.EXPOSURE, exposure, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0..255 (lower is less noise) */
	public void setGainValue(int gain) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.GAIN, gain, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0..255 (default=128) */
	public void setBrightnessValue(int brightness) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.BRIGHTNESS, brightness, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0..255 (default=128) */
	public void setContrastValue(int contrast) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.CONTRAST, contrast, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0..255 (default=128) */
	public void setSaturationValue(int saturation) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.SATURATION, saturation, 0);
		}
		catch (DSJException e2) {}
	}

	/** 2000-6500 (default=4000..5000) */
	public void setWhitebalanceValue(int whitebalance) {
		try {
			capturer.getActiveVideoDevice().getControls()
					.setParameterValue(CaptureDeviceControls.WHITEBALANCE, whitebalance, 0);
		}
		catch (DSJException e2) {}
	}

	/** 0.. */
	public void setSharpnessValue(int sharpness) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(CaptureDeviceControls.SHARPNESS, sharpness, 0);
		}
		catch (DSJException e2) {}
	}

	public int[] getParameterRange(int key) {

		try {
			return capturer.getActiveVideoDevice().getControls().getParameterRange(key);
		}
		catch (DSJException e2) {
			return null;
		}
	}

	public int getCurrentValue(int key) {
		try {
			return capturer.getActiveVideoDevice().getControls().getCurrentValue(key);
		}
		catch (DSJException e2) {
			return 0;
		}
	}

	public void setParameterValue(int key, int value) {
		try {
			capturer.getActiveVideoDevice().getControls().setParameterValue(key, value, 0);
		}
		catch (DSJException e2) {}
	}

	public boolean getAuto(int key) {
		try {
			return capturer.getActiveVideoDevice().getControls().getAuto(key);
		}
		catch (DSJException e2) {
			return false;
		}
	}

	public void setAuto(int key, boolean auto) {
		try {
			capturer.getActiveVideoDevice().getControls().setAuto(key, auto);
		}
		catch (DSJException e2) {}
	}

	public void setPreferredFrameRate(int framerate) {
		pin.setPreferredFrameRate(framerate);
	}
}
