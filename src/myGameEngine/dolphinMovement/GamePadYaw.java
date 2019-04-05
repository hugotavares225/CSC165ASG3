package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3f;

public class GamePadYaw extends AbstractInputAction {
	
	private Camera camera;
	private SceneNode node;

	public GamePadYaw(Camera c, SceneNode d) {
		camera = c;
		node = d;
	}
	
	@Override
	public void performAction(float t, Event e) {
		
		//Yaw right
	    Angle rotAmt = Degreef.createFrom(2.0f); //rotate by 3 degrees
		if (camera.getMode() == 'c' && e.getValue() < -0.1) {
			//rotate n and v around u axis
			Vector3f u = camera.getRt(); 
			Vector3f n = camera.getFd();
			Vector3f v = camera.getUp();
		    u = (Vector3f) (u.rotate(rotAmt, v)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt, v)).normalize();
			camera.setRt(u);
			camera.setFd(n);
		}
		else if (camera.getMode() == 'n' && e.getValue() < -0.1) {
			node.yaw(rotAmt);
		}

		
		//Yaw Left
		if (camera.getMode() == 'c' && e.getValue() > 0.1) {
			//rotate u and n around v axis
			Vector3f u = camera.getRt();
			Vector3f n = camera.getFd();
			Vector3f v = camera.getUp();
		    u = (Vector3f) (u.rotate(rotAmt.negate(), v)).normalize();
		    n = (Vector3f) (n.rotate(rotAmt.negate(), v)).normalize();
			camera.setRt(u);
			camera.setFd(n);
		}
		else if (camera.getMode() == 'n' && e.getValue() > 0.1) {
			node.yaw(rotAmt.negate());
		}	
	}
}
