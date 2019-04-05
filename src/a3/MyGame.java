package a3;


import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
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
import ray.rage.util.BufferUtil;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import myGameEngine.dolphinMovement.*;
import myGameEngine.nodeControllers.*;
import net.java.games.input.Event;
import myGameEngine.camera3PMovement.*;

import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;
import java.awt.geom.*;


public class MyGame extends VariableFrameRateGame implements MouseListener, MouseMotionListener  {
	
	//Declare Action variables
	private Action cameraBackward, cameraForward, cameraLeft, cameraRight, //Action Camera Classes Declarations
					yawCameraLeft, yawCameraRight, pitchCameraUp, pitchCameraDown, gamePadYaw, 
					gamePadPitch, gamePadBackForward, gamePadRightLeft;//offOnDolphin, //Action Camera Classes Declarations
	private Camera camera;
	
	
	//Declare Scene node variables
	private SceneNode cameraNode;
    private SceneNode dolphinNode;
	private InputManager im; // Input Manager for action classes
	private SceneManager sm;
	
	//Dolphin Scene
	private List<String> planetsCollidedWith = new ArrayList<String>(); //list of planets already collided with
	
	
	//Minimizing variable allocation in update /From dolphin click source code
	private GL4RenderSystem rs;
	private float elapsTime = 0.0f;
	private String elapsTimeStr, counterStr, dispStr, dispStr2;
	private int elapsTimeSec, counter = 0;
	
	/*----------ASG2--------*/
	
	//3P Camera declarations
	private Camera3PController orbitController1, orbitController2, orbitController3;
	
	//Controllers
	private StretchController sc;
	private BounceController bc, bc2;
	private RotateAroundController rac;
	private Viewport topViewport;
	
	//----3RD ASG----
	protected ScriptEngine jsEngine;
	protected File scriptFile3, scriptFile1;
	
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private boolean isClientConnected = false;
	private Vector<UUID> gameObjectsToRemove;	
	private GameClient gameClient;

	//skybox variables
	private static final String SKYBOX_NAME = "SkyBox";

    public MyGame(String serverAddr, int sPort) {
    	super();
    	serverAddress = serverAddr;
    	serverPort = sPort;
    	serverProtocol = ProtocolType.UDP;
    	setupNetworking();
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
        camera.getFrustum().setFarClipDistance(1000.0f);
	}
	
	@Override
	protected void setupScene(Engine eng, SceneManager sm) throws IOException {
		
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
        File scriptFile2 = new File("scripts/CreateLight.js");
        jsEngine.put("sm", sm);
        this.runScript(scriptFile2);
        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject((Light)jsEngine.get("plight"));
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
        
        //Call setup inputs function
		setupInputs();
		
		
		setupOrbitCameras(eng, sm);
		setSkyBox(eng);
	}

	//Orbit cameras
	protected void setupOrbitCameras(Engine eng, SceneManager sm) { 
		String gpName = im.getFirstGamepadName();
		//String msName = im.getMouseName();
		String kbName = im.getKeyboardName();
		orbitController1 = new Camera3PController(camera, cameraNode, dolphinNode, gpName, im);
		orbitController2 = new Camera3PController(camera, cameraNode, dolphinNode, kbName, im);
		//orbitController3 = new Camera3PController(camera2, cameraNode2, dolphinNode2, msName, im);
	}

	@Override
	protected void update(Engine engine) {
		String 	dispStr = "START";
		int topBot = topViewport.getActualBottom();
			
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		
		//HUD
		rs.setHUD(dispStr, 25, topBot+5);	

			
		//Check collisions with the planets	and black hole
		//checkCollision(NodeGoesHere);
		
		//process inputs
		im.update(elapsTime);
		orbitController1.updateCameraPosition();
		orbitController2.updateCameraPosition();
		processNetworking(elapsTime);
		//gc.processPackets();

	}
	
	//Checks collision between objects
	public void checkCollision(SceneNode nodeObj) {
		
		//Entity Location
		Vector3f entityLoc = (Vector3f) nodeObj.getLocalPosition();
		
		//Dolphin 1 location from entity 
		Vector3f dolphin1Loc = (Vector3f) dolphinNode.getLocalPosition();
		Vector3f dol1DistFromPl = (Vector3f)entityLoc.sub(dolphin1Loc);
		float plDistFromDolphin1 = dol1DistFromPl.length();
		
		//How big the entity is x axis wise
		float nodeObjDis = nodeObj.getLocalScale().x();
		
		
		//When player collides with planet, place the core on the dolphin and make planet smaller
		if (plDistFromDolphin1 < nodeObjDis + 2  &&  dolphinNode.getChildCount() != 1) {
			if (!planetsCollidedWith.contains(nodeObj.getName())) {
				planetsCollidedWith.add(nodeObj.getName());
			}
		}	
		
	}
	

	//SetUp Inputs for Controls
	protected void setupInputs() {
		//New input manager
		im = new GenericInputManager();
		
		//Instantiate Action classes 
		cameraForward = new CameraForward(camera, dolphinNode);
		cameraBackward = new CameraBackward(camera, dolphinNode);
		cameraRight = new CameraRight(camera, dolphinNode);
		cameraLeft = new CameraLeft(camera, dolphinNode);
		yawCameraLeft = new YawCameraLeft(camera, dolphinNode);
		yawCameraRight = new YawCameraRight(camera, dolphinNode);
		pitchCameraUp = new PitchCameraUp(camera, dolphinNode);
		pitchCameraDown = new PitchCameraDown(camera, dolphinNode);
		
		//Action classes 
		gamePadBackForward = new GamePadBackForward(camera, dolphinNode);
		gamePadRightLeft = new GamePadRightLeft(camera, dolphinNode);
		gamePadPitch = new GamePadPitch(camera, dolphinNode);
		gamePadYaw = new GamePadYaw(camera, dolphinNode);
		
		//attach action objects to gamepad
		if(im.getFirstGamepadName() != null) {
			String gpName = im.getFirstGamepadName();
    		//move forward
    		im.associateAction(gpName, 
    				net.java.games.input.Component.Identifier.Axis.Y,
    				gamePadBackForward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//move backward
    		im.associateAction(gpName, 
    				net.java.games.input.Component.Identifier.Axis.X,
    				gamePadRightLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//yaw
    		im.associateAction(gpName, 
    				net.java.games.input.Component.Identifier.Axis.RX,
    				gamePadYaw, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//pitch
    		im.associateAction(gpName, 
    				net.java.games.input.Component.Identifier.Axis.RY,
    				gamePadPitch, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
			
		//attach action objects to keyboard 
    	if(im.getKeyboardName() != null) {
    		String kbName = im.getKeyboardName();
    		
    		//w; move forward
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.W,
    				cameraForward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//s; move backward
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.S, 
    				cameraBackward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//a; move left
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.A, 
    				cameraLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//d; move right
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.D, 
    				cameraRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//left key; yaw left
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.LEFT,
    				yawCameraLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//right key; yaw right
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.RIGHT,
    				yawCameraRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//up key; pitch up
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.UP,
    				pitchCameraUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//down key; pitch down
    		im.associateAction(kbName, 
    				net.java.games.input.Component.Identifier.Key.DOWN,
    				pitchCameraDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    		//space bar; offOn
    		//im.associateAction(kbName, 
    				//net.java.games.input.Component.Identifier.Key.SPACE,
    				//offOnDolphin, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	}
	}
	
	/*---------------------------------------------------------*/
	/*-----Create the Entities---*/
	/*---------------------------------------------------------*/
	private void makeEntities(Engine eng) throws IOException {
		
		//Set up X Axis
		ManualObject x = makeX(eng, sm);
		SceneNode xNode = sm.getRootSceneNode().createChildSceneNode("XNode");
		xNode.attachObject(x);
		
		//Set up Y Axis
		ManualObject y = makeY(eng, sm);
		SceneNode yNode = sm.getRootSceneNode().createChildSceneNode("YNode");
		yNode.attachObject(y);
		
		//set up Z Axis
		ManualObject z = makeZ(eng, sm);
		SceneNode zNode = sm.getRootSceneNode().createChildSceneNode("ZNode");
		zNode.attachObject(z);
		
		/**MODEL**/
		//--------------First Dolphin-------------
        //Create dolphin entity
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);        
        //create dolphin node
        dolphinNode = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphinNode.attachObject(dolphinE);
        dolphinNode.setLocalPosition(1.0f, 0.5f, -1.4f);       
		dolphinNode.yaw(Degreef.createFrom(45.0f));		
	}
	
	/*-------------------------------*/
	/*--------MANUAL OBJECTS---------*/
	/*------------------------------**/
	
	/*Create X axis line*/
	protected ManualObject makeX(Engine eng, SceneManager sm) throws IOException {
		//Make X axes vertices
		float[] verticesX = new float[] {
				-500.0f, 0.0f, 0.0f,
				500.0f, 0.0f, 0.0f,
		};
		
		//Make indices
		int[] indicesX = new int[] {0,1};
		
		//Create the manual object
		ManualObject x = sm.createManualObject("X");
		ManualObjectSection xSec = x.createManualSection("XSection");
		x.setGpuShaderProgram(sm.getRenderSystem().
				getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		xSec.setPrimitive(Primitive.LINES);

		//create buffers
		FloatBuffer vertBufX = BufferUtil.directFloatBuffer(verticesX);
		IntBuffer indexBufX = BufferUtil.directIntBuffer(indicesX);

		//set the buffers
		xSec.setVertexBuffer(vertBufX);
		xSec.setIndexBuffer(indexBufX);
		
		//Get material
	    Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.RED);
	    
	    //Get Texture
	    Texture tex = eng.getTextureManager().getAssetByPath("bright-red.jpeg");
	    TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    
	    //Set texture
	    tstate.setTexture(tex);
	    x.setRenderState(tstate);
	    x.setMaterial(mat);    
		x.setDataSource(DataSource.INDEX_BUFFER);
		return x;		
	}
	
	/*Create Y Axis Line*/
	protected ManualObject makeY(Engine eng, SceneManager sm) throws IOException {
		//Make Y axes vertices
		float[] verticesY = new float[] {
				0.0f, -500.0f, 0.0f,
				0.0f, 500.0f, 0.0f,
		};
		
		//Make indices
		int[] indicesY = new int[] {0,1};
		
		//Create the manual object
		ManualObject y = sm.createManualObject("Y");
		ManualObjectSection ySec = y.createManualSection("YSection");
		y.setGpuShaderProgram(sm.getRenderSystem().
				getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		ySec.setPrimitive(Primitive.LINES);

		//create buffers
		FloatBuffer vertBufY = BufferUtil.directFloatBuffer(verticesY);
		IntBuffer indexBufY = BufferUtil.directIntBuffer(indicesY);

		//set the buffers
		ySec.setVertexBuffer(vertBufY);
		ySec.setIndexBuffer(indexBufY);
		
		//Get material
	    Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.RED);
	    
	    //Get Texture
	    Texture tex = eng.getTextureManager().getAssetByPath("bright-green.jpeg");
	    TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    
	    //Set texture
	    tstate.setTexture(tex);
	    y.setRenderState(tstate);
	    y.setMaterial(mat);    
		y.setDataSource(DataSource.INDEX_BUFFER);
		return y;		
	}
	
	/*Create Z Axis Line*/
	protected ManualObject makeZ(Engine eng, SceneManager sm) throws IOException {
		//Make Z axes vertices
		float[] verticesZ = new float[] {
				0.0f, 0.0f, -500.0f,
				0.0f, 0.0f, 500.0f,
		};
		
		//Make indices
		int[] indicesZ = new int[] {0,1};
		
		//Create the manual object
		ManualObject z = sm.createManualObject("Z");
		ManualObjectSection zSec = z.createManualSection("ZSection");
		z.setGpuShaderProgram(sm.getRenderSystem().
				getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		zSec.setPrimitive(Primitive.LINES);

		//create buffers
		FloatBuffer vertBufZ = BufferUtil.directFloatBuffer(verticesZ);
		IntBuffer indexBufZ = BufferUtil.directIntBuffer(indicesZ);

		//set the buffers
		zSec.setVertexBuffer(vertBufZ);
		zSec.setIndexBuffer(indexBufZ);
		
		//Get material
	    Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
	    mat.setEmissive(Color.GREEN);
	    
	    //Get Texture
	    Texture tex = eng.getTextureManager().getAssetByPath("bright-blue.jpeg");
	    TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
	    
	    //Set texture
	    tstate.setTexture(tex);
	    z.setRenderState(tstate);
	    z.setMaterial(mat);    
		z.setDataSource(DataSource.INDEX_BUFFER);
		return z;		
	}
	
	
	//Start Game
	public static void main(String[] args) throws IOException {

        Scanner r = new Scanner(System.in);
		System.out.println("Are you the host? (Enter y/n)");
		String response = r.nextLine();
		int serverPort = 8065;
		
		//If you are host use your own ip address
		if(response.charAt(0) == 'y')
		{
			System.out.println("You are the host");
			GameServerUDP server = new GameServerUDP(serverPort);
			server.getLocalInetAddress();
			System.out.println("The server connection info is " + server.getLocalInetAddress() + ":" + serverPort);
			System.out.println("waiting for client connection...");

			String[] msgTokens = server.getLocalInetAddress().toString().split("/");
			System.out.println(msgTokens[1]);
			MyGame game = new MyGame(msgTokens[1], serverPort);
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
		
		//If you are joining enter in ip address
		else
		{
			System.out.println("Enter host server's IP address:");
			String serverIP = r.nextLine();
			System.out.println("Joining server " + serverIP + ":" + serverPort);
			MyGame game = new MyGame(serverIP, serverPort);
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
		
		while(it.hasNext()) { 
			sm.destroySceneNode(it.next().toString());
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
			gameClient.sendJoinMessage();
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
	
	/*-----------------------
	 * Add a new ghost avatar
	 ----------------------*/
	public void addGhostAvatarToGameWorld(GhostAvatar avatar)
			throws IOException {
		if (avatar != null) { 
			Entity ghostE = sm.createEntity("ghosts", "dolphinHighPoly.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);
			SceneNode ghostN = sm.getRootSceneNode().createChildSceneNode(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(1.0f, 1.0f, 1.0f);
			avatar.setNode(ghostN);
			avatar.setEntity(ghostE);
			//avatar.setPosition(node’s position... maybe redundant);
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
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction { // for leaving the game... need to attach to an input device
		@Override
		public void performAction(float time, Event evt) { 
			if(gameClient != null && isClientConnected == true) { 
				gameClient.sendByeMessage();
			} 
		}
	}
}
