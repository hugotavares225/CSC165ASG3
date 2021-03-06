package myGameEngine.avatarMovement;

import Network.GameClient;
import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Angle;
import ray.rml.Degreef;

public class PitchUpAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	private MyGame myGame;
	
	public PitchUpAction(Node n, GameClient gc, MyGame g) { 
		avN = n;
		gameClient = gc;
		myGame = g;
	}
	
	public void performAction(float time, Event e) { 
		Angle rotAmt = Degreef.createFrom(3.0f);
		avN.pitch(rotAmt);
		gameClient.sendMoveMessages(avN.getWorldPosition());
	}

}
