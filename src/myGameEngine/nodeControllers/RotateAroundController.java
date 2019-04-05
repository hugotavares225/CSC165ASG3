package myGameEngine.nodeControllers;

import ray.rage.scene.Node;
import ray.rage.scene.controllers.AbstractController;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class RotateAroundController extends AbstractController{
	
	private float RotateAroundRate = .009f;
	private float cycleTime = 2000.0f;
	private float totalTime = 0.0f;
	private float direction = 2.0f;

	@Override
	protected void updateImpl(float elapsedTimeMillis) {
		System.out.println("HI");
		totalTime += elapsedTimeMillis;
		float RotateAroundAmt = 1.0f + direction * RotateAroundRate;
		
		if (totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0f;
		}
		
		for (Node n: super.controlledNodesList) {
			Vector3 curPos = n.getLocalPosition();
			curPos = Vector3f.createFrom(curPos.x(), curPos.y()*RotateAroundAmt,
					curPos.z());
			n.setLocalPosition(curPos);
		}
		
	}

}
