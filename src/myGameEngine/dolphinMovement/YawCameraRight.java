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

/*
 * Yaw camera using LEFT/RGHT arrow keys or RX-Axis input from controller
 */

public class YawCameraRight extends AbstractInputAction {
	private Camera camera;
	private SceneNode node;
	
	public YawCameraRight(Camera c, SceneNode n) {
		camera = c;
		node = n;
	}
	
	@Override
	public void performAction(float arg0, Event arg1) {
	    Angle rotAmt = Degreef.createFrom(-3.0f); //rotate by 3 degrees
		if (camera.getMode() == 'c') {
			//rotate u and n around v axis
			Vector3f u = camera.getRt(); 
			Vector3f n = camera.getFd();
			Vector3f v = camera.getUp();
		    u = (Vector3f) (u.rotate(rotAmt, v)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt, v)).normalize();
			camera.setRt(u);
			camera.setFd(n);
		}
		else 
			node.yaw(rotAmt);
	}
}