package myGameEngine.dolphinMovement;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;
import net.java.games.input.Event;


/*
 * Moves camera backward using A Key or X-Axis input from controller
 */

public class CameraBackward extends AbstractInputAction {

	private Camera camera;
	private SceneNode node;
	
	public CameraBackward(Camera c, SceneNode d) {
		camera = c;
		node = d;
	}

	@Override
	public void performAction(float t, Event e) {
		float speed = 50.0f;
		/*For keyboard*/
		//rotate vector
		if (camera.getMode() == 'c') {
			Vector3f n = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f) Vector3f.createFrom(speed*n.x(), speed*n.y(), speed*n.z());
			Vector3f p2 = (Vector3f) p.sub(p1);
			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
		}
		else
			node.moveBackward(speed);
	}
}
