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
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.Vector;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.vecmath.Vector3d;

import com.bulletphysics.collision.shapes.SphereShape;

import Network.*;
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
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
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
	private Camera camera;
	private Action moveForwardAction, moveLeftAction, moveRightAction,
		moveBackwardAction, yawLeftAction, yawRightAction, pitchUpAction, 
		pitchDownAction;

	private Action shootForward;
	 
	
	//Declare Scene node variables
	private SceneNode cameraNode;
    private SceneNode dolphinNode;
    private SceneNode tessN, ballNode, ballNodeOn, ballNodeOff, treeNode, treeNode2;
    private SceneNode plightNode;
    private Entity ballOffE;
    
	private InputManager im; // Input Manager for action classes
	private SceneManager sm;
	
	//Dolphin Scene
	private List<String> planetsCollidedWith = new ArrayList<String>(); //list of planets already collided with
	private List<SceneNode> terrainNodes = new ArrayList<SceneNode>();
	
	
	//Minimizing variable allocation in update /From dolphin click source code
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
	private int health = 100;
	
	//PHYSICS ENGINE VARIABLES
	private PhysicsEngine physicsEng;
	private PhysicsObject carPhysObj, treePhysObj, gndPlane;
	private boolean shooting = false;
	private float time;
	private static int numOfProjectiles = 0;
	

    public MyGame(String serverAddr, int sPort) {
    	super();
    	serverAddress = serverAddr;
    	serverPort = sPort;
    	serverProtocol = ProtocolType.UDP;
    }
    
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
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
        camera.getFrustum().setFarClipDistance(4000.0f);
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
		
		//use the spin speed setting from the first script to initialze dolphin rotation
		scriptFile1 = new File("scripts/doubleSpeed.js");
		runScript(scriptFile1);
		
        makeEntities(eng);

        
		//**LIGHT**
        //LIGHT SETUP THROGUH SCRIPT
        scriptFile2 = new File("scripts/CreateLight.js");
        jsEngine.put("sm", sm);
        this.runScript(scriptFile2);
        plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject((Light)jsEngine.get("plight"));
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
        
        
        //Call setup inputs function
		setupInputs();	
		setupOrbitCameras(eng, sm);
		
		// 2^patches: min=5, def=7, warnings start at 10
		Tessellation tessE = sm.createTessellation("tessE", 6);
		// subdivisions per patch: min=0, try up to 32
		tessE.setSubdivisions(16f);
		tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
		tessN.attachObject(tessE);
		
		// to move it, note that X and Z must BOTH be positive OR negative
		// tessN.translate(Vector3f.createFrom(-6.2f, -2.2f, 2.7f));
		//tessN.yaw(Degreef.createFrom(37.2f));
		tessN.scale(8000.0f, 55000.0f, 8000.0f);
		tessE.setTexture(this.getEngine(), "mountain.jpg");
		tessE.setHeightMap(this.getEngine(), "MountainH.png");
		tessE.setNormalMap(this.getEngine(),"MountainN.png");
		setSkyBox(eng);

		
        //initPhysicsSystem();
        //createRagePhysicsWorld();		
	}

	/*
	 * UPDATE CAMERAS
	 */
	protected void setupOrbitCameras(Engine eng, SceneManager sm) { 
		
		String gpName = im.getFirstGamepadName();
		//String msName = im.getMouseName();
		String kbName = im.getKeyboardName();
		orbitController1 = new Camera3PController(camera, cameraNode, dolphinNode, gpName, im);
		orbitController2 = new Camera3PController(camera, cameraNode, dolphinNode, kbName, im);
		//orbitController3 = new Camera3PController(camera2, cameraNode2, dolphinNode2, msName, im);
	}

	/*
	 * UPDATE
	 */
	@Override
	protected void update(Engine engine) {
		String 	dispStr = "Health Remaining: " + health;
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

		//Projectile
		if (shooting) {
			//keep track of time starting from 0
			projectileTime += engine.getElapsedTimeMillis();
			
				//projectile doesn't exist so create one and send create message to server
			    if (ballNodeOn == null) {
		        ballNodeOn = sm.getRootSceneNode().createChildSceneNode("ballNodeOn");
		        ballNodeOn.scale(4.5f, 4.5f, 4.5f);
		        ballNodeOn.attachObject(ballOffE);
		        ballNodeOn.moveForward(6.0f);
		        ballNodeOn.setLocalPosition(dolphinNode.getLocalPosition());	
				ballNodeOn.setLocalRotation(dolphinNode.getLocalRotation());
				gameClient.sendCreateMessagesP(ballNodeOn.getWorldPosition());
			    }
			    
			    //Move the ball node 
			    if (ballNodeOn != null) {
					ballNodeOn.moveForward(30.0f);//ball speed
					gameClient.sendMoveMessagesP(ballNodeOn.getWorldPosition());
					gameClient.sendScaleMessagesP(ballNodeOn.getLocalScale());
					gameClient.sendRotateMessagesP(ballNodeOn.getWorldRotation());
					
					for (SceneNode element : terrainNodes) {
						checkCollision(element);
					}
	
					updateProjectilePosition();
			    }
			    
			//Projectile only exists for 2.5 seconds
			if (projectileTime >= 1500f ) {
				//System.out.println("Shooting no more");
				projectileTime = 0f;
				sm.destroySceneNode(ballNodeOn);
				ballNodeOn = null;
				shooting = false;
			}
		}
			gameClient.sendScaleMessages(dolphinNode.getLocalScale());
			gameClient.sendRotateMessages(dolphinNode.getWorldRotation());	

	}

	/*
	 * PHYSICS SYSTEM
	 */
	private void initPhysicsSystem() { 
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0f,-1f, 0f};
		physicsEng = PhysicsEngineFactory.createPhysicsEngine(engine);
		//physicsEng.initSystem();
		//physicsEng.setGravity(gravity);

	}
	
	/*
	 * PHYSICS WORLD
	 */
	private void createRagePhysicsWorld() {
		float mass = 1.0f;
		float up[] = {0.0f, 1.0f, 0.0f};
		double[] temptf;
		float [] tempSize = {1.5f, 1.5f, 1.5f};
		temptf = toDoubleArray(treeNode.getLocalTransform().toFloatArray());
		//treePhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 2.0f);
		//treePhysObj.setBounciness((float) 1.0);
		//treeNode.setPhysicsObject(treePhysObj);

		
		temptf = toDoubleArray(dolphinNode.getLocalTransform().toFloatArray());
		//carPhysObj = physicsEng.addSphereObject(physicsEng.nextUID(), mass, temptf, 2.0f);
		//carPhysObj.setBounciness(1.0f);
		//dolphinNode.setPhysicsObject(carPhysObj);

		/*temptf = toDoubleArray(tessN.getLocalTransform().toFloatArray());
		gndPlane = physicsEng.addStaticPlaneObject(physicsEng.nextUID(), temptf, up, 0.0f);
		gndPlane.setBounciness(1.0f);
		tessN.scale(8000.0f, 55000.0f, 8000.0f);
		tessN.setPhysicsObject(gndPlane);*/
		//ballPhysObj.setFriction(1.0f);
		//ballPhysObj.setLinearVelocity(horizontal);
		//ballNodeOn.setPhysicsObject(ballPhysObj);
		//ballNodeOn.setLocalPosition((ballNodeOn.getLocalPosition());
		
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
		

		//Scene Node Obj
		Vector3f nodeObjLoc = (Vector3f) nodeObj.getLocalPosition();
		
		//Car loc from Scene Node
		Vector3f ballLoc = (Vector3f) ballNodeOn.getLocalPosition();
		Vector3f ballDistFromObj = (Vector3f)nodeObjLoc.sub(ballLoc);
		
		
		float ballDistFromOb = ballDistFromObj.length();
		
		//How big the entity is x axis wise
		float nodeObjDis = nodeObj.getLocalScale().x();
		

		//When player collides with planet, place the core on the dolphin and make planet smaller
		if (ballDistFromOb < nodeObjDis + 5 ) {
			if (!planetsCollidedWith.contains(nodeObj.getName())) {
				System.out.println("MY OWN COLLISION SHIT NOT FROM THE ENGINE");
				planetsCollidedWith.add(nodeObj.getName());
				sm.destroySceneNode(nodeObj.getName());
				//sm.destroySceneNode(ballNodeOn);
			}
		}
		
	}
	

	/*
	 * SETUP INPUTS FOR CONTROLS
	 */
	protected void setupInputs() {
		//New input manager
		im = new GenericInputManager();
		System.out.print("INPUTSACTIVE");
		
		SendCloseConnectionPacketAction close = new SendCloseConnectionPacketAction();
		
		//Initialize Action Classes
		moveForwardAction = new MoveForwardAction(dolphinNode, gameClient, this);
		moveBackwardAction = new MoveBackAction(dolphinNode, gameClient, this);
		moveLeftAction = new MoveLeftAction(dolphinNode, gameClient, this);
		moveRightAction = new MoveRightAction(dolphinNode, gameClient, this);
		yawLeftAction = new YawLeftAction(dolphinNode, gameClient, this);
		yawRightAction = new YawRightAction(dolphinNode, gameClient, this);
		pitchUpAction = new PitchUpAction(dolphinNode, gameClient, this);
		pitchDownAction = new PitchDownAction(dolphinNode, gameClient, this);
		
        shootForward = new ShootForward(this);
			
			
		//attach action objects to keyboard 
    	if(im.getKeyboardName() != null) {
    		String kbName = im.getKeyboardName();
    		System.out.println("inside key");
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
        Entity dolphinE = sm.createEntity("myDolphin", "fullycar3.obj");
		Material dolphinMat = sm.getMaterialManager().getAssetByPath("fullycar3.mtl");
	    Texture tex = eng.getTextureManager().getAssetByPath("fullycar3.png"); //Get Texture
	    TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    tstate.setTexture(tex); //set texture for black hole
	    dolphinE.setRenderState(tstate);//
        dolphinE.setPrimitive(Primitive.TRIANGLES);    
		dolphinE.setMaterial(dolphinMat);

        dolphinNode = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphinNode.attachObject(dolphinE);
        dolphinNode.setLocalPosition(778.5f, 2.5f, 3540.6f);
		Angle rotAmt = Degreef.createFrom(50f);
        dolphinNode.yaw(rotAmt);
        dolphinNode.scale(1.5f, 1.5f, 1.5f);

        
        /*---Set up trees----*/
        Entity tree1 = sm.createEntity("tree1", "tree.obj");
        Material treeMat = sm.getMaterialManager().getAssetByPath("tree.mtl");
        tree1.setPrimitive(Primitive.TRIANGLES);
        tree1.setMaterial(treeMat);
        treeNode = sm.getRootSceneNode().createChildSceneNode("treeNode");
        treeNode.attachObject(tree1);
        treeNode.setLocalPosition(778.5f, 5.2f, 3572.6f);
        treeNode.scale(8.0f, 8.0f, 8.0f);
        terrainNodes.add(treeNode);
        
        Entity tree2 = sm.createEntity("tree2", "tree2.obj");
        Material treeMat2 = sm.getMaterialManager().getAssetByPath("tree.mtl");
        tree2.setPrimitive(Primitive.TRIANGLES);
        tree2.setMaterial(treeMat2);
        treeNode2 = sm.getRootSceneNode().createChildSceneNode("treeNode2");
        treeNode2.attachObject(tree2);
        treeNode2.setLocalPosition(710.5f, 1.2f, 3566.6f);
        treeNode2.scale(12.0f, 12.0f, 12.0f);
        terrainNodes.add(treeNode2);
	}
	
	//Updare vertical position of the car for terrain
	public void updateVerticalPosition() {
		SceneNode dolphinN = this.getEngine().getSceneManager().getSceneNode("myDolphinNode");
		SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
		
		//Figure out Avatar's position relative to plane
		Vector3 worldAvatarPosition = dolphinN.getWorldPosition();
		Vector3 localAvatarPosition = dolphinN.getLocalPosition();
		float terrHeight = tessE.getWorldHeight(worldAvatarPosition.x()+0.1f, worldAvatarPosition.z()+0.1f);
		// use avatar World coordinates to get coordinates for height		
		//Sets Avatar above terrain 
		Vector3 newAvatarPosition = Vector3f.createFrom(
				localAvatarPosition.x(),
				terrHeight+2.5f,
				localAvatarPosition.z());
		dolphinN.setLocalPosition(newAvatarPosition);	
	}
	
	
	public void updateProjectilePosition() {
		SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
		Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
		
		
		//Figure out Avatar's position relative to plane
		Vector3 worldProjectilePosition = ballNodeOn.getWorldPosition();
		Vector3 localProjectilePosition = ballNodeOn.getLocalPosition();
		float terrHeight = tessE.getWorldHeight(worldProjectilePosition.x()+0.1f, worldProjectilePosition.z()+0.1f);
		
		//Sets Avatar above terrain 
		Vector3 newProjectilePosition = Vector3f.createFrom(
				localProjectilePosition.x(),
				terrHeight+2.5f,
				localProjectilePosition.z());
		ballNodeOn.setLocalPosition(newProjectilePosition);	
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
			server = new GameServerUDP(serverPort);
			server.getLocalInetAddress();
			//System.out.println("The server connection info is " + server.getLocalInetAddress() + ":" + serverPort);
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
		
		//If you are joining enter in address
		else
		{
			System.out.println("Enter host server's IP address:");
			String serverIP = r.nextLine();
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
		SceneNode dolphinN = sm.getSceneNode("myDolphinNode");
		return dolphinN.getWorldPosition();
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
	
	/*-----------------------
	 * Add a new ghost avatar
	 ----------------------*/
	public void addGhostAvatarToGameWorld(GhostAvatar avatar, Vector3 pos)
			throws IOException {
		if (avatar != null) { 
			//Avatar
			numOfAvatars += 1;
			sm = this.getEngine().getSceneManager();
			Entity ghostE = sm.createEntity("ghosts" + String.valueOf(numOfAvatars), "fullycar3.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);		
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(avatar.getID().toString());
			//System.out.println(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(pos);
			avatar.setNode(ghostN);
			avatar.setEntity(ghostE);
			
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
}
