/*
Copyright (c) 2012 Aaron Perkins

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package jmeplanet.test;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.material.Material;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;

import jmeplanet.Planet;
import jmeplanet.FractalDataSource;
import jmeplanet.PlanetAppState;

/**
 * PlanetPhysicsTest
 * 
 */
public class PlanetPhysicsTest extends SimpleApplication {
    
    private BulletAppState bulletAppState;
    private PlanetAppState planetAppState;
    private CameraNode cameraNode;
    private RigidBodyControl cameraNodePhysicsControl;
    
    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        PlanetPhysicsTest app = new PlanetPhysicsTest();
        
        app.setSettings(settings);
        app.showSettings = false;
        app.start();
    }
    
    public PlanetPhysicsTest() {
        super( new StatsAppState(), new DebugKeysAppState() );
    }
 
    @Override
    public void simpleInitApp() {
        
        // Only show severe errors in log
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // setup physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
        
        // setup input
        setupInput();
        
        // Setup camera
        this.getCamera().setFrustumFar(10000000f);
        this.getCamera().setFrustumNear(1.0f);
        
        // setup camera and camera node
        CameraControl cameraControl = new CameraControl(this.getCamera(),ControlDirection.SpatialToCamera);
        cameraNode = new CameraNode("Camera", cameraControl);
        cameraNode.setLocalTranslation(new Vector3f(0f, 0f, 180000f));
        cameraNode.rotate(0, FastMath.PI, 0);
        cameraNodePhysicsControl = new RigidBodyControl(new BoxCollisionShape(new Vector3f(5f, 5f, 5f)), 1f);
        cameraNode.addControl(cameraNodePhysicsControl);
        rootNode.attachChild(cameraNode);
        bulletAppState.getPhysicsSpace().add(cameraNode);
        cameraNodePhysicsControl.setLinearDamping(0.8f);
        cameraNodePhysicsControl.setAngularDamping(0.99f);
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(new ColorRGBA(0.45f,0.5f,0.6f,1.0f));
        sun.setDirection(new Vector3f(1f, -1f, 0f));
        rootNode.addLight(sun);
        
        // Add sky
        Node sceneNode = new Node("Scene");
        sceneNode.attachChild(Utility.createSkyBox(this.getAssetManager(), "Textures/blue-glow-1024.dds"));
        rootNode.attachChild(sceneNode);
        
        // Add planet app state
        planetAppState = new PlanetAppState();
        stateManager.attach(planetAppState);
        
        // Add planet
        Planet planet = createEarthLikePlanet(63710.0f, 800f, 4);
        planet.addControl(new RigidBodyControl(new SphereCollisionShape(planet.getRadius() + 10f), 0f));
        planetAppState.addPlanet(planet);
        rootNode.attachChild(planet);
        bulletAppState.getPhysicsSpace().add(planet);
        
        // Add moon
        Planet moon = createMoonLikePlanet(20000, 300, 5);
        moon.setLocalTranslation(-150000f, 0f, 0f);
        moon.addControl(new RigidBodyControl(new SphereCollisionShape(moon.getRadius() + 10f), 0f));
        planetAppState.addPlanet(moon);
        rootNode.attachChild(moon);
        bulletAppState.getPhysicsSpace().add(moon);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        Planet planet = planetAppState.getNearestPlanet();
        if (planet != null && planet.getPlanetToCamera() != null) {
            cameraNodePhysicsControl.setGravity(planet.getPlanetToCamera().normalize().mult(-100f));
            //System.out.println(planet.getName() + ": " + planet.getPlanetToCamera());
            //System.out.println("Camera Speed: " + cameraNodePhysicsControl.getLinearVelocity());
        }   
    }
    
    private void setupInput() {
        // Toggle mouse cursor
        inputManager.addMapping("TOGGLE_CURSOR", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        // Toggle wireframe
        inputManager.addMapping("TOGGLE_WIREFRAME", 
            new KeyTrigger(KeyInput.KEY_T));
        // Movement keys
        inputManager.addMapping("RotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true),
                                               new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("RotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, false),
                                                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("RotateDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false),
                                             new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("RotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true),
                                               new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        
        inputManager.addListener(actionListener, "TOGGLE_WIREFRAME", "TOGGLE_CURSOR");
        inputManager.addListener(analogListener, "MoveLeft","MoveRight","MoveForward","MoveBackward","RotateLeft","RotateRight","RotateUp","RotateDown" );
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
                        
            if (name.equals("TOGGLE_CURSOR") && !pressed) {
                if (inputManager.isCursorVisible()) {
                    inputManager.setCursorVisible(false);
                } else {
                    inputManager.setCursorVisible(true);
                }
            }

            if (name.equals("TOGGLE_WIREFRAME") && !pressed) {
                for (Planet planet: planetAppState.getPlanets()) {
                    planet.toogleWireframe();
                }
            }
            
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            
            float linearSpeed = 10000f;
            float angularSpeed = 50f;
            tpf = 1;

            if (name.equals("MoveLeft"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getLeft().mult(linearSpeed * tpf));
            if (name.equals("MoveRight"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getLeft().mult(-linearSpeed * tpf));
            if (name.equals("MoveForward"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getDirection().mult(linearSpeed * tpf));
            if (name.equals("MoveBackward"))
                cameraNodePhysicsControl.applyCentralForce(getCamera().getDirection().mult(-linearSpeed * tpf));
            
            Vector3f xRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(0).normalize();
            Vector3f yRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(1).normalize();
            Vector3f zRotation = cameraNodePhysicsControl.getPhysicsRotation().getRotationColumn(2).normalize();

            if (name.equals("RotateLeft"))
                cameraNodePhysicsControl.applyTorque(yRotation.mult(angularSpeed * tpf));
            if (name.equals("RotateRight"))
                cameraNodePhysicsControl.applyTorque(yRotation.mult(-angularSpeed * tpf));
            if (name.equals("RotateUp"))
                cameraNodePhysicsControl.applyTorque(xRotation.mult(angularSpeed * tpf));
            if (name.equals("RotateDown"))
                cameraNodePhysicsControl.applyTorque(xRotation.mult(-angularSpeed * tpf));

        }   
    };
    
    private Planet createEarthLikePlanet(float radius, float heightScale, int seed) {
        boolean logarithmicDepthBuffer = true;
        
        // Prepare planet material
        Material planetMaterial = new Material(this.assetManager, "MatDefs/Planet/Terrain.j3md");
        
        // shore texture
        Texture dirt = this.assetManager.loadTexture("Textures/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region1ColorMap", dirt);
        planetMaterial.setVector3("Region1", new Vector3f(0, heightScale * 0.2f, 0));
        // grass texture
        Texture grass = this.assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region2ColorMap", grass);
        planetMaterial.setVector3("Region2", new Vector3f(heightScale * 0.16f, heightScale * 0.88f, 0));
        // rock texture
        Texture rock = this.assetManager.loadTexture("Textures/rock.jpg");
        rock.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region3ColorMap", rock);
        planetMaterial.setVector3("Region3", new Vector3f(heightScale * 0.84f, heightScale * 1.36f, 0));
        // snow texture
        Texture snow = this.assetManager.loadTexture("Textures/snow.jpg");
        snow.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region4ColorMap", snow);
        planetMaterial.setVector3("Region4", new Vector3f(heightScale * 0.94f, heightScale * 1.5f, 0));
        
        //planetMaterial = new Material(this.assetManager, "MatDefs/Planet/LogarithmicDepthBufferSimple.j3md");
        //planetMaterial.setColor("Color", ColorRGBA.Green);

        // Turn on Logarithmic Depth Buffer to avoid z-fighting
        planetMaterial.setBoolean("LogarithmicDepthBuffer", logarithmicDepthBuffer);
        
         // Create height data source
        FractalDataSource dataSource = new FractalDataSource(seed);
        dataSource.setHeightScale(heightScale);
        
        // create planet
        Planet planet = new Planet("Planet", radius, planetMaterial, dataSource);
        
        // create ocean
        Material oceanMaterial = assetManager.loadMaterial("Materials/Ocean.j3m");
        //oceanMaterial = new Material(this.assetManager, "MatDefs/Planet/LogarithmicDepthBufferSimple.j3md");
        //oceanMaterial.setColor("Color", ColorRGBA.Blue);
        oceanMaterial.setBoolean("LogarithmicDepthBuffer", logarithmicDepthBuffer);
        planet.createOcean(oceanMaterial);
        
        // create atmosphere
        Material atmosphereMaterial = new Material(this.assetManager, "MatDefs/Planet/Atmosphere.j3md");
        float atmosphereRadius = radius + (radius * .05f);
        atmosphereMaterial.setColor("Ambient", new ColorRGBA(0.5f,0.5f,1f,1f));
        atmosphereMaterial.setColor("Diffuse", new ColorRGBA(0.5f,0.5f,1f,1f));
        atmosphereMaterial.setColor("Specular", new ColorRGBA(0.7f,0.7f,1f,1f));
        atmosphereMaterial.setFloat("Shininess", 3.0f);
        atmosphereMaterial.setBoolean("LogarithmicDepthBuffer", logarithmicDepthBuffer);
        

        planet.createAtmosphere(atmosphereMaterial, atmosphereRadius);

        return planet;
    }
    
    private Planet createMoonLikePlanet(float radius, float heightScale, int seed) {
        // Prepare planet material
        Material planetMaterial = new Material(this.assetManager, "MatDefs/Planet/Terrain.j3md");
        
        // region1 texture
        Texture region1 = this.assetManager.loadTexture("Textures/moon.jpg");
        region1.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region1ColorMap", region1);
        planetMaterial.setVector3("Region1", new Vector3f(0, heightScale * 0.2f, 0));
        // region2 texture
        Texture region2 = this.assetManager.loadTexture("Textures/moon.jpg");
        region2.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region2ColorMap", region2);
        planetMaterial.setVector3("Region2", new Vector3f(heightScale * 0.16f, heightScale * 0.88f, 0));
        // region3 texture
        Texture region3 = this.assetManager.loadTexture("Textures/rock.jpg");
        region3.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region3ColorMap", region3);
        planetMaterial.setVector3("Region3", new Vector3f(heightScale * 0.84f, heightScale * 1.36f, 0));
        // region4 texture
        Texture region4 = this.assetManager.loadTexture("Textures/rock.jpg");
        region4.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("Region4ColorMap", region4);
        planetMaterial.setVector3("Region4", new Vector3f(heightScale * 0.94f, heightScale * 1.5f, 0));

        // Turn on Logarithmic Depth Buffer to avoid z-fighting
        planetMaterial.setBoolean("LogarithmicDepthBuffer", true);
        
         // Create height data source
        FractalDataSource dataSource = new FractalDataSource(seed);
        dataSource.setHeightScale(heightScale);
        
        // create planet
        Planet planet = new Planet("Moon", radius, planetMaterial, dataSource);
        
        return planet;
    }
    

    
}