package NPC;

import java.util.Random;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

public class NPCcontroller {
	
	private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	private NPC[] NPClist = new NPC[5];
	private long thinkStartTime;
	private long tickStateTime; 
	private long lastThinkUpdateTime;
	private long lastTickUpdateTime;
	private NPC npc;
	private int numOfNPCs = 0;
	private boolean nearFlag = false;
	Random rand = new Random();
	
	public NPCcontroller (NPC n) {
		  thinkStartTime = System.nanoTime();
		  tickStateTime = System.nanoTime();
		  lastThinkUpdateTime = thinkStartTime;
		  lastTickUpdateTime = tickStateTime;
		  npc = n;
		  
		  //think
		  setupBehaviorTree();
		//npcLoop();
	}
	
	
	//update all the locations of an npcs
	public void updateNPCs()  { 
		  npc.updateLocation();  	
	}
	  
	  //Start tick and thick
	  public void start () {
		  /*thinkStartTime = System.nanoTime();
		  tickStateTime = System.nanoTime();
		  lastThinkUpdateTime = thinkStartTime;
		  lastTickUpdateTime = tickStateTime;
		  //setupNPC();
		  setupBehaviorTree();
		  npcLoop();	*/  
	  }
	  
	  /*public void setupNPC() {
			SceneManager sm = this.getEngine().getSceneManager();
			Entity npcE = sm.createEntity("ghostProj" + String.valueOf(numOfProjectiles), "sphere.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);		
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode("proj"+numOfProjectiles);
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(pos);
			projectile.setNode(ghostN);
			projectile.setEntity(ghostE);
		  npc = new NPC();
		  npc.setPos(Vector3f.createFrom((float)780, (float)2.5, (float)3560));
	  }*/
	  
	  public void npcLoop() {
		  while (true) {
			  long currentTime = System.nanoTime();
			  float elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
			  float elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);
			  
			  //TICK
			  if (elapsedTickMilliSecs >= 50.0f) {
				  lastTickUpdateTime = currentTime;			  
				  npc.updateLocation();
				  //server.sendNPCinfo();
			  }
			  
			  //THINK
			  if (elapsedThinkMilliSecs >= 500.0f) {
				  lastThinkUpdateTime = currentTime;
				  bt.update(elapsedThinkMilliSecs);
			  }
			  Thread.yield();
		  }
	  }
	  
	public void setupBehaviorTree() { 
		bt.insertAtRoot(new BTSequence(10));
		bt.insertAtRoot(new BTSequence(20));
		bt.insert(10, new OneSecPassed(this,npc,false));
		bt.insert(10, new GetSmall(npc));
		//bt.insert(20, new AvatarNear(server,this,npc,false));
		bt.insert(20, new GetBig(npc));
	}

	public boolean getNearFlag() {
		return nearFlag;
	} 	  
	
	public void setNearFlag(boolean set) {
		nearFlag = set;
	}

	public NPC getNPC() {
		return npc;
	}
	
	public int getNumOfNpcs () {
		return numOfNPCs;
	}
}
