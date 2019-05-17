package myGameEngine.avatarMovement;

import Network.GameClient;
import a3.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Node;

public class MoveBackAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	private MyGame myGame;
	
	public MoveBackAction(Node n, GameClient gc, MyGame g) { 
		avN = n;
		gameClient = gc;
		myGame = g;
	}
	
	public void performAction(float time, Event e) { 
		avN.moveBackward(5.0f);
		gameClient.sendMoveMessages(avN.getWorldPosition());
		myGame.updateVerticalPosition();
		//myGame.setCarIsMoving(true);
	}
}
