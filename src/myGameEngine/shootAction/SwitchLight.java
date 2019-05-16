package myGameEngine.shootAction;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import Network.GameClient;
import a3.MyGame;
import net.java.games.input.Event;

public class SwitchLight extends AbstractInputAction {
	//private Node projectileN;
	//private GameClient gameClient;
	private MyGame myGame;
	
	public SwitchLight(MyGame g) { 
		//projectileN = n;
		//gameClient = gc;
		myGame = g;
	}
	
	public void performAction(float time, Event e) { 
		//projectileN.moveForward(0.3f);
		//gameClient.sendMoveMessages(projectileN.getWorldPosition());
		//myGame.updateProjectilePosition();
		myGame.setLight();
	}
}