import com.digitalmodular.utilities.ConfigManager;
import de.humatic.dsj.CaptureDeviceControls;
import java.util.ArrayList;

import util.CameraFormatSelectionDialog;
import util.CameraSelectionDialog;
import util.DirectShowCamera;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSMediaType;
import java.util.Map;

/**
 * @author Mark Jeronimus
 */
// date 2014/08/05
public class AdvancedCameraSettingsMain {
	public static void main(String[] args) throws Exception {
		if(args.length > 0 && args[0].equals("--load") ) {
			loadCamerasSettings();
		} else {
		DirectShowCamera[] cameras = queryCameras();

		if (cameras.length == 0)
			System.exit(0);

		new MultiCameraTestMain(cameras);
		}
	}

	private static DirectShowCamera[] queryCameras() {
		DSFilterInfo[] cameraInfos = DirectShowCamera.getCameras();
		ArrayList<DSFilterInfo> camerasToExclude = new ArrayList<DSFilterInfo>();
		ArrayList<DirectShowCamera> cameras = new ArrayList<DirectShowCamera>();
                DSFilterInfo cameraInfo = null;
                int i = 1;

		do {
			CameraSelectionDialog cd = new CameraSelectionDialog("Select camera " + i, cameraInfos, camerasToExclude);
			cameraInfo = cd.getSelectedCamera();
			if (cameraInfo == null)
				break;

			CameraFormatSelectionDialog cfd = new CameraFormatSelectionDialog("Select format for camera " + i,
					DirectShowCamera.getFormats(cameraInfo));
			DSMediaType format = cfd.getSelectedFormat();
			if (format == null)
				break;

			camerasToExclude.add(cameraInfo);

			DirectShowCamera camera = new DirectShowCamera(cameraInfo);
			camera.selectFormat(format);
			cameras.add(camera);
                        i++;
		} while (cameraInfo != null);

		return cameras.toArray(new DirectShowCamera[cameras.size()]);
	}

	public AdvancedCameraSettingsMain() {

	}
	
        
        private static final int[][]					propertyKeys		= {
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

	private static void loadCamerasSettings() {
            DSFilterInfo[] cameraInfos = DirectShowCamera.getCameras();
            ConfigManager.revert();
            Map<String, String> data = ConfigManager.getAllData();

            for(DSFilterInfo cameraInfo : cameraInfos) {
                DirectShowCamera camera = new DirectShowCamera(cameraInfo);

                try {
                    camera.start();
                    for (int j = 0; j < propertyKeys.length; j++) {
                        for (int i = 0; i < propertyKeys[j].length; i++) {

                            try {
                                String value = data.get(camera.getPath() + "?" + propertyKeys[j][i]);
                                if(value != null) {
                                    System.out.println("Set "+camera.getName()+" value "+propertyKeys[j][i]+" to "+value);
                                    camera.setParameterValue(propertyKeys[j][i], Integer.parseInt(value));
                                }
                            }
                            catch(Exception e) {}

                            try {
                                String value = data.get(camera.getPath() + "&" + propertyKeys[j][i]);
                                if(value != null) {
                                    System.out.println("Set "+camera.getName()+" auto "+propertyKeys[j][i]+" to "+value);
                                    camera.setAuto(propertyKeys[j][i], Boolean.parseBoolean(value));
                                }
                            }
                            catch(Exception e) {}
                        }
                    }
                    camera.stop();
                }
                catch(Exception e) {
                    System.out.println(e);
                }
            }

	}
}
