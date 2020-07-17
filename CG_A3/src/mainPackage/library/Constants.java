package mainPackage.library;

public class Constants {
	
	// Camera coordinates
	/* Front View
	public static float[] VPN = new float[]{50.0f, 186.0f, 50.0f};
	public static float[] VUP = new float[]{0,0,-1};
	public static float[] VRP = new float[]{0.0f,-200.0f,0.0f};
	*/
	/* Bottom View
	public static float[] VPN = new float[]{0.0f, 0.0f, -186.0f};
	public static float[] VUP = new float[]{0,-1,0};
	public static float[] VRP = new float[]{64.0f,64.0f,250.0f};
	*/
	// Back View
	public static float[] VPN = new float[]{0.0f, -186.0f, 0.0f};
	public static float[] VUP = new float[]{0,0,-1};
	public static float[] VRP = new float[]{64.0f, 250.0f, 64.0f};
	
	
	// Definition of Image buffer
	public final static int iROWS = 512;
	public final static int iCOLS = 512;
	
	// Shading Data Storage
	public static final int SIZE = 128;
	
	// Dimensions of Data file
	public final static int ROWS = 128;
	public final static int COLS = 128;
	public final static int SLCS = 128;
	public final static int THRESHOLD_OPACITY = 40;
	
	/* Definition of the window on the image plane in the camera coordinates.
	 * For mapping (j,i) in the screen coordinate to (x,y) on the image plane.
	 * The specified screen size stimulates the 35mm film.
	 */
	public static float Xmin = -0.0175f;
	public static float Ymin = -0.0175f;
	public static float Xmax = 0.0175f;
	public static float Ymax = 0.0175f;
	
	public static float Kd = 0.75f;
	
	// Setting Light Source position and intensity parameters
	public static float[] LRP = new float[]{.577f, -.577f, -.577f};
	public static float LIGHT_SOURCE_INTENSITY = 255.0f;

}
