package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class PitchCameraDown extends AbstractInputAction {
	private Camera camera;
	private SceneNode node;
	
	public PitchCameraDown(Camera c, SceneNode n) {
		camera = c;
		node = n;
	}
	
	@Override
	public void performAction(float arg0, Event arg1) {
	    Angle rotAmt = Degreef.createFrom(-3.0f); //rotate by 3 degrees
		if (camera.getMode() == 'c') {
			//rotate n and v around u axis
			Vector3f u = camera.getRt(); //get right vector
			Vector3f n = camera.getFd(); //get forward vector
			Vector3f v = camera.getUp(); //get up vector 
		    v = (Vector3f) (v.rotate(rotAmt, u)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt, u)).normalize();
			camera.setUp(v);
			camera.setFd(n);
		}
		else {
			node.pitch(rotAmt.negate());
		}
	}

}