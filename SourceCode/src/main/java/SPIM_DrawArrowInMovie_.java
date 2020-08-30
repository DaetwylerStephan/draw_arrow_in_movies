

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.Iterator;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.WindowManager;



public class SPIM_DrawArrowInMovie_ implements PlugIn, KeyListener, MouseListener {
	
	private String writetofilename;
	private String loadfile;
	private String image;
	private ImagePlus imp;
	private ImageCanvas canvas;
	
	private HashMap<Integer, ArrayList<int[]>> pointmap = new HashMap<Integer, ArrayList<int[]>>();
		
	private Overlay overlaytotal = new Overlay();
	private Overlay overlayarrow = new Overlay();
	private BasicStroke stroke;
	private GeneralPath path;
	
	private int[][] interpolatedvalues;
	private ArrowShapeV2 arrow;
	private ArrowOptionPanel2 arrowpanel =  new ArrowOptionPanel2(this);
			
	   
	/**
	 * @param args
	 */
	@Override
	public void run(String args) {
		
		//----------get user input--------------------------------------------------------------------------
		//generate an object of GenericDialog class to which options can be added and which can be evaluated
		final GenericDialogPlus gd = new GenericDialogPlus( "Indicate folder" );
		
		gd.addFileField( "Load movie", "D:\\tutorial\\movie.tif", 50);
		gd.addFileField( "Specify pointfile path (optional)", "D:\\tutorial\\pointfile\\pointfile.txt", 50);
		gd.showDialog();

		if ( gd.wasCanceled())
			return;
		this.image = gd.getNextString();
		this.loadfile = gd.getNextString();
		
		//------Load image data-----------------------------------------------------------------------------
	
		this.imp = new ImagePlus();
		
		if(image.endsWith(".tif")){
			//it either loads .tif file 
			imp = IJ.openImage(image);
			File saveto = new File(image).getParentFile();
			writetofilename = saveto + File.separator + "pointlist";
		}else{
			//or an image sequence of individual tifs
			imp = openStack(image);
			writetofilename = image  + "_pointlist";
		}
		
		//------Display image and bring it to front--------------------------------------------------------
		
		imp.show();
		imp.setTitle(image);
	    WindowManager.getActiveWindow();
	    	
		//-------------set interaction gui listeners to interact with image ---------------------------
		ImageWindow win = imp.getWindow();
		canvas = win.getCanvas();
		canvas.addMouseListener(this);
		win.removeKeyListener(IJ.getInstance());
		canvas.removeKeyListener(IJ.getInstance());
		
		win.addKeyListener(this);
		canvas.addKeyListener(this);
		///ImagePlus.addImageListener(this);
	    	
	    //--------------------add arrowpanel----------------------------------------------------------
	    arrowpanel.setLocationRelativeTo(null);
	    arrowpanel.setVisible(true);
		
	   	}			
			
			

	
	/**
	 * Input: filename, Output: ImagePlus object
	 * Use openStack to open image sequences of .tif files in a folder. If no .tif files are found, an empty ImagePlus stack is returned
	 */
	private static ImagePlus openStack(String directoryname){
		
		//get a list of all files tif files and sort them according to the name
		File directory_file = new File(directoryname);
		String [] loadimagelist = directory_file.list();
		String[] loadimagelist_tif= filter(loadimagelist, "(.*).tif$");
		Arrays.sort(loadimagelist_tif);
		
		//open all the .tif files into an ImageStack
		if(loadimagelist_tif.length>0) {
		ImagePlus init = IJ.openImage(directoryname+File.separator + loadimagelist_tif[0]);
		ImageStack imp = new ImageStack(init.getProcessor().getWidth(), init.getProcessor().getHeight());
		
		for(int i = 0; i< loadimagelist_tif.length;i++){
			ImagePlus tmp = IJ.openImage(directoryname+File.separator + loadimagelist_tif[i]);
			imp.addSlice(tmp.getProcessor());
		}
		
		ImagePlus returnimage = new ImagePlus("stack", imp);
		return returnimage;

		}else {
		IJ.log("folder does not contain .tif files to open. Please open a different folder");	
		ImagePlus emptyimage = new ImagePlus();
		return emptyimage;
		}
	}

	
	/**
	 * run the ImageJ plugin
	 * @param args2
	 */
	public static void main(String[] args2) {
		new ImageJ();
		IJ.getInstance().setAlwaysOnTop(true);
		IJ.runPlugIn("SPIM_DrawArrowInMovie_","");
	}

	    
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}



	/**
	 * if you press the mouse, then draw a yellow circle in the image and add point to pointlist 
	 */
	 public void mousePressed(MouseEvent e) {
		 
		 if(IJ.getToolName() =="rectangle") {
			
		 
		Point2D cursorlocation = canvas.getCursorLoc();
		
		int offscreenX = (int) cursorlocation.getX();
		int offscreenY = (int) cursorlocation.getY();
		System.out.println(offscreenX + " " + offscreenY);
		imp.setOverlay(overlaytotal);	
			//if(!(stackcontainer.getXYZ(offscreenX, offscreenY, imp.getSlice()-1)>0)){
				  
		    path = new GeneralPath();
		    path.moveTo(offscreenX, offscreenY);
		    //newPath = true;
		    stroke = new BasicStroke(10, BasicStroke.CAP_ROUND/*CAP_BUTT*/, BasicStroke.JOIN_ROUND);
			//}
			path.lineTo(offscreenX, offscreenY);
			
			ShapeRoi roi = new ShapeRoi(path);
		    Color c = new Color(1, 1, 0,0.4f);
			roi.setStrokeColor(c);
		    roi.setStroke(stroke);
		    roi.setPosition(imp.getSlice());
		    overlaytotal.add(roi);
		    
			int slicenb = (int) imp.getSlice();
		    int[] pointtoadd = {(int) offscreenX,(int) offscreenY};
		    
		    if(pointmap.get(slicenb)==null){
		    pointmap.put(slicenb, new ArrayList<int[]>());//initialize array if nothing saved under the hashmap key yet
		    pointmap.get(slicenb).add(pointtoadd);
		    }else{
		    pointmap.get(slicenb).add(pointtoadd);
		    }  
		    
		 }
	    }

	 /**
		 * if you press the key "s", "l", "i", "d" or "r", then call the corresponding function 
		 */
	@Override
	public void keyPressed(KeyEvent e) {
		        int keyCode = e.getKeyCode();
		        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
		        
		        if(keyCode == 83){//press key "s"
		        	save_points();  	
		        }
		       
		        if(keyCode == 76){//press key "l"	
		        	loadpointfile();
		        }
		        
		        if(keyCode==73){//press key "i"	
		        	interpolate_values();
		        }
		        
		        if(keyCode==68){//press key "d"
		        	draw_arrowoverlay();
		        }
		        
		        if(keyCode ==82){//press key "r"
		        	deleteoverlay();

		        }        
	}



	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}	
	
	/**
	 * draw arrow overlay into image
	 */
	public void draw_arrowoverlay(){
		
		//get your arrow from the arrowpanel
		this.arrow = arrowpanel.getselectedarrow();
		
		//modify the arrow overlay here
		imp.setOverlay(overlayarrow);
		
		//go over all planes
		for(int i=0; i<interpolatedvalues[0].length;i++){
			
			//get the coordinates
			int planenb =interpolatedvalues[2][i];
			
			
			///-----------define start and endpoint of arrow based on angle + correction
			double angle = arrow.getAngle();
			int width = (int) arrowpanel.getwidth();
			//convert angle to radians
			angle = Math.toRadians(angle);
			
			//add correction to correct for width/thickness
			int correctionx = (int) (Math.cos(angle)*(width+1)) - (int) arrowpanel.getCorrectionX();
			int correctiony= (int) (Math.sin(angle)*width) - (int) arrowpanel.getCorrectionY();
			//System.out.println(correctionx + " " + correctiony);

			double length = arrow.getLength();
			Point2D endpoint = new Point2D.Float(interpolatedvalues[0][i]-correctionx, interpolatedvalues[1][i]-correctiony);
			Point2D startpoint =new Point2D.Float();
			float xvalue =(float) (endpoint.getX()-(Math.cos(angle)*length));
			float yvalue = (float) (endpoint.getY()-(Math.sin(angle)*length));
			startpoint.setLocation(xvalue,yvalue);
			
			arrow.setStartPoint(startpoint);
			arrow.setEndPoint(endpoint);
						
			///-----------define overlay roi
	
			stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			ArrowShapeRoi roi = new ArrowShapeRoi(arrow, stroke);
			roi.setPosition(planenb);
			roi.setStrokeColor(arrow.getColor());
			
			if(arrow.getfill()){
				roi.setFillColor(arrow.getColor());
			}
			
			//generate unique identifier for this overlay so that it can be retrieved later
			int numberofdigits = (int) Math.floor(Math.log10(interpolatedvalues[0].length))+2;
			String roiname = "roi" + IJ.pad(planenb, numberofdigits);
			
			overlayarrow.add(roi, roiname);
			
			imp.updateAndDraw(); 			
		}
	}
	
	/**
	 * take list of points, interpolate them and save points to a .txt file 
	 */
	public void save_points(){
		System.out.println(Arrays.toString(interpolatedvalues[0]));
		if(!pointmap.isEmpty()) {
		printhashmap(pointmap);
		InterpolateHashMap interpolateit = new InterpolateHashMap();
		this.interpolatedvalues = interpolateit.interpolateList(pointmap);
		writepointfile();} 
		else {
			IJ.log("no points are selected to save");
		}
		
	}
	
	/**
	 * flatten image. Note imp.flattenStack() produced some strange artefact - therefore, flatten frame by frame
	 */
	public void flattenimage(){
		imp.setAntialiasRendering(true);
		Overlay alloverlays = imp.getOverlay();
		int numberofdigits = (int) Math.floor(Math.log10(interpolatedvalues[0].length))+2;
		
		//convert stack to RGB 
		if (imp.getBitDepth()!=24) {
			new ImageConverter(imp).convertToRGB();
		}
				
		for(int planeiter =0; planeiter < interpolatedvalues[0].length;planeiter++) {
		
			//get the coordinates
			int planenb =interpolatedvalues[2][planeiter];
		
			//retrieve unique identifier for overlay. This identifier was generated when the arrow overlay was added first.
			String roiname = "roi" + IJ.pad(planenb, numberofdigits);
			
			ImagePlus currentimage = new ImagePlus("currentimage", imp.getStack().getProcessor(planenb));
			int currentindex = alloverlays.getIndex(roiname);
			Overlay currentoverlay = new Overlay();
			currentoverlay.add(alloverlays.get(currentindex));
			currentimage.setOverlay(currentoverlay);
			
			ImagePlus newimage = currentimage.flatten();
						
			imp.getStack().setProcessor(newimage.getProcessor(), planenb);
		}	
		}

	/**
	 * delete arrow overlay in image 
	 */
	public void deleteoverlay(){
		overlayarrow.clear();
		imp.updateAndDraw(); 
	}
	
	/**
	 * delete point overlay in image 
	 */
	public void deletepoints(){
		overlaytotal.clear();
		pointmap.clear();
		imp.updateAndDraw(); 
	}
	
	/**
	 * take the manually annotated points and interpolate them 
	 */
	public void interpolate_values(){
		
		InterpolateHashMap interpolateit = new InterpolateHashMap();
    	this.interpolatedvalues = interpolateit.interpolateList(pointmap);
    	
    	imp.setOverlay(overlaytotal);
    	overlaytotal.clear();
    	
    	//add a basic stroke to every interpolated position. visualize new interpolated values
  	  	for(int i=0;i<interpolatedvalues[0].length;i++){
	    path = new GeneralPath();
	    path.moveTo(interpolatedvalues[0][i], interpolatedvalues[1][i]);
	   	stroke = new BasicStroke(10, BasicStroke.CAP_ROUND/*CAP_BUTT*/, BasicStroke.JOIN_ROUND);
		path.lineTo(interpolatedvalues[0][i], interpolatedvalues[1][i]);
	   	
		ShapeRoi roi = new ShapeRoi(path);
	    Color c = new Color(1, (float) 0.5, 0,0.4f);
		roi.setStrokeColor(c);
	    roi.setStroke(stroke);
	    roi.setPosition(interpolatedvalues[2][i]);
	    overlaytotal.add(roi);
  	  	}		  	
  	  printhashmap(pointmap);
	}
	
	/**
	 * print hashmap 
	 */
	private void printhashmap(HashMap pointmap){
		 Set set = pointmap.entrySet();
         Iterator iterator = set.iterator();
         while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
            ArrayList<int[]> tmp = (ArrayList<int[]>) mentry.getValue();
            for(int i=0;i<tmp.size();i++){
            System.out.println(Arrays.toString(tmp.get(i)) + " ");
         }
         }
	}
	
	/**
	 * write the list of points to the folder specified at the startup 
	 */
	 private void writepointfile() {			
			try{
				//System.out.println(writetofilename);			
				File registerfile = new File(writetofilename);

				int filenb =-1;
				if(registerfile.exists()){filenb = registerfile.list().length;}
				else{registerfile.mkdirs();}
				String filename = writetofilename + File.separator+ "pointfile" + IJ.pad(filenb+1, 3) + ".txt";
				FileWriter writer = new FileWriter(filename);
				System.out.println(filename);
				for(int i_points=0;i_points<interpolatedvalues[0].length;i_points++){
					for(int i=0;i<interpolatedvalues.length;i++){
					writer.write(interpolatedvalues[i][i_points] + ",");
					}
					writer.write(System.lineSeparator());
				}
				writer.close();
				IJ.log("File saved to: " + filename);
			}catch(IOException ex){
			IJ.log("Writing registration file did not work.");	
			}
		}
	 
	 /**
	  * loads a list of points from a .txt file to the interpolatedvalues array
	 */
	 public void loadpointfile(){
		
		     File file = new File(loadfile); //for ex foo.txt
		     ArrayList<int[]> intpos = new ArrayList<int[]>();
		     
		     try {
				FileReader fileReader = new FileReader(file);
				BufferedReader reader = new BufferedReader(fileReader);
				
				String line =null;				
				
				//read in lines of text file and transform them to Integers
				while((line = reader.readLine())!=null){
					String[] currentpos_string = line.split(",");
					int[] currentpos = new int[currentpos_string.length];
					for(int i=0;i<currentpos_string.length;i++){
						currentpos[i]= Integer.parseInt(currentpos_string[i]);
					}
					intpos.add(currentpos);	
				}
				reader.close();
				
				interpolatedvalues = new int[3][intpos.size()];
				
				System.out.println(intpos.size());
				
				for(int i=0;i<intpos.size();i++){
					interpolatedvalues[0][i] = intpos.get(i)[0];
					interpolatedvalues[1][i] = intpos.get(i)[1];
					interpolatedvalues[2][i] = intpos.get(i)[2];
				}
				
				for(int i=0;i<interpolatedvalues[0].length;i++) {
					int slicenb = interpolatedvalues[2][i];
				    int[] pointtoadd = {(int) interpolatedvalues[0][i],(int) interpolatedvalues[1][i]};
				    if(pointmap.get(slicenb)==null){
					    pointmap.put(slicenb, new ArrayList<int[]>());//initialize array if nothing saved under the hashmap key yet
					    pointmap.get(slicenb).add(pointtoadd);
					    }else{
					    pointmap.get(slicenb).add(pointtoadd);
					}
				}
				
		     } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		     } catch (IOException e) {
					e.printStackTrace();
			}		
	 }
	 
	 
	/**
	 * INNER CLASS from Arrow Tools plugin by Jean-Yves Tinevez, defines the arrow shape drawn with anti-aliased drawing
	*/
		private static class ArrowShapeRoi extends ShapeRoi {
			private static final long serialVersionUID = 1L;
			private ArrowShapeV2 arrow;
			private BasicStroke stroke;
			public ArrowShapeRoi(ArrowShapeV2 _arrow, BasicStroke _stroke) {
				super(_arrow);
				Shape out_lineshape = _stroke.createStrokedShape(_arrow);
				this.or(new ShapeRoi(out_lineshape));
				arrow = _arrow;
				stroke = _stroke;
			}
			/**
			 * Overrides the {@link ShapeRoi#draw(Graphics)} of ShapeRoi so that we can have
			 * anti-aliased drawing.
			 */
			public void draw(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				super.draw(g2);
			}
			public ArrowShapeV2 getArrow() { return arrow; }
			public BasicStroke getStroke() { return stroke; }
		}
	 
	 /**
		 * @param in - a list of strings (filenames)
		 * @param pattern - a regex pattern to filter the list of strings
		 * @return String[] / filtered list
		 */
		private static String[] filter(String[] in, String pattern) {
			ArrayList<String> all = new ArrayList<String>(in.length);
			for(String s : in)
				if(s.matches(pattern))
					all.add(s);
			String[] out = new String[all.size()];
			all.toArray(out);
			return out;
		}
		
}
