package a3;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.vecmath.Vector3d;

import com.bulletphysics.collision.shapes.SphereShape;

import NPC.NPC;
import NPC.NPCcontroller;
import Network.*;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.networking.IGameConnection.ProtocolType;
import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;

import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;

import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Camera;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkeletalEntity;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.util.BufferUtil;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import myGameEngine.nodeControllers.*;
import myGameEngine.shootAction.ShootForward;
import myGameEngine.shootAction.SwitchLight;
import net.java.games.input.Event;
import myGameEngine.avatarMovement.*;
import myGameEngine.camera3PMovement.*;

import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;
import java.awt.geom.*;

import ray.rml.Angle;
import ray.rml.Degreef;

import ray.physics.PhysicsEngine;
import ray.physics.PhysicsObject;
import ray.physics.PhysicsEngineFactory;


public class MyGame extends VariableFrameRateGame implements MouseListener, MouseMotionListener  {

	//Declare Action variables
	private static List<Vector3> ghostLoc = new ArrayList<>();
	private Camera camera;
	private Action moveForwardAction, moveLeftAction, moveRightAction,
		moveBackwardAction, yawLeftAction, yawRightAction, pitchUpAction, 
		pitchDownAction;

	private Action shootForward, switchLight;
	 
	//Declare Scene node variables
	private SceneNode cameraNode;
    private SceneNode vehicleNode;
    private SceneNode tessN, ballNodeOn, treeNode, treeNode2;
    private Entity ballOffE;
    
	private InputManager im; // Input Manager for action classes
	private SceneManager sm;
	
	//vehicle Scene
	private List<String> ballCollidesWithNode = new ArrayList<String>(); //list of planets already collided with
	private List<String> carCollidesWithNode = new ArrayList<String>(); //list of planets already collided with
	private List<SceneNode> terrainNodes = new ArrayList<SceneNode>();
	
	
	//Minimizing variable allocation in update /From vehicle click source code
	private GL4RenderSystem rs;
	private float elapsTime = 0.0f;
	private float projectileTime = 0.0f;
	private String elapsTimeStr, counterStr, dispStr, dispStr2;
	private int elapsTimeSec, counter = 0;
	
	
	//CAMERA VARIABLES
	private Camera3PController orbitController1, orbitController2, orbitController3;
	
	//CONTROLLER VARIABLES
	private StretchController sc;
	private BounceController bc, bc2;
	private RotateAroundController rac;
	private Viewport topViewport;
	
	//SCRIPT ENGINE VARIABLES
	protected ScriptEngine jsEngine;
	protected File scriptFile3, scriptFile1, scriptFile2;
	
	//SERVER VARIABLES
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private boolean isClientConnected = false;
	private Vector<UUID> gameObjectsToRemove;	
	private GameClient gameClient;
	private static GameServerUDP server;

	//SKYBOX
	private static final String SKYBOX_NAME = "SkyBox";
	static int numOfAvatars = 0;
	
	//GAME VARIABLES
	private static MyGame game;
	private int health = 0;
	
	//PHYSICS ENGINE VARIABLES
	private PhysicsEngine physicsEng;
	private PhysicsObject ball1, ball2, gndPlane, car, npcPhy;
	private boolean shooting = false;
	private float time;
	private SceneNode ball1Node;
	private SceneNode ball2Node;
	private boolean running = false;
	private int carId = 1;
	private float physTime;
	private static int numOfProjectiles = 0;
	private static String vehicelEntityName;
	private static String vehicleObj;
	private static String vehiclesMat;
	private static String vehicleTexture;
	private Entity treeE[];
	private SceneNode treeN[], treeLight[];
	private PhysicsObject ballsPhysObj[], ball; 
	private NPCcontroller npcController; //npc controller
	private NPC npc;
	private int numOfNPC;
	private PhysicsObject[] treeObjs = new PhysicsObject[5];
	private int count;
	private GhostAvatar currentGhostAv;
	
	private Light spotLight; //spotlight for car
	private Sounds newSound; //sound class
	PhysicsObject obj[];

    public MyGame(String serverAddr, int sPort) {
    	super();
    	serverAddress = serverAddr;
    	serverPort = sPort;
    	serverProtocol = ProtocolType.UDP;
    	newSound = new Sounds(this);
    }
    
    /*-------------------------
     * SETUP STUFF
     ------------------------*/
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 30), false);
		
	}
	
	// now we add setting up viewports in the window
	protected void setupWindowViewports(RenderWindow rw) { 
		rw.addKeyListener(this);
		rw.setTitle("");
		topViewport = rw.getViewport(0);
	}
	
	/*Set Up the camera*/
	@Override
	protected void setupCameras(SceneManager sms, RenderWindow rw) {
		setupWindowViewports(rw);
		sm = sms;
        SceneNode rootNode = sm.getRootSceneNode();
        
        //Top ViewPort
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);		
		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 2.5f));
        cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);       
        camera.setMode('n');
        camera.getFrustum().setFarClipDistance(1000.0f);
	}
	
	/*
	 * SETUP SCENE FUNCTION
	 * */
	@Override
	protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		sm = eng.getSceneManager();
    	setupNetworking();
		
        //Setup the Script Engine
		ScriptEngineManager factory = new ScriptEngineManager();
		List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");
		
		//use the spin speed setting from the first script to initialze vehicle rotation
		scriptFile1 = new File("scripts/doubleSpeed.js");
		runScript(scriptFile1);

        
		//**LIGHT**
        //LIGHT SETUP THROGUH SCRIPT
        scriptFile2 = new File("scripts/CreateLight.js");
        jsEngine.put("sm", sm);
        this.runScript(scriptFile2);
        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject((Light)jsEngine.get("plight"));
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));

             
        //Call setup inputs function
        makeEntities(eng);
		setupInputs();	
		setupOrbitCameras(eng, sm);
		

		//skybox
		setSkyBox(eng);

        initPhysicsSystem();
        createRagePhysicsWorld();
        
        //skyboc
        newSound.initAudio(sm);
        newSound.playWindSound();
		
	}

	/*
	 * UPDATE CAMERAS
	 */
	protected void setupOrbitCameras(Engine eng, SceneManager sm) { 
		
		String gpName = im.getFirstGamepadName();
		String kbName = im.getKeyboardName();
		orbitController1 = new Camera3PController(camera, cameraNode, vehicleNode, gpName, im);
		orbitController2 = new Camera3PController(camera, cameraNode, vehicleNode, kbName, im);
	}

	/*
	 * UPDATE
	 */
	@Override
	protected void update(Engine engine) {

		String dispStr;
		//plightNode2.setLocalPosition(vehicleNode.getWorldPosition().x(),vehicleNode.getWorldPosition().y(),vehicleNode.getWorldPosition().z());
		if (health < 5 ) {
			if(currentGhostAv != null) {
				//"Enenmy Health Remaining: " + health;
				dispStr = "Shots On Target:" + currentGhostAv.getHealth()  + "/5"+ 
						"                                                                           SHOOT AT OTHER PLAYER!";
			}
			
			else {
				dispStr = "Waiting for other Player!";
	
			}
		}
		else if (health >= 5) {
			dispStr = "YOU WIN!!!!!! YOU WIN!!!!! YOU WIN!!!! YOU WIN!!!!!! YOU WIN!!!! YOU WIN!!!!! YOU WIN!!!!!";
			vehicleNode.setLocalPosition(400, 400, 400);
		}
		else {dispStr = "ok";};
		int topBot = topViewport.getActualBottom();
			
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		
		//HUD
		rs.setHUD(dispStr, 25, topBot+5);	

		//checkCollision(NodeGoesHere);
		im.update(elapsTime);
		orbitController1.updateCameraPosition();
		orbitController2.updateCameraPosition();
		processNetworking(elapsTime);
		if(npcController!=null) {
			//npcController.start();
			npcController.updateNPCs();
		}
		
		//Projectile
		if (shooting) {
			
		
			//keep track of time starting from 0
			projectileTime += engine.getElapsedTimeMillis();
			
				//projectile doesn't exist so create one and send create message to server
			    if (ballNodeOn == null) {
			    	sm.getLight("ballLight").setVisible(true);
			        
			        ballNodeOn = sm.getRootSceneNode().createChildSceneNode("ballNodeOn");
			        ballNodeOn.scale(4.5f, 4.5f, 4.5f);
			        ballNodeOn.attachObject(ballOffE);	
			        ballNodeOn.setLocalPosition(vehicleNode.getLocalPosition().x(), 
			        		vehicleNode.getLocalPosition().y() + 2.5f, vehicleNode.getLocalPosition().z());
			        
					ballNodeOn.setLocalRotation(vehicleNode.getLocalRotation());
			        ballNodeOn.moveForward(10.0f);
			        ballNodeOn.attachObject(sm.getLight("ballLight"));

			        

					gameClient.sendCreateMessagesP(ballNodeOn.getWorldPosition());
					
					double[] temptf3 = toDoubleArray(ballNodeOn.getLocalTransform().toFloatArray());
					ball = physicsEng.addSphereObject(physicsEng.nextUID(), 5f, temptf3, 10.0f);
					//ball.applyForce(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
					//car.setBounciness(1.0f);
					ballNodeOn.setPhysicsObject(ball);
					newSound.playShotSound(ballNodeOn);
					
			    }
			    
			    //Move the ball node 
			    if (ballNodeOn != null) {
			    				       
					//Get vehicle transform
			    	ballNodeOn.moveForward(20.0f);//ball speed
			    					
					//System.out.println(ballNodeOn.getLocalPosition());
					gameClient.sendMoveMessagesP(ballNodeOn.getWorldPosition());
					gameClient.sendScaleMessagesP(ballNodeOn.getLocalScale());
					gameClient.sendRotateMessagesP(ballNodeOn.getWorldRotation());
					if (currentGhostAv != null) {
						checkHit(currentGhostAv.getNode());
						//System.out.println(currentGhostAv.getPosition());
					}
					
					//if collides with object destroy it
					for (SceneNode element : terrainNodes) {
						checkCollision(element);
					}
	
					updateProjectilePosition(ballNodeOn);
					
			    }
			    
			   
			    

		}

		if (running ) { 
			Matrix4 mat;
			//update Physics engine
			physicsEng.update(elapsTime);
			if (ballNodeOn != null) {
				//Get vehicle transform
				double[] temptfBall = toDoubleArray(ballNodeOn.getLocalTransform().toFloatArray());
				//update car object transform
				ball.setTransform(temptfBall);							
			}
			
			//Get vehicle transform
			double[] temptf = toDoubleArray(vehicleNode.getLocalTransform().toFloatArray());
			//update car object transform
			car.setTransform(temptf);
			
	        /*for (int i = 0; i < 60; i++) {
	        	double[] temptfs = toDoubleArray(treeN[i].getLocalTransform().toFloatArray());
	        	obj[i].setTransform(temptfs);
	        }*/
			
			for (SceneNode s : engine.getSceneManager().getSceneNodes()) { 
				
				//If the node is a physics object
				if (s.getPhysicsObject() != null ) { 
					
					mat = Matrix4f.createFrom(toFloatArray(
					s.getPhysicsObject().getTransform()));
					s.setLocalPosition(mat.value(0,3),mat.value(1,3),
					mat.value(2,3));

				} 
			}
			
		}
		//Projectile only exists for 2.5 seconds
		if (projectileTime >= 2000f ) {
			sm.getLight("ballLight").setVisible(false);
			//System.out.println("Shooting no more");
			projectileTime = 0f;
			sm.destroySceneNode(ballNodeOn);
			ballNodeOn = null;
			shooting = false;
			ballCollidesWithNode.clear();
			
		}
		
		//carLightNode.setLocalPosition(vehicleNode.getLocalPosition());
		//plightNode2.setLocalPosition(vehicleNode.getWorldPosition().x(),vehicleNode.getWorldPosition().y(),vehicleNode.getWorldPosition().z());
		gameClient.sendScaleMessages(vehicleNode.getLocalScale());
		gameClient.sendRotateMessages(vehicleNode.getWorldRotation());


	}
	

	/*
	 * PHYSICS SYSTEM
	 */
	private void initPhysicsSystem() { 
		
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0f, -3f, 0f};
		physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEng.initSystem();
		physicsEng.setGravity(gravity);

	}
	
	/*
	 * PHYSICS WORLD
	 */
	private void createRagePhysicsWorld() {
		float mass = 2.0f;
		float up[] = {0.0f, 1f, 0.0f};
		double[] temptf;
		
		//ballNode1
		temptf = toDoubleArray(ball1Node.getLocalTransform().toFloatArray());
		ball1 = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 3.0f);
		//ball1.setBounciness(1.0f);
		//ball1.setLinearVelocity(linear);
		//ball1.applyForce(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		ball1Node.setPhysicsObject(ball1);
		
		//ballNode2
		temptf = toDoubleArray(ball2Node.getLocalTransform().toFloatArray());
		ball2 = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 3.0f);
		//ball2.setBounciness(1.0f);
		//ball2.setLinearVelocity(linear);
		//ball2.applyForce(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		ball2Node.setPhysicsObject(ball2);
		obj = new PhysicsObject[treeN.length];

        for (int i = 0; i < treeN.length; i++) {
        	double[] temptf2 = toDoubleArray(treeN[i].getLocalTransform().toFloatArray());
        	obj[i] = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf2, 5.5f);
        	//obj[i] = physicsEng.addConeObject(physicsEng.nextUID(), mass, temptf2, 30.0f, 8.0f);
        	treeN[i].scale(50,50,50);
        	treeN[i].setPhysicsObject(obj[i]);
        }
		
		
		
		//car
		temptf = toDoubleArray(vehicleNode.getLocalTransform().toFloatArray());
		car = physicsEng.addSphereObject(physicsEng.nextUID(), 5f, temptf, 10.0f);
		car.applyForce(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		//car.setBounciness(1.0f);
		vehicleNode.setPhysicsObject(car);
		//System.out.println(vehicleNode.getPhysicsObject().getTransform());
		


		//ground plane
		temptf = toDoubleArray(tessN.getLocalTransform().toFloatArray());
		gndPlane = physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
		gndPlane.setBounciness(1.0f);
		tessN.scale(8000.0f, 55000.0f, 8000.0f);
		tessN.setLocalPosition(0.0f ,0.0f, 0.0f);
		tessN.setPhysicsObject(gndPlane);
		running = true;	
	}
	
	/*
	 * FLOAT ARRAY CREATOR
	 */
	private float[] toFloatArray(double[] arr) { 
		if (arr == null) return null;	
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) { 
			ret[i] = (float)arr[i];
		}
		return ret;
	}
	
	/*
	 * DOUBLE ARRAY CREATOR
	 */
	private double[] toDoubleArray(float[] arr) { 
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) { 
			ret[i] = (double)arr[i];
		}
		return ret;
	}
	
	
	//Sets whether projectile is being shot
	public void setShooting (boolean set) {
		shooting = set;
	}
	
	public boolean getShooting () {
		return shooting;
	}
	
	/*
	 * CHECK COLLISIONS BETWEEN OBJECTS
	 */
	public void checkCollision(SceneNode nodeObj) {
		
		Vector3f nodeObjLoc = (Vector3f) nodeObj.getLocalPosition(); //terrainNode location
		Vector3f ballLoc = (Vector3f) ballNodeOn.getLocalPosition(); //ball location	
		Vector3f ballDistFromObj = (Vector3f)nodeObjLoc.sub(ballLoc); //distance awayfrom ball and terrainNode loc
				
		float ballDistFromOb = ballDistFromObj.length(); //
		
		//How big the entity is x and y axis wise
		float nodeObjDis = nodeObj.getLocalScale().x();
		float nodeObjDisY = nodeObj.getLocalScale().y();
		

			//When player collides with planet, place the core on the vehicle and make planet smaller
			if (ballDistFromOb < nodeObjDis + 8 || ballDistFromOb < nodeObjDisY + 8 ) {
				if (!ballCollidesWithNode.contains(nodeObj.getName())) {
					System.out.println("DESTROYED");
					//ballCollidesWithNode.add(nodeObj.getName());
					//sm.destroySceneNode(nodeObj.getName());
				}
			}
	}
	
	public void checkHit(SceneNode vehicle) {
		
		Vector3f nodeObjLoc = (Vector3f) vehicle.getLocalPosition(); //terrainNode location
		Vector3f ballLoc = (Vector3f) ballNodeOn.getLocalPosition(); //ball location	
		Vector3f ballDistFromObj = (Vector3f)nodeObjLoc.sub(ballLoc); //distance awayfrom ball and terrainNode loc
				
		float ballDistFromOb = ballDistFromObj.length(); //
		
		//How big the entity is x and y axis wise
		float nodeObjDis = vehicle.getLocalScale().x();
		float nodeObjDisY = vehicle.getLocalScale().y();
		

			//When player collides with planet, place the core on the vehicle and make planet smaller
			if (ballDistFromOb < nodeObjDis + 8 || ballDistFromOb < nodeObjDisY + 8 ) {
				if (!ballCollidesWithNode.contains(vehicle.getName())) {
					System.out.println("You've been hit!");
					health += 1;
					currentGhostAv.decreaseHealth(1);
					ballCollidesWithNode.add(vehicle.getName());
					//sm.destroySceneNode(vehicle.getName());
				}
			}
	}
	

	/*
	 * SETUP INPUTS FOR CONTROLS
	 */
	protected void setupInputs() {
		//New input manager
		im = new GenericInputManager();
		
		SendCloseConnectionPacketAction close = new SendCloseConnectionPacketAction();
		
		//Initialize Action Classes
		moveForwardAction = new MoveForwardAction(vehicleNode, gameClient, this);
		moveBackwardAction = new MoveBackAction(vehicleNode, gameClient, this);
		moveLeftAction = new MoveLeftAction(vehicleNode, gameClient, this);
		moveRightAction = new MoveRightAction(vehicleNode, gameClient, this);
		yawLeftAction = new YawLeftAction(vehicleNode, gameClient, this);
		yawRightAction = new YawRightAction(vehicleNode, gameClient, this);
		pitchUpAction = new PitchUpAction(vehicleNode, gameClient, this);
		pitchDownAction = new PitchDownAction(vehicleNode, gameClient, this);
		
        shootForward = new ShootForward(this);
        switchLight = new SwitchLight(this);
			
		
		//attach action objects to keyboard 
    	if(im.getKeyboardName() != null) {
    		String kbName = im.getKeyboardName();
    		//move forward with W
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.D,
    				shootForward, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    		
    		//move forward with W
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.W,
    				moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		
    		//move back with S
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.S,
    				moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		
    		
    		//yaw right with E
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.Q,
    				yawLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		
    		
    		//yaw left with Q
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.E,
    				yawRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		
    		//Space bar to turn light off on
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.SPACE,
    				switchLight, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    		//Close client
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.C,
    				close, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	}
	}
	
	/*
	 *  CREATE THE ENTITITES
	 */
	private void makeEntities(Engine eng) throws IOException {
		//HeadLight for car
        spotLight = sm.createLight("headlight", Light.Type.SPOT);
        spotLight.setDiffuse(new Color(0.8f, 0.1f, 0.9f));
        spotLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        spotLight.setRange(100f);

        //MAIN LIGHT OVERHEAD
        Light dLight = sm.createLight("dLight", Light.Type.DIRECTIONAL);       
        dLight.setAmbient(new Color(.3f,.3f,.3f));
        dLight.setDiffuse(new Color(0.9f, 0.9f, 0.9f));
        dLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        dLight.setVisible(true);

        SceneNode dLightNode = sm.getRootSceneNode().createChildSceneNode("dLightNode");
        dLightNode.attachObject(dLight);
        dLightNode.setLocalPosition(0f, 120f, 0f);
        
        //When shooting light up scene with this
        Light ballLight = sm.createLight("ballLight", Light.Type.SPOT);       
        ballLight.setAmbient(new Color(.9f,.9f,.9f));
        ballLight.setDiffuse(new Color(0.9f, 0.9f, 0.9f));
        ballLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        ballLight.setVisible(false);
       
        
        
        //Terrain set up
		// 2^patches: min=5, def=7, warnings start at 10
		Tessellation tessE = sm.createTessellation("tessE", 6);
		// subdivisions per patch: min=0, try up to 32
		tessE.setSubdivisions(16f);
		tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
		tessE.setTexture(this.getEngine(), "mountain.jpg");
		tessE.setHeightMap(this.getEngine(), "MountainH.png");
		tessE.setNormalMap(this.getEngine(),"MountainN.png");
		tessN.attachObject(tessE);
		
		
        //BALL NODE SETUP
        Entity ballOnE = sm.createEntity("ballOnE", "sphere.obj");
		Material ballMat = sm.getMaterialManager().getAssetByPath("sphere.mtl");
        ballOnE.setPrimitive(Primitive.TRIANGLES);    
		ballOnE.setMaterial(ballMat);
		
        ballOffE = sm.createEntity("ballOffE", "sphere.obj");
		Material balloffMat = sm.getMaterialManager().getAssetByPath("sphere.mtl");
        ballOnE.setPrimitive(Primitive.TRIANGLES);    
		ballOnE.setMaterial(balloffMat);
		
		
		//CAR NODE SETUP
        Entity vehicleE = sm.createEntity("myvehicle", vehicleObj);
		Material vehicleMat = sm.getMaterialManager().getAssetByPath(vehiclesMat);
	    Texture tex = eng.getTextureManager().getAssetByPath(vehicleTexture); //Get Texture
	    TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstate.setTexture(tex); //set texture for black hole
	    vehicleE.setRenderState(tstate);//
        vehicleE.setPrimitive(Primitive.TRIANGLES);    
		vehicleE.setMaterial(vehicleMat);

        vehicleNode = sm.getRootSceneNode().createChildSceneNode(vehicleE.getName() + "Node");
        vehicleNode.attachObject(vehicleE);
        vehicleNode.attachObject(spotLight);

        vehicleNode.setLocalPosition(778.5f, 4.5f, 3540.6f);
		Angle rotAmt = Degreef.createFrom(46f);
        vehicleNode.yaw(rotAmt);
        vehicleNode.scale(2.5f, 2.5f, 2.5f);
        terrainNodes.add(vehicleNode);
        
        treeE = new Entity[60];
        treeN = new SceneNode[60];
        

        //TREES ARE CONES
        //Random r = new Random(seed);
        /*---Set up tree Entitites----*/
        Material treeMat = sm.getMaterialManager().getAssetByPath("ownCone.mtl");
	    Texture tex5 = eng.getTextureManager().getAssetByPath("goldTex.jpg"); //Get Texture
	    TextureState tstate5 = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstate5.setTexture(tex5); //set texture for black hole
        for (int i = 0; i < treeE.length; i++) {
        	treeE[i] = sm.createEntity("treeE"+i, "ownCone.obj");
        	treeE[i].setPrimitive(Primitive.TRIANGLES);

    	    treeE[i].setRenderState(tstate5);//
            treeE[i].setPrimitive(Primitive.TRIANGLES);   
            treeE[i].setMaterial(treeMat);
        }
        
        
        /*---Set up tree nodes----*/
        
        for (int i = 0; i < treeN.length; i++) {
        	int xAxis = ThreadLocalRandom.current().nextInt(-3997, 3990 + 1);       	
        	int zAxis = ThreadLocalRandom.current().nextInt(-3880, 4100 + 1);
        	//Tree nodes
        	treeN[i] = sm.getRootSceneNode().createChildSceneNode("treeN"+i);
        	treeN[i].attachObject(treeE[i]);
        	treeN[i].setLocalPosition(xAxis, 6.0f, zAxis);
        	terrainNodes.add(treeN[i]);       	
        }
                        

        //BALL PHYSICS OBJECTS
	    // Ball 1
	    SceneNode rootNode = sm.getRootSceneNode();
	    Entity ball1Entity = sm.createEntity("ball1", "cube.obj");
	    ball1Node = rootNode.createChildSceneNode("Ball1Node");
	    ball1Node.attachObject(ball1Entity);
	    //ball1Node.scale(3, 3, 3);
	    ball1Node.setLocalPosition(770.5f, 3.0f, 3472.0f);
	    terrainNodes.add(ball1Node);
	    // Ball 2
	    Entity ball2Entity = sm.createEntity("Ball2", "cube.obj");
	    ball2Node = rootNode.createChildSceneNode("Ball2Node");
	    ball2Node.attachObject(ball2Entity);
	    ball2Node.setLocalPosition(800.5f,3.0f,3472.0f);
	    terrainNodes.add(ball2Node);

		//sm = this.getEngine().getSceneManager();
        Entity npcE = sm.createEntity("npc", "man.obj");
        Material npcMat = sm.getMaterialManager().getAssetByPath("man.mtl");
        npcE.setPrimitive(Primitive.TRIANGLES);
        npcE.setMaterial(npcMat);
        SceneNode npcNode = sm.getRootSceneNode().createChildSceneNode("npcNode");
        npcNode.attachObject(npcE);
        npcNode.setLocalPosition(840.5f, 1.0f, 3600.6f);
        npcNode.scale(50.0f, 50.0f, 50.0f);
		Angle rotAmtNPC = Degreef.createFrom(166f);
        npcNode.yaw(rotAmtNPC);
        npc = new NPC(npcNode.getLocalPosition());
        terrainNodes.add(npcNode);
	        
		npc.setNode(npcNode);
		npc.setEntity(npcE);	
		setupNPC();
	     
	}
	
	//Updare vertical position of the car for terrain
	public void updateVerticalPosition() {
		SceneNode vehicleN = this.getEngine().getSceneManager().getSceneNode("myvehicleNode");
		SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
		
		//Figure out Avatar's position relative to plane
		Vector3 worldAvatarPosition = vehicleN.getWorldPosition();
		Vector3 localAvatarPosition = vehicleN.getLocalPosition();
		float terrHeight = tessE.getWorldHeight(worldAvatarPosition.x()+0.1f, worldAvatarPosition.z()+0.1f);
		// use avatar World coordinates to get coordinates for height		
		//Sets Avatar above terrain 
		Vector3 newAvatarPosition = Vector3f.createFrom(
				localAvatarPosition.x(),
				terrHeight+4.5f,
				localAvatarPosition.z());
		vehicleN.setLocalPosition(newAvatarPosition);	
	}
	
	
	public void updateProjectilePosition(SceneNode node) {
		//SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
		
		
		//Figure out Avatar's position relative to plane
		Vector3 worldProjectilePosition = node.getWorldPosition();
		Vector3 localProjectilePosition = node.getLocalPosition();
		float terrHeight = tessE.getWorldHeight(worldProjectilePosition.x()+0.1f, worldProjectilePosition.z()+0.1f);
		
		//Sets Avatar above terrain 
		if (node == ballNodeOn) {
 		Vector3 newProjectilePosition = Vector3f.createFrom(
				localProjectilePosition.x(),
				terrHeight+2.5f,
				localProjectilePosition.z());
		node.setLocalPosition(newProjectilePosition);	
		}
		
		else {
	 		Vector3 newProjectilePosition = Vector3f.createFrom(
					localProjectilePosition.x(),
					terrHeight+1f,
					localProjectilePosition.z());
			node.setLocalPosition(newProjectilePosition);	
		}
	}
	
	
	//Start Game
	public static void main(String[] args) throws IOException {

        Scanner r = new Scanner(System.in);
		System.out.println("Are you the host? (Enter y/n)");
		String response = r.nextLine();
		int serverPort = 6000;
 

		
		//If you are host use your own ip address
		if(response.charAt(0) == 'y')
		{
			System.out.println("You are the host");
			//GameServerUDP server = new GameServerUDP(serverPort);
			int avatarNum = selectionMenu();
		    switch (avatarNum) {
	        case 1:
	            //vehicelEntityName = "myVehicle";
	            vehicleObj = "fullycar3.obj";
	            vehiclesMat = "fullycar3.mtl";
	            vehicleTexture = "fullycar3.png";
	            break;
	        case 2:
	            //vehicelEntityName = "myVehicle";
	            vehicleObj = "truckv1.obj";
	            vehiclesMat = "truckv1.mtl";
	            vehicleTexture = "truckv1.png";
	            break;
		    }
			server = new GameServerUDP(serverPort);
			server.getLocalInetAddress();
			//System.out.println("The server connection info is " + server.getLocalInetAddress() + ":" + serverPort);  
			//System.out.println("Choose Car From Below:");
			
			
			
			System.out.println("waiting for client connection...");
			String[] msgTokens = server.getLocalInetAddress().toString().split("/");
			System.out.println(msgTokens[1]);
			game = new MyGame(msgTokens[1], serverPort);
	        try {
	            game.startup();
	            game.run();
	        } catch (Exception e) {
	            e.printStackTrace(System.err);
	        } finally {
	            game.shutdown();
	            game.exit();
	        }
		}
		else
		{
			
			System.out.println("Enter host server's IP address:");
			String serverIP = r.nextLine();
			int avatarNum = selectionMenu();
		    switch (avatarNum) {
	        case 1:
	            //vehicelEntityName = "myVehicle";
	            vehicleObj = "fullycar3.obj";
	            vehiclesMat = "fullycar3.mtl";
	            vehicleTexture = "fullycar3.png";
	            break;
	        case 2:
	            //vehicelEntityName = "myVehicle";
	            vehicleObj = "truck.obj";
	            vehiclesMat = "truck.mtl";
	            vehicleTexture = "fullycar3.png";
	            break;
		    }
			
			System.out.println("Joining server " + serverIP + ":" + serverPort);
			game = new MyGame(serverIP, serverPort);
	        try {
	            game.startup();
	            game.run();
	        } catch (Exception e) {
	            e.printStackTrace(System.err);
	        } finally {
	            game.shutdown();
	            game.exit();
	        }
		}	    
	}
	
	/*Selection Menu*/
	public static int selectionMenu() {
        int selection;
        Scanner input = new Scanner(System.in);
		
		System.out.println("Choose Car From Below:");
		System.out.println("1 - Car");
		System.out.println("2 - Truck");
		System.out.print("Enter Choice Number: ");
        selection = input.nextInt();
        return selection;  	
	}
	
	
	
	/*---------------
	 * Script Runner
	 ---------------*/
	private void runScript(File scriptFile1) { 
		try { 
			FileReader fileReader = new FileReader(scriptFile1);
			jsEngine.eval(fileReader);
			fileReader.close();
		}
		catch (FileNotFoundException e1) { 
			System.out.println(scriptFile1 + " not found " + e1); 
		}
		catch (IOException e2) { 
			System.out.println("IO problem with " + scriptFile1 + e2); 
		}
		catch (ScriptException e3) { 
			System.out.println("Script Exception in " + scriptFile1 + e3); 
		}
		catch (NullPointerException e4)
		{ 
			System.out.println ("Null ptr exception reading " + scriptFile1 + e4); }
	}
	
	
	/*-----------------
	 * Set SkyBox
	 ----------------*/
	private void setSkyBox(Engine eng) throws IOException {
		// set up sky box
		Configuration conf = eng.getConfiguration();
		TextureManager tm = getEngine().getTextureManager();
		tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
		Texture front = tm.getAssetByPath("front.jpg");
		Texture back = tm.getAssetByPath("back.jpg");
		Texture left = tm.getAssetByPath("left.jpg");
		Texture right = tm.getAssetByPath("right.jpg");
		Texture top = tm.getAssetByPath("top.jpg");
		Texture bottom = tm.getAssetByPath("bottom.jpg");
		tm.setBaseDirectoryPath(conf.valueOf("assets.textures.path"));
		// cubemap textures are flipped upside-down.
		// All textures must have the same dimensions, so any image’s
		// heights will work since they are all the same height
		AffineTransform xform = new AffineTransform();
		xform.translate(0, front.getImage().getHeight());
		xform.scale(1d, -1d);
		front.transform(xform);
		back.transform(xform);
		left.transform(xform);
		right.transform(xform);
		top.transform(xform);
		bottom.transform(xform);
		SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
		sb.setTexture(front, SkyBox.Face.FRONT);
		sb.setTexture(back, SkyBox.Face.BACK);
		sb.setTexture(left, SkyBox.Face.LEFT);
		sb.setTexture(right, SkyBox.Face.RIGHT);
		sb.setTexture(top, SkyBox.Face.TOP);
		sb.setTexture(bottom, SkyBox.Face.BOTTOM);
		sm.setActiveSkyBox(sb);
	}
	
	/*-------------------
	 * Process Networking
	 --------------------*/
	protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
		if (gameClient != null) {
			gameClient.processPackets();
		}
		// remove ghost avatars for players who have left the game
		Iterator<UUID> it = gameObjectsToRemove.iterator();
		if (it != null ) {
			while(it.hasNext()) { 
				sm.destroySceneNode(it.next().toString());
			}
		}
		gameObjectsToRemove.clear(); 
	}
	
	/*------------------
	 * Setup Networking
	 -----------------*/
	private void setupNetworking() { 
		gameObjectsToRemove = new Vector<UUID>();
		isClientConnected = false;
		try { 
			gameClient = new GameClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 
		catch (UnknownHostException e) { 
			e.printStackTrace();
		} 
		catch (IOException e) { 
			e.printStackTrace();
		}
		
		if (gameClient == null) { 
			System.out.println("missing protocol host"); }
		else
		{ // ask client protocol to send initial join message
			//to server, with a unique identifier for this client
			gameClient.sendJoinMessages();
		} 
	}
	
	/*----------------------
	 * Set Is Connected Flag
	 ---------------------*/
	public void setIsConnected(boolean flag) {
		isClientConnected = flag;
	}
	
	/*---------------------
	 * Gets Player Position
	 ---------------------*/
	public Vector3 getPlayerPosition() {
		SceneNode vehicleN = sm.getSceneNode("myvehicleNode");
		return vehicleN.getWorldPosition();
	}
	
	/*---------------------
	 * Gets Projectile Position
	 ---------------------*/
	public Vector3 getProjectilePosition() {
		//Vector3 pos;
		if (ballNodeOn != null) {
			return ballNodeOn.getWorldPosition();
		}
		else return null;
		//return pos;
	}
	
	public List<SceneNode> getTerrainNodePos() {		
		return terrainNodes;
	}
	
	/*-----------------------
	 * Add a new ghost avatar
	 ----------------------*/
	public void addGhostAvatarToGameWorld(GhostAvatar avatar, Vector3 pos)
		throws IOException {
		if (avatar != null) { 

			//Avatar
			numOfAvatars += 1;
			sm = this.getEngine().getSceneManager();
			Entity ghostE = sm.createEntity("ghosts" + String.valueOf(numOfAvatars), vehicleObj);
			ghostE.setPrimitive(Primitive.TRIANGLES);		
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(avatar.getID().toString());
			//System.out.println(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(pos);
			avatar.setNode(ghostN);
			avatar.setEntity(ghostE);
			count = avatar.getGhostAvatarCount();
			currentGhostAv = avatar;
			//System.out.println(count);			
		}
		
	}
	
	public void addGhostNPCToGameWorld(NPC newNpc, Vector3 pos)
			throws IOException {
		if (newNpc != null) { 
			//Avatar
			numOfNPC += 1;
			sm = this.getEngine().getSceneManager();
			Entity ghostE = sm.createEntity("ghostNPC" + String.valueOf(numOfNPC), "truck.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);		
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode("npc"+numOfNPC);
			//System.out.println(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(pos);
			newNpc.setNode(ghostN);
			newNpc.setEntity(ghostE);		
		}
	}
	
	/*-----------------------
	 * Add a new ghost projectile
	 ----------------------*/
	public void addGhostProjectileToGameWorld(GhostProjectile projectile, Vector3 pos)
			throws IOException {
		if (projectile != null) { 
			//Avatar
			numOfProjectiles  += 1;
			sm = this.getEngine().getSceneManager();
			Entity ghostE = sm.createEntity("ghostProj" + String.valueOf(numOfProjectiles), "sphere.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);		
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode("proj"+numOfProjectiles);
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(pos);
			projectile.setNode(ghostN);
			projectile.setEntity(ghostE);
		}
	}
	
	/*--------------------
	 * Remove ghost avatar
	 --------------------*/
	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar) { 
		if(avatar != null) {
			gameObjectsToRemove.add(avatar.getID());
		}
	}
	
	/*--------------------
	 * Remove ghost projectile
	 --------------------*/
	public void removeGhostProjectileFromGameWorld(GhostProjectile projectile) { 
		if(projectile != null) {
			gameObjectsToRemove.add(projectile.getID());
		}
	}
	
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction { // for leaving the game... need to attach to an input device
		@Override
		public void performAction(float time, Event evt) { 
			if(gameClient != null && isClientConnected == true) { 
				gameClient.sendByeMessages();
				game.shutdown();
				game.exit();
			} 
		}
	}
	
	//setup npc
	private void setupNPC()  {
		npcController = new NPCcontroller(npc);
	}
	
	//get sounds
	public Sounds getSounds() {
		return newSound;
	}

	//Switch Lights
	public void setLight() {
		if (sm.getLight("dLight").isVisible()) {
			sm.getLight("dLight").setVisible(false);
		}
		else
			sm.getLight("dLight").setVisible(true);
	}





}



