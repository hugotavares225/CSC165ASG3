package myGameEngine.nodeControllers;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class StretchController extends AbstractController{
	
	private float scaleRate = .003f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private float direction = 2.0f;

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		totalTime += elapsedTimeMillis;
		float scaleAmt = 1.0f + direction * scaleRate;
		
		if (totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0f;
		}
		
		for (Node n: super.controlledNodesList) {
			Vector3 curScale = n.getLocalScale();
			curScale = Vector3f.createFrom(curScale.x()*scaleAmt, curScale.y(),
					curScale.z());
			n.setLocalScale(curScale);
		}
		
	}

}
