package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

/*
 * Moves camera right using S Key or Y-Axis input from controller
 */

public class CameraRight extends AbstractInputAction {
	private Camera camera;
	private SceneNode node;
	
	public CameraRight(Camera c, SceneNode n) {
		camera = c;
		node = n;
		
	}
	
	@Override
	public void performAction(float t, Event e) {
		float speed = 0.1f;
		//Move using keyboard
		if (camera.getMode() == 'c') {
			Vector3f u = camera.getRt();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f) Vector3f.createFrom(speed*u.x(), speed*u.y(), speed*u.z());
			Vector3f p2 = (Vector3f) p.add(p1);
			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));	
		}
		else 
			node.moveRight(-1.0f*speed);
	}
}
