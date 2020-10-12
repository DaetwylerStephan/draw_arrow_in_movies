import ij.plugin.Colors;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


public class ArrowOptionPanel2 extends javax.swing.JFrame {

	private static final long serialVersionUID = 1;
    /** A list of event listeners for this component. */
    private  EventListenerList listenerList = new EventListenerList();
    private boolean firingActionEvent = false;

	private JPanel jPanelMain;
	private JLabel jLabelArrowThickness;
	private JSlider jSliderArrowThickness;
	private JLabel jLabelArrowAngle;
	private JSlider jSliderArrowAngle;
	private JTextField jTextFieldArrowAngle;
	private Canvas canvasDrawingArea;
	private JPanel jPanelDrawArea;
	private JCheckBox jCheckBoxFillArrow;
	private JTextField jTextFieldArrowThickness;
	private JLabel jLabelArrowLength;
	private JTextField jTextFieldHeadLength;
	private JSlider jSliderHeadLength;
	private JLabel jLabelAllArrowLength;
	private JTextField jTextFieldAllArrowLength;
	private JSlider jSliderAllArrowLength;
	private JComboBox jComboBoxHeadStyle;
	private JLabel jLabelHeadStyle;
	private JComboBox jComboBoxColorStyle;
	private JLabel jLabelColorStyle;
	private JLabel jLabelPluginDescription;
	private JTextField jTextFieldArrowCorrectionX;
	private JTextField jTextFieldArrowCorrectionY;
	private JSlider jSliderAllArrowCorrectionX;
	private JSlider jSliderAllArrowCorrectionY;
	private JButton interpolate_button;
	private JButton drawarrow_button;
	private JButton delete_arrow_button;
	private JButton loadpointlistbutton;
	private JButton savepointlistbutton;
	private JButton deletepoints_button;
	private JButton help_button;
	private JButton addoverlay_button;
	private JButton open_newmovie_button;
	private SPIM_DrawArrowInMovie_ spim_DrawArrowInMovie_parent;

	
	private static BasicStroke stroke;
	private static ArrowShapeV2 arrow;
	
	/*
	 * INNER CLASSES
	 */
	
	/**
	 * Canvas that will draw an example arrow as specified by the GUI of this frame. 
	 */
	private class ArrowExampleCanvas extends Canvas {
		private static final long serialVersionUID = 1L;
		Point2D start, end;
		public void paint(Graphics g) {
			final double arrowlength = Double.parseDouble(jTextFieldAllArrowLength.getText());
			final double arrowheadlength = Double.parseDouble(jTextFieldHeadLength.getText());
		
			final double maxlength = Double.max(arrowlength, arrowheadlength);
			super.paint(g);
			start 	= new Point2D.Double(jPanelDrawArea.getWidth()*0.5-maxlength/2, jPanelDrawArea.getHeight()/2.0);
			end 	= new Point2D.Double(jPanelDrawArea.getWidth()*0.5+maxlength/2, jPanelDrawArea.getHeight()/2.0);
			arrow = new ArrowShapeV2((ArrowShapeV2.ArrowStyle2) jComboBoxHeadStyle.getSelectedItem());
			arrow.setStartPoint(start);
			arrow.setEndPoint(end);
			try {
				final double headlength = Double.parseDouble(jTextFieldHeadLength.getText());
				final float width = Float.parseFloat(jTextFieldArrowThickness.getText());
				arrow.setLength(arrowlength);
				arrow.setHeadLength(headlength);
				stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
				Graphics2D g2 = (Graphics2D) g;
				Color selectedcolor = Colors.getColor((String) jComboBoxColorStyle.getSelectedItem(), Color.white);
				g2.setColor(selectedcolor);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				        RenderingHints.VALUE_ANTIALIAS_ON);
				Shape shape = stroke.createStrokedShape(arrow);
				Area area = new Area(shape); // this will get us the thick outline
				area.add(new Area(arrow)); // to fill inside
				g2.draw(area);
				if (jCheckBoxFillArrow.isSelected()) {	g2.fill(area);		}
				// Fire a property change
				fireActionEvent();
			} catch (NumberFormatException nfe) { }
		}
		
	}
	
	/**
	 * Change listener that will listen to change in slider value and will set the corresponding {@link JTextField}
	 * value accordingly
	 */
	private class SliderChangeListener implements ChangeListener {
		private JTextField text_field;
		public SliderChangeListener(JTextField _text_field) {
			this.text_field = _text_field;
		}
		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();
			text_field.setText(String.format("%d", slider.getValue()) );
			canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
		}
	}
	
	
	
	/**
	 * ActionListener that will listen to change in text field value and set the corresponding {@link JSlider} 
	 * value accordingly
	 */
	private class TextFieldActionListener implements ActionListener {
		private JSlider slider;
		public TextFieldActionListener(JSlider _slider) {
			this.slider = _slider;
		}
		public void actionPerformed(ActionEvent e) {
			JTextField text_field = (JTextField) e.getSource();
			try {
				final double val = Double.parseDouble(text_field.getText());
				slider.setValue( (int) val);
			} catch (NumberFormatException nfe) {
				text_field.setText(String.format("%d", slider.getValue()));
			}
			canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
		}
		
	}
	
	/**
	 * ActionListener that will listen to change in buttons and call the corresponding functions
	 */
	
	private class ButtonActionListener implements ActionListener {
		private JButton button_field;
		//constructor
		public ButtonActionListener(JButton button_field) {
			this.button_field = button_field;
		}
		
		// define what happens if you press buttons:
		//"Help"  "Save points (s)" "Interpolate (i)" "Draw arrow (d)" "Remove arrows (r)" "Delete points" "Load points (l)"

		public void actionPerformed(ActionEvent ae) {
			String choice = ae.getActionCommand();
		      if (choice.equals("Help")) {
		    	  String plugin_explanation ="<html>How to use the plugin? "
			    	    + "<br/> Check out the tutorial video and paper for a detailed description of how to use this plugin. In short:"
			    	  	+ "<br/> 1. Click into the image to select process of interest in selected frames."
		    	  		+ "<br/> 2. Press button or \"i\" for interpolation, refine where needed by further clicking "
		    	  		+ "<br/> 3. Design arrow and draw it with \"d\", or \"r\" to remove them again to draw another arrow. "
		    	  		+ "<br/> 4. Press \"s\" to save the point list of the current selection and press \"l\" to load a previous selection."
		    	  		+ "<br/> 5. Press \"Permanently add overlay\" to convert your stack to RGB and permanently add your arrows to the movie."
		    	  		+ "<br/> 6. Use Fiji's \"Save As...\" functionality to save your movie with the arrows in your favorite file format"
		    	  		+ "<br/> Note: This plugin does not work on multi-channel hyperstacks. Change type to RGB beforehand.\"</html>";
		          JOptionPane.showMessageDialog(null, plugin_explanation, "Help", JOptionPane.PLAIN_MESSAGE);
		      }
		      else if(choice.equals("Save points (s)")) {
		    	  spim_DrawArrowInMovie_parent.save_points();
		    	 }
		      else if(choice.equals("Interpolate (i)")) {
		    	  spim_DrawArrowInMovie_parent.interpolate_values();
		      }
		      else if(choice.equals("Draw arrows (d)")) {
	    		  spim_DrawArrowInMovie_parent.draw_arrowoverlay();
		      }
		      else if(choice.equals("Remove arrows (r)")) {
		    	  spim_DrawArrowInMovie_parent.deleteoverlay();
		      }
		      else if(choice.equals("Remove points")) {
		    	  int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all labels?", "Delete points",
		    		      JOptionPane.YES_NO_OPTION,
		    		      JOptionPane.PLAIN_MESSAGE);
		    	  if(res == 0) {
		    		  spim_DrawArrowInMovie_parent.deletepoints();
		    		         System.out.println("Pressed YES");
		    	   } else if (res == 1) {
		    		         System.out.println("Pressed NO");
		    	    }
		      }
		      else if(choice.equals("Load points (l)")) {
	    		  spim_DrawArrowInMovie_parent.loadpointfile();
		      }
		      else if(choice.equals("Permanently add overlay")) {
	    		  spim_DrawArrowInMovie_parent.flattenimage();
		      }
		      else if(choice.equals("Open new movie")){
		    	  int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to open a new movie?", "Open new movie...",
		    		      JOptionPane.YES_NO_OPTION,
		    		      JOptionPane.PLAIN_MESSAGE);
		    	  if(res == 0) {
		    		  spim_DrawArrowInMovie_parent.openMovie();
		    		         System.out.println("Pressed YES");
		    	   } else if (res == 1) {
		    		         System.out.println("Pressed NO");
		    	    }
		      }

		}

	
	}
	
	
	/**
	 * MouseWheellistener that will listen to mouse scroll over a slider and update the slider value
	 * accordingly.
	 */
	private class SliderMouseWheelListener implements MouseWheelListener {
		private JSlider slider;
		public SliderMouseWheelListener(JSlider _slider) {
			this.slider = _slider;
		}
				public void mouseWheelMoved(MouseWheelEvent e) {
			int steps = e.getWheelRotation();
			slider.setValue(slider.getValue()+steps);
		}
	}
		
	/*
	 * CONSTRUCTOR
	 */
	
	/**
	 * Instantiates the config panel with using settings from arguments. 
	 */
	public ArrowOptionPanel2(ArrowShapeV2 _arrow, BasicStroke _stroke) {
		super();
		initGUI();
		stroke = _stroke;
		arrow = _arrow;
		jComboBoxHeadStyle.setSelectedItem(arrow.getStyle());
		jTextFieldArrowThickness.setText(String.format("%.0f", stroke.getLineWidth()));
		jSliderArrowThickness.setValue((int) stroke.getLineWidth());
		jTextFieldHeadLength.setText(String.format("%.0f", arrow.getLength()));
		jSliderHeadLength.setValue((int) arrow.getLength() );
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			} 
		};
		jComboBoxHeadStyle.addActionListener(al);
		jComboBoxColorStyle.addActionListener(al);
		jTextFieldArrowThickness.addActionListener(new TextFieldActionListener(jSliderArrowThickness));
		jTextFieldArrowAngle.addActionListener(new TextFieldActionListener(jSliderArrowAngle));
		jTextFieldHeadLength.addActionListener(new TextFieldActionListener(jSliderHeadLength));
		jTextFieldAllArrowLength.addActionListener(new TextFieldActionListener(jSliderAllArrowLength));
		jSliderArrowThickness.addChangeListener(new SliderChangeListener(jTextFieldArrowThickness));
		jSliderArrowThickness.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowThickness));
		jSliderArrowAngle.addChangeListener(new SliderChangeListener(jTextFieldArrowAngle));
		jSliderArrowAngle.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowAngle));
		jSliderHeadLength.addChangeListener(new SliderChangeListener(jTextFieldHeadLength));
		jSliderAllArrowLength.addChangeListener(new SliderChangeListener(jTextFieldAllArrowLength));
		jSliderAllArrowLength.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowLength));
		jTextFieldArrowCorrectionX.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionX));
		jTextFieldArrowCorrectionY.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionY));
		jSliderAllArrowCorrectionX.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionX));
		jSliderAllArrowCorrectionX.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionX));
		jSliderAllArrowCorrectionY.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionY));
		jSliderAllArrowCorrectionY.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionY));	
		jCheckBoxFillArrow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			}
		});
		help_button.addActionListener(new ButtonActionListener(help_button));
		drawarrow_button.addActionListener(new ButtonActionListener(drawarrow_button));
		delete_arrow_button.addActionListener(new ButtonActionListener(delete_arrow_button));
		loadpointlistbutton.addActionListener(new ButtonActionListener(loadpointlistbutton));
		savepointlistbutton.addActionListener(new ButtonActionListener(savepointlistbutton));
		deletepoints_button.addActionListener(new ButtonActionListener(deletepoints_button));
		interpolate_button.addActionListener(new ButtonActionListener(interpolate_button));
		addoverlay_button.addActionListener(new ButtonActionListener(addoverlay_button));
		open_newmovie_button.addActionListener(new ButtonActionListener(open_newmovie_button));


	}
	
	/**
	 * Default constructor
	 * 	 */
	public ArrowOptionPanel2() {
		super();
		initGUI();

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			} 
		};
		jComboBoxHeadStyle.addActionListener(al);
		jComboBoxColorStyle.addActionListener(al);
		jTextFieldArrowThickness.addActionListener(new TextFieldActionListener(jSliderArrowThickness));
		jTextFieldArrowAngle.addActionListener(new TextFieldActionListener(jSliderArrowAngle));
		jTextFieldHeadLength.addActionListener(new TextFieldActionListener(jSliderHeadLength));
		jTextFieldAllArrowLength.addActionListener(new TextFieldActionListener(jSliderAllArrowLength));
		jSliderArrowThickness.addChangeListener(new SliderChangeListener(jTextFieldArrowThickness));
		jSliderArrowThickness.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowThickness));
		jSliderArrowAngle.addChangeListener(new SliderChangeListener(jTextFieldArrowAngle));
		jSliderArrowAngle.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowAngle));
		jSliderHeadLength.addChangeListener(new SliderChangeListener(jTextFieldHeadLength));
		jSliderHeadLength.addMouseWheelListener(new SliderMouseWheelListener(jSliderHeadLength));
		jSliderAllArrowLength.addChangeListener(new SliderChangeListener(jTextFieldAllArrowLength));
		jSliderAllArrowLength.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowLength));
		jTextFieldArrowCorrectionX.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionX));
		jTextFieldArrowCorrectionY.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionY));
		jSliderAllArrowCorrectionX.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionX));
		jSliderAllArrowCorrectionX.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionX));
		jSliderAllArrowCorrectionY.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionY));
		jSliderAllArrowCorrectionY.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionY));
		jCheckBoxFillArrow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			}
		});
		help_button.addActionListener(new ButtonActionListener(help_button));
		drawarrow_button.addActionListener(new ButtonActionListener(drawarrow_button));
		delete_arrow_button.addActionListener(new ButtonActionListener(delete_arrow_button));
		loadpointlistbutton.addActionListener(new ButtonActionListener(loadpointlistbutton));
		savepointlistbutton.addActionListener(new ButtonActionListener(savepointlistbutton));
		deletepoints_button.addActionListener(new ButtonActionListener(deletepoints_button));
		interpolate_button.addActionListener(new ButtonActionListener(interpolate_button));
		addoverlay_button.addActionListener(new ButtonActionListener(addoverlay_button));
		open_newmovie_button.addActionListener(new ButtonActionListener(open_newmovie_button));

	}
	
	
	/**
	 * Default constructor
	 * @param spim_DrawArrowInMovie_ 
	 * 	 */
	public ArrowOptionPanel2(SPIM_DrawArrowInMovie_ spim_DrawArrowInMovie_) {
		super();
		this.spim_DrawArrowInMovie_parent = spim_DrawArrowInMovie_;
		initGUI();

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			} 
		};
		jComboBoxHeadStyle.addActionListener(al);
		jComboBoxColorStyle.addActionListener(al);
		jTextFieldArrowThickness.addActionListener(new TextFieldActionListener(jSliderArrowThickness));
		jTextFieldArrowAngle.addActionListener(new TextFieldActionListener(jSliderArrowAngle));
		jTextFieldHeadLength.addActionListener(new TextFieldActionListener(jSliderHeadLength));
		jTextFieldAllArrowLength.addActionListener(new TextFieldActionListener(jSliderAllArrowLength));
		jSliderArrowThickness.addChangeListener(new SliderChangeListener(jTextFieldArrowThickness));
		jSliderArrowThickness.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowThickness));
		jSliderArrowAngle.addChangeListener(new SliderChangeListener(jTextFieldArrowAngle));
		jSliderArrowAngle.addMouseWheelListener(new SliderMouseWheelListener(jSliderArrowAngle));
		jSliderHeadLength.addChangeListener(new SliderChangeListener(jTextFieldHeadLength));
		jSliderHeadLength.addMouseWheelListener(new SliderMouseWheelListener(jSliderHeadLength));
		jSliderAllArrowLength.addChangeListener(new SliderChangeListener(jTextFieldAllArrowLength));
		jSliderAllArrowLength.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowLength));
		jTextFieldArrowCorrectionX.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionX));
		jTextFieldArrowCorrectionY.addActionListener(new TextFieldActionListener(jSliderAllArrowCorrectionY));
		jSliderAllArrowCorrectionX.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionX));
		jSliderAllArrowCorrectionX.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionX));
		jSliderAllArrowCorrectionY.addChangeListener(new SliderChangeListener(jTextFieldArrowCorrectionY));
		jSliderAllArrowCorrectionY.addMouseWheelListener(new SliderMouseWheelListener(jSliderAllArrowCorrectionY));
		jCheckBoxFillArrow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvasDrawingArea.paint(canvasDrawingArea.getGraphics());
			}
		});
		help_button.addActionListener(new ButtonActionListener(help_button));
		drawarrow_button.addActionListener(new ButtonActionListener(drawarrow_button));
		delete_arrow_button.addActionListener(new ButtonActionListener(delete_arrow_button));
		loadpointlistbutton.addActionListener(new ButtonActionListener(loadpointlistbutton));
		savepointlistbutton.addActionListener(new ButtonActionListener(savepointlistbutton));
		deletepoints_button.addActionListener(new ButtonActionListener(deletepoints_button));
		interpolate_button.addActionListener(new ButtonActionListener(interpolate_button));
		addoverlay_button.addActionListener(new ButtonActionListener(addoverlay_button));
		open_newmovie_button.addActionListener(new ButtonActionListener(open_newmovie_button));
		
	}
	
	/*
	 * PUBLIC METHODS
	 */
	

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ArrowOptionPanel2 inst = new ArrowOptionPanel2();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public ArrowShapeV2 getselectedarrow(){
		double arrowlength = Double.parseDouble(jTextFieldAllArrowLength.getText());
		double arrowheadlength = Double.parseDouble(jTextFieldHeadLength.getText());
		arrow.setLength(arrowlength);
		arrow.setHeadLength(arrowheadlength);
		arrow.setAngle(Double.parseDouble(jTextFieldArrowAngle.getText()));
		arrow.setfill(jCheckBoxFillArrow.isSelected());
		Color selectedcolor = Colors.getColor((String) jComboBoxColorStyle.getSelectedItem(), Color.white);
		arrow.setColor(selectedcolor);
		return arrow;
		
	}
	
	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class,l);
	}
	
    /** Removes an <code>ActionListener</code>.
    *
    * @param l  the <code>ActionListener</code> to remove
    */
   public void removeActionListener(ActionListener l) {
	    listenerList.remove(ActionListener.class, l);
   }

   /**
    * Returns an array of all the <code>ActionListener</code>s added
    * to this JComboBox with addActionListener().
    *
    * @return all of the <code>ActionListener</code>s added or an empty
    *         array if no listeners have been added
    * @since 1.4
    */
   public ActionListener[] getActionListeners() {
       return (ActionListener[])listenerList.getListeners(
               ActionListener.class);
   }
   
  
   /*
    * get parameters from this panel
    */
   
   public BasicStroke getStroke() {	   return stroke;   }
   public double getLength() { return arrow.getLength(); }
   public ArrowShapeV2.ArrowStyle2 getStyle() { return arrow.getStyle(); }   
   public double getHeadLength() {return arrow.getHeadLength();}
   public double getCorrectionX() {return Double.parseDouble(jTextFieldArrowCorrectionX.getText());}
   public double getCorrectionY() {return Double.parseDouble(jTextFieldArrowCorrectionY.getText());}
   public double getwidth() {return Double.parseDouble(jTextFieldArrowThickness.getText());}

	/*
	 * define the GUI and positions of panels and buttons in it
	 */
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			BorderLayout thisLayout = new BorderLayout();
			getContentPane().setLayout(thisLayout);
			this.setTitle("Arrow options");
			this.setResizable(false);
			{
			jPanelMain = new JPanel();
				getContentPane().add(jPanelMain, BorderLayout.CENTER);
				jPanelMain.setLayout(null);
				jPanelMain.setFont(new java.awt.Font("Dialog",0,10));
				jPanelMain.setPreferredSize(new java.awt.Dimension(353, 205));
				//arrow style -text 
				{
					jLabelHeadStyle = new JLabel();
					jPanelMain.add(jLabelHeadStyle);
					jLabelHeadStyle.setText("Arrow head style");
					jLabelHeadStyle.setBounds(10, 15, 95, 15);
					jLabelHeadStyle.setFont(new java.awt.Font("Arial",0,10));
				}
				
				{
					ComboBoxModel jComboBoxHeadStyleModel = 
						new DefaultComboBoxModel( ArrowShapeV2.ArrowStyle2.values() );
					jComboBoxHeadStyle = new JComboBox();
					jPanelMain.add(jComboBoxHeadStyle);
					jComboBoxHeadStyle.setModel(jComboBoxHeadStyleModel);
					jComboBoxHeadStyle.setBounds(112, 11, 146, 24);
					jComboBoxHeadStyle.setFont(new java.awt.Font("Arial",0,10));
				}
				///arrow fill Arrow
				{
					jCheckBoxFillArrow = new JCheckBox();
					jPanelMain.add(jCheckBoxFillArrow);
					jCheckBoxFillArrow.setText("Fill");
					jCheckBoxFillArrow.setBounds(8, 41, 51, 23);
					jCheckBoxFillArrow.setFont(new java.awt.Font("Arial",0,10));
				}
				//colormodel
				{
					jLabelColorStyle = new JLabel();
					jPanelMain.add(jLabelColorStyle);
					jLabelColorStyle.setText("Color");
					jLabelColorStyle.setBounds(70, 45, 95, 15);
					jLabelColorStyle.setFont(new java.awt.Font("Arial",0,10));
				}
				
				
				{
					ComboBoxModel jComboBoxColorStyleModel = 
						new DefaultComboBoxModel(Colors.colors);
					jComboBoxColorStyle = new JComboBox();
					jPanelMain.add(jComboBoxColorStyle);
					jComboBoxColorStyle.setModel(jComboBoxColorStyleModel);
					jComboBoxColorStyle.setBounds(112, 40, 146, 24);
					jComboBoxColorStyle.setFont(new java.awt.Font("Arial",0,10));
					jComboBoxColorStyle.setMaximumRowCount(6);

				}
			
				////arrow head length
				{
					jLabelArrowLength = new JLabel();
					jPanelMain.add(jLabelArrowLength);
					jLabelArrowLength.setText("Arrow head length");
					jLabelArrowLength.setBounds(10, 75, 94, 16);
					jLabelArrowLength.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jSliderHeadLength = new JSlider();
					jPanelMain.add(jSliderHeadLength);
					jSliderHeadLength.setBounds(107, 75, 115, 19);
					jSliderHeadLength.setFont(new java.awt.Font("Arial",0,10));
					jSliderHeadLength.setMinorTickSpacing(1);
					jSliderHeadLength.setMinimum(0);
					jSliderHeadLength.setMaximum(100);
					jSliderHeadLength.setValue(10);

				}
				{
					jTextFieldHeadLength = new JTextField();
					jPanelMain.add(jTextFieldHeadLength);
					jTextFieldHeadLength.setText("10");
					jTextFieldHeadLength.setBounds(225, 75, 30, 20);
					jTextFieldHeadLength.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldHeadLength.setBackground(new java.awt.Color(238,238,238));
				}
				
				///arrow length
				{
					jLabelAllArrowLength = new JLabel();
					jPanelMain.add(jLabelAllArrowLength);
					jLabelAllArrowLength.setText("Arrow length");
					jLabelAllArrowLength.setBounds(10, 100, 94, 16);
					jLabelAllArrowLength.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jTextFieldAllArrowLength = new JTextField();
					jPanelMain.add(jTextFieldAllArrowLength);
					jTextFieldAllArrowLength.setText("10");
					jTextFieldAllArrowLength.setBounds(225, 100, 30, 20);
					jTextFieldAllArrowLength.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldAllArrowLength.setBackground(new java.awt.Color(238,238,238));
				}
				{
					jSliderAllArrowLength = new JSlider();
					jPanelMain.add(jSliderAllArrowLength);
					jSliderAllArrowLength.setBounds(107, 100, 115, 19);
					jSliderAllArrowLength.setFont(new java.awt.Font("Arial",0,10));
					jSliderAllArrowLength.setMinorTickSpacing(1);
					jSliderAllArrowLength.setMinimum(0);
					jSliderAllArrowLength.setMaximum(100);
					jSliderAllArrowLength.setValue(10);

				}
				
				///arrow thickness
				{
					jLabelArrowThickness = new JLabel();
					jPanelMain.add(jLabelArrowThickness);
					jLabelArrowThickness.setText("Arrow thickness");
					jLabelArrowThickness.setBounds(10, 125, 102, 16);
					jLabelArrowThickness.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jTextFieldArrowThickness = new JTextField(); 
					jPanelMain.add(jTextFieldArrowThickness);
					jTextFieldArrowThickness.setText("1");
					jTextFieldArrowThickness.setBounds(225, 125, 30, 20);
					jTextFieldArrowThickness.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldArrowThickness.setBackground(new java.awt.Color(238,238,238));
				}
				{
					jSliderArrowThickness = new JSlider();
					jPanelMain.add(jSliderArrowThickness);
					jSliderArrowThickness.setBounds(110, 125, 115, 20);
					jSliderArrowThickness.setMinimum(1);
					jSliderArrowThickness.setMaximum(20);
					jSliderArrowThickness.setMinorTickSpacing(1);
					jSliderArrowThickness.setValue(1);
				}
				
				//arrow preview text
				{
					JLabel arrowpreviewtext = new JLabel();
					jPanelMain.add(arrowpreviewtext);
					arrowpreviewtext.setText("Arrow preview");
					arrowpreviewtext.setBounds(100, 150, 102, 12);
					arrowpreviewtext.setFont(new java.awt.Font("Arial",0,10));
				}
				//draw area
				{
					jPanelDrawArea = new JPanel();
					BorderLayout jPanelDrawAreaLayout = new BorderLayout();
					jPanelDrawArea.setLayout(jPanelDrawAreaLayout);
					jPanelMain.add(jPanelDrawArea);
					jPanelDrawArea.setBounds(20, 165, 240, 45);
					jPanelDrawArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
					jPanelDrawArea.setBackground(Color.BLACK);
					{
						canvasDrawingArea = new ArrowExampleCanvas();
						jPanelDrawArea.add(canvasDrawingArea, BorderLayout.CENTER);
						canvasDrawingArea.setPreferredSize(new java.awt.Dimension(231, 24));
					}
				}
				
				///arrow angle
				{
					jLabelArrowAngle = new JLabel();
					jPanelMain.add(jLabelArrowAngle);
					jLabelArrowAngle.setText("Arrow angle");
					jLabelArrowAngle.setBounds(10, 225, 102, 16);
					jLabelArrowAngle.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jTextFieldArrowAngle = new JTextField(); 
					jPanelMain.add(jTextFieldArrowAngle);
					jTextFieldArrowAngle.setText("0");
					jTextFieldArrowAngle.setBounds(225, 225, 30, 20);
					jTextFieldArrowAngle.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldArrowAngle.setBackground(new java.awt.Color(238,238,238));
				}
				{
					jSliderArrowAngle = new JSlider();
					jPanelMain.add(jSliderArrowAngle);
					jSliderArrowAngle.setBounds(110, 225, 115, 20);
					jSliderArrowAngle.setMinimum(0);
					jSliderArrowAngle.setMaximum(360);
					jSliderArrowAngle.setMinorTickSpacing(1);
					jSliderArrowAngle.setValue(0);
				}
				
				///arrow correctionx
				{
					JLabel jLabelArrowCorrectionX = new JLabel();
					jPanelMain.add(jLabelArrowCorrectionX);
					jLabelArrowCorrectionX.setText("Move arrow by x");
					jLabelArrowCorrectionX.setBounds(10, 245, 102, 16);
					jLabelArrowCorrectionX.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jTextFieldArrowCorrectionX = new JTextField(); 
					jPanelMain.add(jTextFieldArrowCorrectionX);
					jTextFieldArrowCorrectionX.setText("0");
					jTextFieldArrowCorrectionX.setBounds(225, 245, 30, 20);
					jTextFieldArrowCorrectionX.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldArrowCorrectionX.setBackground(new java.awt.Color(238,238,238));
				}
				{
					jSliderAllArrowCorrectionX = new JSlider();
					jPanelMain.add(jSliderAllArrowCorrectionX);
					jSliderAllArrowCorrectionX.setBounds(110, 245, 115, 20);
					jSliderAllArrowCorrectionX.setMinimum(-50);
					jSliderAllArrowCorrectionX.setMaximum(50);
					jSliderAllArrowCorrectionX.setMinorTickSpacing(1);
					jSliderAllArrowCorrectionX.setValue(1);
				}
				
				///arrow correctionx
				{
					JLabel jLabelArrowCorrectionY = new JLabel();
					jPanelMain.add(jLabelArrowCorrectionY);
					jLabelArrowCorrectionY.setText("Move arrow by y");
					jLabelArrowCorrectionY.setBounds(10, 265, 102, 16);
					jLabelArrowCorrectionY.setFont(new java.awt.Font("Arial",0,10));
				}
				{
					jTextFieldArrowCorrectionY = new JTextField(); 
					jPanelMain.add(jTextFieldArrowCorrectionY);
					jTextFieldArrowCorrectionY.setText("0");
					jTextFieldArrowCorrectionY.setBounds(225, 265, 30, 20);
					jTextFieldArrowCorrectionY.setFont(new java.awt.Font("Arial",0,10));
					jTextFieldArrowCorrectionY.setBackground(new java.awt.Color(238,238,238));
				}
				{
					jSliderAllArrowCorrectionY = new JSlider();
					jPanelMain.add(jSliderAllArrowCorrectionY);
					jSliderAllArrowCorrectionY.setBounds(110, 265, 115, 20);
					jSliderAllArrowCorrectionY.setMinimum(-50);
					jSliderAllArrowCorrectionY.setMaximum(50);
					jSliderAllArrowCorrectionY.setMinorTickSpacing(1);
					jSliderAllArrowCorrectionY.setValue(1);
				}
											
			
				// add buttons "Help"  "Save points (s)" "Interpolate (i)" "Draw arrow (a)" "Delete arrows (d)" "Delete points" "Load points (l)"
				{
					interpolate_button=new JButton("Interpolate (i)");
					jPanelMain.add(interpolate_button);
					interpolate_button.setFont(new java.awt.Font("Arial",0,10));
					interpolate_button.setBounds(20,290,115, 20);    
					}
				{
					drawarrow_button=new JButton("Draw arrows (d)");
					jPanelMain.add(drawarrow_button);
					drawarrow_button.setFont(new java.awt.Font("Arial",0,10));
					drawarrow_button.setBounds(145,290,115, 20);    
					}
				{
					delete_arrow_button=new JButton("Remove arrows (r)");
					jPanelMain.add(delete_arrow_button);
					delete_arrow_button.setFont(new java.awt.Font("Arial",0,9));
					delete_arrow_button.setBounds(20,312,115, 20);    
					}
				{
					deletepoints_button=new JButton("Remove points");
					jPanelMain.add(deletepoints_button);
					deletepoints_button.setFont(new java.awt.Font("Arial",0,10));
					deletepoints_button.setBounds(145,312,115, 20);    
					}
				{
					loadpointlistbutton=new JButton("Load points (l)");
					jPanelMain.add(loadpointlistbutton);
					loadpointlistbutton.setFont(new java.awt.Font("Arial",0,10));
					loadpointlistbutton.setBounds(20,334,115, 20);    
					}
				{
					savepointlistbutton=new JButton("Save points (s)");
					jPanelMain.add(savepointlistbutton);
					savepointlistbutton.setFont(new java.awt.Font("Arial",0,10));
					savepointlistbutton.setBounds(145,334,115, 20);    
					}
			    {
					addoverlay_button=new JButton("Permanently add overlay");
					jPanelMain.add(addoverlay_button);
					addoverlay_button.setFont(new java.awt.Font("Arial",0,10));
					addoverlay_button.setBounds(20,356,240, 20);    
					
				}
			    
			    {
			    	open_newmovie_button=new JButton("Open new movie");
					jPanelMain.add(open_newmovie_button);
					open_newmovie_button.setFont(new java.awt.Font("Arial",0,10));
					open_newmovie_button.setBounds(20,378,240, 20);    
					
				    }
			   {
				help_button=new JButton("Help");
				jPanelMain.add(help_button);
				help_button.setFont(new java.awt.Font("Arial",0,10));
				help_button.setBounds(20,400,240, 20);    
				
			    }
	  
				////key explanation
							{
								jLabelPluginDescription = new JLabel();
								jPanelMain.add(jLabelPluginDescription);
								jLabelPluginDescription.setText("Cite: Daetwyler, Modes and Fiolka (2020). Biology Open");
								jLabelPluginDescription.setBounds(20, 422, 260, 30);
								jLabelPluginDescription.setFont(new java.awt.Font("Arial",0,9));
							}
			}
			pack();
			this.setSize(280, 475);//size of whole panel
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.
	 *  
	 * @see EventListenerList
	 */
	private void fireActionEvent() {
		if (!firingActionEvent) {
			// Set flag to ensure that an infinite loop is not created
			firingActionEvent = true;
			ActionEvent e = null;
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			long mostRecentEventTime = EventQueue.getMostRecentEventTime();
			int modifiers = 0;
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
			if (currentEvent instanceof InputEvent) {
				modifiers = ((InputEvent)currentEvent).getModifiers();
			} else if (currentEvent instanceof ActionEvent) {
				modifiers = ((ActionEvent)currentEvent).getModifiers();
			}
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for ( int i = listeners.length-2; i>=0; i-=2 ) {
				if ( listeners[i]==ActionListener.class ) {
					// Lazily create the event:
					if ( e == null )
						e = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,
								"arrowPropertyChanged",
								mostRecentEventTime, modifiers);
					((ActionListener)listeners[i+1]).actionPerformed(e);
				}
			}
			firingActionEvent = false;
		}
	}

}
