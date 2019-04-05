package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

public class GamePadBackForward extends AbstractInputAction {
	private Camera camera;
	private SceneNode node;
	
	public GamePadBackForward(Camera c, SceneNode d) {
		camera = c;
		node = d;
	}

	@Override
	public void performAction(float t, Event e) {
		float speed = 0.08f;
		//Move camera forward
		if (camera.getMode() == 'c' && e.getValue() < -0.1f ) {
			System.out.println(e);
			Vector3f n = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f) Vector3f.createFrom(speed*n.x(), speed*n.y(), speed*n.z());
			Vector3f p2 = (Vector3f) p.add(p1);
			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
		}
		else if (camera.getMode() == 'n' && e.getValue() < -0.1f) {
			node.moveForward(speed);
		}
		
		//Move camera backward
		if (camera.getMode() == 'c' && e.getValue() > 0.1f ) {
			System.out.println(e);
			Vector3f n = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f) Vector3f.createFrom(speed*n.x(), speed*n.y(), speed*n.z());
			Vector3f p2 = (Vector3f) p.sub(p1);
			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
		}
		else if (camera.getMode() == 'n' && e.getValue() > 0.1f) {
			node.moveBackward(speed);
		}		
	}
}
