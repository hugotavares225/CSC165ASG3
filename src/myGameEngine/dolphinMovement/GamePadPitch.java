package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3f;

public class GamePadPitch extends AbstractInputAction {
	
	private Camera camera;
	private SceneNode node;

	public GamePadPitch(Camera c, SceneNode d) {
		camera = c;
		node = d;
	}
	
	@Override
	public void performAction(float t, Event e) {
		
		//Pitch Up
	    Angle rotAmt = Degreef.createFrom(0.5f); //rotate by 3 degrees
		if (camera.getMode() == 'c' && e.getValue() < -0.1) {
			//rotate n and v around u axis
			Vector3f u = camera.getRt(); //get right vector
			Vector3f n = camera.getFd(); //get forward vector
			Vector3f v = camera.getUp(); //get up vector 
		    v = (Vector3f) (v.rotate(rotAmt, u)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt, u)).normalize();
			camera.setUp(v);
			camera.setFd(n);
		}
		else if (camera.getMode() == 'n' && e.getValue() < -0.1) {
			node.pitch(rotAmt.negate());
		}

		
		//Pitch Down
		if (camera.getMode() == 'c' && e.getValue() > 0.1) {
			//rotate n and v around u axis
			Vector3f u = camera.getRt(); //get right vector
			Vector3f n = camera.getFd(); //get forward vector
			Vector3f v = camera.getUp(); //get up vector 
		    v = (Vector3f) (v.rotate(rotAmt.negate(), u)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt.negate(), u)).normalize();
			camera.setUp(v);
			camera.setFd(n);
		}
		else if (camera.getMode() == 'n' && e.getValue() > 0.1) {
			node.pitch(rotAmt);
		}	
	}

}
