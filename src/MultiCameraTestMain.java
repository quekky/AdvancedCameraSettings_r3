import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JFrame;
import javax.swing.Timer;

import util.CameraSettingsPanel;
import util.DirectShowCamera;
import util.FrameListener;

import com.digitalmodular.utilities.swing.ImagePanel;

/**
 * @author Mark Jeronimus
 * @version 1.0
 * @since 1.0
 * @date 2012/04/03
 */
public class MultiCameraTestMain extends JFrame implements FrameListener {
	private final DirectShowCamera[]	cameras;
	private ImagePanel[]				images;

	public MultiCameraTestMain(final DirectShowCamera[] cameras) throws Exception {
		super("Dual camera test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.cameras = cameras;
		images = new ImagePanel[cameras.length];

		for (int i = 0; i < cameras.length; i++) {
			DirectShowCamera camera = cameras[i];

			images[i] = new ImagePanel(true);

			camera.addFrameListener(this);
			camera.start();
		}

		{
			Timer t = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("initializeComponents");
					initializeComponents();
					setSize(400 * cameras.length, 600);
					setLocationRelativeTo(null);
					setVisible(true);
				}
			});
			t.setRepeats(false);
			t.start();
		}
	}

	void initializeComponents() {
		setLayout(new GridLayout(2, cameras.length));

		for (DirectShowCamera camera : cameras)
			add(new CameraSettingsPanel(camera));

		for (int i = 0; i < cameras.length; i++) {
			add(images[i]);
		}
	}

	@Override
	public synchronized void newFrame(Object source, byte[] data) {
		// Catch exceptions because somewhere up the call tree it is silently
		// caught and not reported.
		try {
			int index = -1;
			for (int i = 0; i < cameras.length; i++) {
				if (cameras[i] == source) {
					index = i;
					break;
				}
			}

			int width = cameras[index].getFormat().getWidth();
			int height = cameras[index].getFormat().getHeight();

			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			byte[] array = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
			System.arraycopy(data, 0, array, 0, array.length);

			images[index].setImage(image);

			repaint();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
