import java.awt.Color;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import lejos.robotics.mapping.MenuAction;
import lejos.robotics.mapping.NavigationPanel;
import lejos.robotics.mapping.NavigationModel.NavEvent;
import lejos.robotics.localization.*;

/**
 * Test of the Monte Carlo Localization algorithm for global localization.
 * 
 * You should run MCLTest from the samples project on the NXT. See the comments in that sample for how 
 * to set your robot up.
 * 
 * You will need to set up a line map of your room (or other environment). This should be in the SVG file, Roon.svg.
 * 
 * You can use a tool like svg-edit to set up this map. Make sure you only use <line> tags.
 * 
 * To control your localization robot from the PC, run this sample and connect to your robot, by typing in 
 * the name of the brick and pressing Connect.
 * 
 * Then press Load Map, and your map will be loaded and displayed with a randomly generated particle set.
 * 
 * You then control the robot by pressing Get Pose and Random Move. Get Pose will cause the robot to take
 * range readings and update its pose estimate. This will update the particle set.
 * 
 * Random Move will cause the robot to make a random travel, followed by rotate move. Again the particle set will 
 * be updated.
 * 
 * Keep clicking Get Pose and Random Move until the robot has a good estimate of its pose. You should see the
 * particle set cluster around a few possible poses, and eventually find the correct pose.
 * 
 * @author Lawrie Griffiths
 *
 */
public class MCLCommand extends NavigationPanel {
	private static final long serialVersionUID = 1L;

	private static final int FRAME_WIDTH = 1000;
	private static final int FRAME_HEIGHT = 800;
	private static final int NUM_PARTICLES = 200;
	private static final String TITLE = "MCL Command";
	private static final int INITIAL_ZOOM = 150;
	private static final Point INITIAL_VIEW_START = new Point(-150,-30);
	private static final String MAP_FILE_NAME = "Room.svg";
	private static final int MCL_CLEARANCE = 20;

	private static final JButton randomButton = new JButton("Random move");
	private static final JButton getPoseButton = new JButton("Get Pose");
	
	private static MCLPoseProvider mcl = new MCLPoseProvider(null,NUM_PARTICLES,MCL_CLEARANCE);
  
  	/**
   	* Create a MapTest object and display it in a GUI frame.
   	* Then connect to the NXT.
   	*/
  	public static void main(String[] args) throws Exception {
  		(new MCLCommand()).run();
  	}
  
  	public MCLCommand() {
  		setTitle(TITLE);
  		setDescription("MCLCommand shows the Monte Carlo Localization\nalgorithm in action");
  		
  		buildGUI();
  	}
  
  	/**
  	 * Build the application-specific GUI
  	 */
  	@Override
  	protected void buildGUI() {
  		// All panels required
	    super.buildGUI();
	    
	    // Add the Get Pose and Random Move buttons
		commandPanel.add(getPoseButton);
		commandPanel.add(randomButton);
		
		// disable buttons until connected
		getPoseButton.setEnabled(false);
		randomButton.setEnabled(false);
	
		// When Get pose is pressed, invoke the MCL Pose provider
		// to take readings and get the pose. Then get the updated
		// particles, the details of the estimated pose and the range
		// readings.
		getPoseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.getPose();
				model.getRemoteParticles();
				model.getEstimatedPose();
				//System.out.println("Max weight:" + model.getParticles().getMaxWeight());
				model.getRemoteReadings();
				getPoseButton.setEnabled(false);
			}
		});
		
		// When the Random Move button is pressed, make a random move 
		// and get the updated particles
		randomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				model.randomMove();
				model.getRemoteParticles();
			}
		});
		
		// Switch on tool tips for particle weights
		mapPanel.setToolTipText("");
  	}
  
  	/**
  	 * Called when the mouse is clicked in the map area
  	 */
  	@Override
	protected void popupMenuItems(Point p, JPopupMenu menu) { 
	    // Get details of the particle closest to the mouse click
	    menu.add(new MenuAction(NavEvent.FIND_CLOSEST, "Particle Readings", p, model, this));
	}
	
	/**
	 * Called whenever an event is received from the NXT
	 */
	@Override
	public void eventReceived(NavEvent navEvent) {
		// Enable the Get Pose button when the estimated pose has been sent
		if (navEvent == NavEvent.ESTIMATED_POSE) {
			getPoseButton.setEnabled(true);
		}
	}
	
	/**
	 * Called when the connection is established
	 */
	@Override
	public void whenConnected() {
		model.setDebug(true);
		// Load the map and generate the particles and sends both to the NXT
		model.loadMap(MAP_FILE_NAME);
		zoomSlider.setValue(INITIAL_ZOOM);
		mapPanel.viewStart = INITIAL_VIEW_START;
		model.generateParticles();
		
		// Enable buttons
		getPoseButton.setEnabled(true);
		randomButton.setEnabled(true);
	}
	
	/**
	 * Run the sample 
	 */
	public void run() throws Exception {
		// Associate the MCLPoseProvider with the model
		model.setMCL(mcl);
		
		// Open the MCLTest navigation panel in a JFrame window
	    openInJFrame(this, FRAME_WIDTH, FRAME_HEIGHT, TITLE, Color.white, menuBar);
	}
}