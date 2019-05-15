package myGameEngine.camera3PMovement;

import net.java.games.input.Component;
import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3PController {
	
	private Camera camera;			//current camera
	private SceneNode cameraN;		//node attached to camera
	private SceneNode target;		//target camera to look at
	private float cameraAzimuth;	//rotation of camera around y axis
	private float cameraElevation;	//elevation of camera above target
	private float radius;			//distance of camera and tager
	private Vector3 targetPos;		//target's position in the world
	private Vector3 worldUpVec;		//
	
	public Camera3PController(Camera cam, SceneNode camN, SceneNode targ,
				String controllerName, InputManager im) {
		camera = cam;
		cameraN = camN;
		target = targ;
		cameraAzimuth = 225.0f; 	//Start from BEHIND and ABOVE the target
		cameraElevation = 20.0f;	//elevation is in degrees
		radius = 30.0f;
		worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
		setupInput(im, controllerName);
		updateCameraPosition();
	}
	
	public void updateCameraPosition() {
		double theta = Math.toRadians(cameraAzimuth);
		double phi = Math.toRadians(cameraElevation);
		double x = radius * Math.cos(phi)*Math.sin(theta);
		double y = radius * Math.sin(phi);
		double z = radius * Math.cos(phi) * Math.cos(theta);
		cameraN.setLocalPosition(Vector3f.createFrom((float)x, (float)y, 
				(float)z).add(target.getWorldPosition()));
		cameraN.lookAt(target, worldUpVec);
	}
	
	private void setupInput(InputManager im, String cn) {
		String kbName = im.getKeyboardName();
		String gpName = im.getFirstGamepadName();
		String msName = im.getMouseName();
		Action orbitAroundAction = new OrbitAroundAction();
		Action orbitLeftAction = new OrbitAroundLeftAction();
		Action orbitRightAction = new OrbitAroundRightAction();
		Action orbitElevUpAction = new OrbitElevationUpAction();
		Action orbitElevDownAction = new OrbitElevationDownAction();
		Action orbitRadInAction = new OrbitRadiusInAction();
		Action orbitRadOutAction = new OrbitRadiusOutAction();

		/*GAMEPAD*/
		if (cn != null && cn == gpName) {
			System.out.println("Gamepad Active");
			/*----Left Orbit---*/
			//im.associateAction(cn, Component.Identifier.Axis.Z, 
				//	orbitAroundAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*----Elevate up----*/
			//im.associateAction(cn, Component.Identifier.Button._5,
				//	orbitElevUpAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*----Elevate down--*/
			//im.associateAction(cn, Component.Identifier.Button._4, 
				//	orbitElevDownAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*----Zoom In---*/
			//im.associateAction(cn, Component.Identifier.Button._3, //4 B
				//orbitRadInAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);		
			/*----Zoom Out*---*/
			//im.associateAction(cn, Component.Identifier.Button._0,
				//orbitRadOutAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
		
		/*KEYBOARD*/
		if (cn != null && cn == kbName) {
			System.out.println("Keyboard Active");
			/*Left Orbit*/
			im.associateAction(cn, Component.Identifier.Key.LEFT, 
				orbitLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*Right Orbit*/
			im.associateAction(cn, Component.Identifier.Key.RIGHT, 
				orbitRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*Elevate Up*/
			im.associateAction(cn, Component.Identifier.Key.UP, 
				orbitElevUpAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*Elevate Down*/
			im.associateAction(cn, Component.Identifier.Key.DOWN,
					orbitElevDownAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			/*----Zoom In---*/
			im.associateAction(cn, Component.Identifier.Key.M,
				orbitRadInAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);		
			/*----Zoom Out*---*/
			im.associateAction(cn, Component.Identifier.Key.N,
				orbitRadOutAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
		
		if (cn != null && cn == msName) {
			
		}
 	}
	
	/*
	 * Orbit for GamePad class
	 */
	private class OrbitAroundAction extends AbstractInputAction {

		public void performAction(float time, Event evt) {
			float rotAmount = 0.0f;
			
		    if (evt.getValue() < -0.2) { 
		    	rotAmount = 1.0f; 
		    }          
		    else if (evt.getValue() > 0.2) { 
		    	rotAmount = -1.0f; 
		    }          
		    else {
		    	rotAmount = 0.0f; 
		    }
		    
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		}
	}
	
	/*
	 * Orbit AroundLeft class
	 */
	private class OrbitAroundLeftAction extends AbstractInputAction {

		public void performAction(float time, Event evt) {
			float rotAmount = 2.0f;		    
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
			Angle rotAmt = Degreef.createFrom(rotAmount);
			target.yaw(rotAmt);
		}
	}
	
	/*
	 * Orbit Around Right
	 */
	private class OrbitAroundRightAction extends AbstractInputAction {
		
		public void performAction(float time, Event evt) {
			float rotAmount = -2.0f;
			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
			Angle rotAmt = Degreef.createFrom(rotAmount);
			target.yaw(rotAmt);
		}
	}
	
	/*
	 * Orbit Radius class (ZOOM IN)
	 */
	private class OrbitRadiusInAction extends AbstractInputAction {
		
		public void performAction(float time, Event evt) {
			float rotAmount = 0.5f;
			radius += rotAmount;
			if (radius >= 359.0f)
				radius = 359.0f;
			radius = radius % 360;
			System.out.println(radius);
			updateCameraPosition();
		}
	}
	
	/*
	 * Orbit Radius class (ZOOM OUT)
	 */
	private class OrbitRadiusOutAction extends AbstractInputAction {
		
		public void performAction(float time, Event evt) {
			float rotAmount = -0.5f;
			radius += rotAmount;
			if (radius <= 0.0f )
				radius = 0.001f;
			radius = radius % 360;
			if (radius >= 0.0f)
			updateCameraPosition();
		}
	}
	
	/*
	 * Elevation class
	 */
	private class OrbitElevationUpAction extends AbstractInputAction {
	    float y = target.getLocalPosition().y();
	    
		public void performAction(float time, Event evt) {
			float rotAmount = 1.0f;
			cameraElevation += rotAmount;
			
			//keeps camera from elevating up too much
			if (cameraElevation >= 90.0f) {
				cameraElevation = 89.99f;
			}
			
			//Keeps camera above ground plane
			if ((cameraElevation >= -90.0 && cameraElevation < -2.0f ) && y == 0.5f) {
				cameraElevation = 0.0f;
			}
			cameraElevation = cameraElevation % 360;
			updateCameraPosition();
		}
	}
	
	/*
	 * Elevation class
	 */
	private class OrbitElevationDownAction extends AbstractInputAction {

	      
		public void performAction(float time, Event evt) {
		    float y = target.getLocalPosition().y();
		    float x = target.getLocalPosition().x();
		    float z = target.getLocalPosition().z();
			System.out.println(target.getName());
			System.out.println(x);
			System.out.println(y);
			System.out.println(z);
			
			System.out.println(cameraElevation);
			float rotAmount = -1.0f;

			cameraElevation += rotAmount;
			//keeps camera from elevation down too much
			if (cameraElevation <= -90.0f)
				cameraElevation = -89.99f;
			
			//Keeps camera above ground plane when camera is elevated down dolphin and
			// dolphin
			if ((cameraElevation >= -90.0 && cameraElevation < -2.0f ) && y == 0.5f) {
				cameraElevation = 0.0f;
			}
			
			//keeps camera above ground plane
			if ((cameraElevation <= 0.0f && cameraElevation >= -2.0f) && y == 0.5f) {
				cameraElevation = 0.0f;
			}

			cameraElevation = cameraElevation % 360;
			updateCameraPosition();
		}
	}

}
