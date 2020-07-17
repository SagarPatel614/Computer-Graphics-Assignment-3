package mainPackage;

import java.util.List;

import javax.imageio.ImageIO;

import mainPackage.library.Constants;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainClass {
	
	// Definitions of Output Image
	private static String outputFilePath = "output_image.png";
	
	// Camera coordinates
	private static Vector VPN = new Vector(Constants.VPN[0], Constants.VPN[1], Constants.VPN[2]);
	private static Vector VUP = new Vector(Constants.VUP[0], Constants.VUP[1], Constants.VUP[2]);
	private static Vector VRP = new Vector(Constants.VRP[0], Constants.VRP[1], Constants.VRP[2]);
		
	// Definition of Image buffer
	final static int iROWS = Constants.iROWS;
	final static int iCOLS = Constants.iCOLS;
	static int[][] image = new int[iROWS][iCOLS];
	
	// Dimensions of Data file
	final static int ROWS = Constants.ROWS;
	final static int COLS = Constants.COLS;
	final static int SLCS = Constants.SLCS;
	static float[][][] ctData = new float[SLCS][ROWS][COLS];
	final static int THRESHOLD_OPACITY = Constants.THRESHOLD_OPACITY;
	
	// Shading Data Storage
	private static final int SIZE = Constants.SIZE;
	public static float shadingData[][][] = new float[SIZE][SIZE][SIZE];
	
	/* Definition of the window on the image plane in the camera coordinates.
	 * For mapping (j,i) in the screen coordinate to (x,y) on the image plane.
	 * The specified screen size stimulates the 35mm film.
	 */
	static float Xmin = Constants.Xmin;
	static float Ymin = Constants.Ymin;
	static float Xmax = Constants.Xmax;
	static float Ymax = Constants.Ymax;
	
	// Declaring Vector Variables
	static Vector P0 = new Vector();
	static Vector dirV = new Vector();
	
	static float Kd = Constants.Kd;
	
	// Setting Light Source position and intensity parameters
	static Vector LRP = new Vector(Constants.LRP[0], Constants.LRP[1], Constants.LRP[2]);
	static float Ip = Constants.LIGHT_SOURCE_INTENSITY;
	
	// Global variables
	static int s = 0;
	static List<Float> Ts = new ArrayList<>();
	

	public static void main(String[] args) {
				
		Vector initialize = new Vector();
		System.out.println("Initializing Camera Coordinates!!");
		initialize.initializeMCW(VPN, VUP, VRP);
		
		// Load CT-Data
		loadCTData();
		
		// Compute Shading value at each Voxel
		computeShadingData();
		
		// Perform Volume Rendering
		System.out.println("Tracing Object...");
		
		for(int i = 0; i < iROWS; i++) {
			for(int j = 0; j < iCOLS; j++) {
				//Ray-Construction
				ray_construction(i,j);
				
				//Ray Box Intersection
				int n = ray_box_intersection();
				
				if(n == 2) {
					// Volume Ray Tracing
					image[i][j] = (int) volume_ray_tracing();
					
				}
			}
		}
		
		//save PNG image
		System.out.println("Saving Image...");
		saveImage(image);
		System.out.println("Image Saved!!");
	}
	
	// Load CT Data Method
	private static void loadCTData() {
        File file = new File("smallHead.den");
        try (FileInputStream is = new FileInputStream(file)) {

            System.out.println("Loading CT Data...");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[SIZE * SIZE];
            try {
                int layer=0;
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);

                    for (int i = 0; i < SIZE; i++) {
                        for (int j = 0; j < SIZE; j++) {
                            int i1 = data[i * SIZE + j] & 0xff;
                            // Ignores data if opacity is less than given Opacity
                            if(i1< THRESHOLD_OPACITY){
                                ctData[layer][i][j] = 0;
                            }else {
                                float i2 = i1 / 255f;       // opacity is divided by 255 get floating point value
                                ctData[layer][i][j] = i2;
                            }
                        }
                    }
                    layer++;
                    if(layer>127){
                        break;
                    }
                }
                buffer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }catch (IOException e){

        }
        //return null;
    }

	// Ray Construction method
	private static void ray_construction(int i, int j) {
		/* Step 1:
		 * Map (j,i) in the screen coordinates to (xc, yc) in the camera coordinates.
		 */
		float xc = (Xmax - Xmin) * j / (iCOLS - 1) + Xmin;
		float yc = (Ymax - Ymin) * i / (iROWS - 1) + Ymin;
		
		/* Step 2:
		 * Transform the origin (0.0, 0.0, 0.0) of the camera coordinate to P0 in the world coordinate using the transformation
		 * matrix Mcw. Note that the transformation result should be VRP.
		 */
		Vector Origin = new Vector(0f,0f,0f);
		P0 = Origin.transformC2W();

		/* Step 3:
		 * Transform the point (xc, yc, f) on the image plane in the camera coordinates to P1 in the world coordinates using the
		 * transformation matrix Mcw.
		 */
		Vector P1 = new Vector(xc, yc, Vector.focal).transformC2W();
		
		/* Step 4:
		 * Calculate dirV and normalize it to unit length.
		 */
		Vector dir = new Vector().createVector(P1, P0);
		dirV.copy(dir);
		//normalize dirV
		dirV.normalize();
		
		return;
	}
	
	// Ray Box Intersection Method
	private static int ray_box_intersection() {
		// Local Variable 
		int n = 0;	// number of intersections
		float t = 0; // temp value of t
		
		// variable for x, y, z values
		float x,y,z;
		
		// For x = 0. Case 1
		t = (0 - P0.getX()) / dirV.getX();
		y = P0.getY() + dirV.getY()*t;
		z = P0.getZ() + dirV.getZ()*t;
		if(y > 0 && y < 127 && z > 0 && z < 127) {
			Ts.add(t);
			n++;
		}
		
		// For x = 127. Case 2
		t = (127 - P0.getX()) / dirV.getX();
		y = P0.getY() + dirV.getY()*t;
		z = P0.getZ() + dirV.getZ()*t;
		if(y > 0 && y < 127 && z > 0 && z < 127) {
			Ts.add(t);
			n++;
		}
		
		// For y = 0. Case 3
		t = (0 - P0.getY()) / dirV.getY();
		x = P0.getX() + dirV.getX()*t;
		z = P0.getZ() + dirV.getZ()*t;
		if(x > 0 && x < 127 && z > 0 && z < 127) {
			Ts.add(t);
			n++;
		}
		
		// For y = 127. Case 4
		t = (127 - P0.getY()) / dirV.getY();
		x = P0.getX() + dirV.getX()*t;
		z = P0.getZ() + dirV.getZ()*t;
		if(x > 0 && x < 127 && z > 0 && z < 127) {
			Ts.add(t);
			n++;
		}
		
		// For z = 0. Case 5
		t = (0 - P0.getZ()) / dirV.getZ();
		x = P0.getX() + dirV.getX()*t;
		y = P0.getY() + dirV.getY()*t;
		if(y > 0 && y < 127 && x > 0 && x < 127) {
			Ts.add(t);
			n++;
		}
		
		// For z = 127. Case 6
		t = (127 - P0.getZ()) / dirV.getZ();
		y = P0.getY() + dirV.getY()*t;
		x = P0.getX() + dirV.getX()*t;
		if(y > 0 && y < 127 && x > 0 && x < 127) {
			Ts.add(t);
			n++;
		}
		
		return n;		
	}
	
	// Volume ray Tracing Method
	private static float volume_ray_tracing() {
		// Local Variable
		float Dt = 1.0f;
		float C = 0.0f;
		float T = 1.0f;
		float t0,t1;
		
		Vector direction = new Vector();
		
		if(Ts == null) {
			// No intersection with the cube.
		}
		/* March through the CT Volume data from ts[0] to ts[1] by step size Dt
		 * Making ts[0] < ts[1].
		 */
		t0 = Math.min(Ts.get(0),Ts.get(1));
        t1 = Math.max(Ts.get(0),Ts.get(1));
		
		for(float t = t0; t <= t1; t += Dt) {
			/* Compute the 3D coordinates of the current sample position in the Volume.
			 * x = x[t];
			 * y = y[t];
			 * z = z[t];
			 */
			
			direction.copy(dirV);
			Vector v = new Vector();
			v.copy(P0.addVector(direction.multiply(t)));
			s++;
			
			/* Obtain the shading value C and opacity value A from the shading volume and CT volume, respectively.
			 * by using the tri-linear interpolation.
			 */
			
			float opacity = 0;
			opacity = get3dInterpolatedValue(ctData, v);
			
			float shading = 0;
			shading = get3dInterpolatedValue(shadingData, v);
			
			if( T < 0.0001f) {
				break;
			}
			
			/* Accumulate the shading values in the front-to-back order.
			 * Note: We will accumulate the transparency. This value can be used in the for-loop for early termination.
			 */
			
			C += opacity*shading*T;
			
			T *= (1 - opacity);
		
		}
		
		return C;
	}
	
	// Compute Shading Method
	private static void computeShadingData() {
        Vector normal= new Vector();
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                for (int k = 1; k < SIZE - 1; k++) {

                    // x,y,z component for gradient vector is calculated
                    float x = .5f *(ctData[i][j][k + 1] - ctData[i][j][k - 1]);
                    float y = .5f *(ctData[i][j + 1][k] - ctData[i][j - 1][k]);
                    float z = .5f *(ctData[i + 1][j][k] - ctData[i - 1][j][k]);
                    // normal vector is initialized
                    normal.setX(x);
                    normal.setY(y);
                    normal.setZ(z);
                    
                    // Unit vector is calculated
                    normal.normalize();
                    // shading is calculated
                    float v = Ip * Kd * normal.dotProduct(LRP);
                    
                    // if shading value is negative than we have to ignore those values as
                    // in that case normal will be in opposite direction to light
                    int value = (int) Math.max(0,v);
                    shadingData[i][j][k] =  value;
                }
            }
        }
    }
	
	private static float get3dInterpolatedValue(float[][][] data,Vector v) throws IndexOutOfBoundsException {

        int xIndex = (int) Math.floor(v.getX());
        int yIndex = (int) Math.floor(v.getY());
        int zIndex = (int) Math.floor(v.getZ());
        
        if(xIndex >=127) {
        	xIndex = 126;
        } else if(xIndex < 0) {
        	xIndex = 0;
        }
        if(yIndex >=127) {
        	yIndex = 126;
        } else if(yIndex < 0) {
        	yIndex = 0;
        }
        if(zIndex >=127) {
        	zIndex = 126;
        } else if(zIndex < 0) {
        	zIndex = 0;
        }

        float f1= data[zIndex][yIndex][xIndex];
        float f2= 0;
        try {
            f2 = data[zIndex][yIndex][xIndex+1];
        } catch (Exception e) {
            /*String x = "%s , %s , %s";
            x=String.format(x,point.getX(),yIndex,zIndex);
            System.out.println(x);*/
        }
        float s1 = oneDInterpolation(f1, f2, v.getX());

        f1= data[zIndex][yIndex+1][xIndex];
        f2= data[zIndex][yIndex+1][xIndex+1];
        float s2 = oneDInterpolation(f1, f2, v.getX());

        try {
        	f1= data[zIndex+1][yIndex][xIndex];
        } catch(Exception e) {
        	f1 = data[zIndex][yIndex][xIndex];
        	f2= data[zIndex][yIndex][xIndex+1];
        }        
        float s3 = oneDInterpolation(f1, f2, v.getX());

        try {
        	f1= data[zIndex+1][yIndex+1][xIndex];
            f2= data[zIndex+1][yIndex+1][xIndex+1];
        } catch(Exception e) {
        	f1= data[zIndex][yIndex+1][xIndex];
            f2= data[zIndex][yIndex+1][xIndex+1];
        }
        
        float s4 = oneDInterpolation(f1, f2, v.getX());


        float s5 = oneDInterpolation(s1,s3,v.getZ());
        float s6 = oneDInterpolation(s2,s4,v.getZ());

        float value = oneDInterpolation(s5, s6, v.getY());

        return value;
    }
	
	/**
     * A method used to find linear interpolationg
     * @param f1 value at first corner point
     * @param f2 value at second corner point
     * @param x
     * @return
     */
    private static float oneDInterpolation(float f1,float f2, float x){
        // find the floor of the x
        // for example if point is 4.5
        // we will get first point x0= 4
        double x0= Math.floor(x);
        // similarly second point will x1=5
        double x1= x0+1; //Math.ceil(x);
        // This formula helps in interpolating value between two points
        return (float) (f1* (x1-x) + f2*(x-x0));
    }

    
    private static void saveImagePNG( final BufferedImage bi){
        try {
            RenderedImage rendImage = bi;
            //ImageIO.write(rendImage, "bmp", new File(path));
            ImageIO.write(rendImage, "PNG", new File(outputFilePath));
            //ImageIO.write(rendImage, "jpeg", new File(path));
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void saveImage(int[][] arr) {
        int xLength = arr.length;
        int yLength = arr[0].length;
        BufferedImage b = new BufferedImage(yLength, xLength, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < xLength; x++) {
            for (int y = 0; y < yLength; y++) {
                int pixel = arr[y][x];
                pixel= pixel%256;
                // a 32 bit integer is created with the pixel value
                int rgb = 0xff000000 |(int) pixel << 16 | (int) pixel << 8 | (int) pixel;
                b.setRGB(x, y, rgb);
            }
        }

        saveImagePNG(b);
    }

}
