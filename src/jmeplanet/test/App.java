package jmeplanet.test;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.material.Material;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;

import jmeplanet.Planet;
import jmeplanet.FractalDataSource;
import jmeplanet.PlanetAppState;
 
public class App extends SimpleApplication {
    
    private PlanetAppState planetAppState;
    
    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        App app = new App();
        
        app.setSettings(settings);
        app.showSettings = false;
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
        // Only show severe errors in log
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // Toggle mouse cursor
        inputManager.addMapping("TOGGLE_CURSOR", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "TOGGLE_CURSOR"); 
        // Toggle wireframe
        inputManager.addMapping("TOGGLE_WIREFRAME", 
            new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(actionListener, "TOGGLE_WIREFRAME");
        // Collision test
        inputManager.addMapping("COLLISION_TEST", 
            new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(actionListener, "COLLISION_TEST"); 
        
        // Setup camera
        this.getCamera().setFrustumFar(10000000f);
        this.getCamera().setFrustumNear(1f);
        this.getCamera().setLocation(new Vector3f(0f, 0f, 100000f));
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);
        
        // Add sky
        Node sceneNode = new Node("Scene");
        sceneNode.attachChild(Utility.createSkyBox(this.getAssetManager(), "Textures/blue-glow-1024.dds"));
        rootNode.attachChild(sceneNode);
        
        // Add planet app state
        planetAppState = new PlanetAppState();
        stateManager.attach(planetAppState);
        
        // Add planet
        Planet planet = createEarthLikePlanet(63710.0f, 400f, 4);
        planetAppState.addPlanet(planet);
        rootNode.attachChild(planet);
        planet.setLocalTranslation(0f, 0f, 0f);
        
        // Add moon
        Planet moon = createMoonLikePlanet(20000, 200, 5);
        planetAppState.addPlanet(moon);
        rootNode.attachChild(moon);
        moon.setLocalTranslation(-150000f, 0f, 0f);

    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // slow camera down as we approach a planet
        Planet planet = planetAppState.getClosestPlanet();
        if (planet != null && planet.getPlanetToCamera() != null) {
            float cameraHeight = planet.getPlanetToCamera().length();
            this.getFlyByCamera().setMoveSpeed(
                    FastMath.clamp(cameraHeight - planet.getRadius(), 5, 100000));
        }
        
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
    
    private Planet createEarthLikePlanet(float radius, float heightScale, int seed) {
         // planet material
        Material planetMaterial = new Material(this.assetManager, "MatDefs/PlanetTerrain.j3md");
        
         // shore texture
        Texture dirt = this.assetManager.loadTexture("Textures/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region1ColorMap", dirt);
        planetMaterial.setVector3("region1", new Vector3f(0, heightScale * 0.2f, 0));
        // grass texture
        Texture grass = this.assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region2ColorMap", grass);
        planetMaterial.setVector3("region2", new Vector3f(heightScale * 0.16f, heightScale * 0.88f, 0));
        // rock texture
        Texture rock = this.assetManager.loadTexture("Textures/rock.jpg");
        rock.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region3ColorMap", rock);
        planetMaterial.setVector3("region3", new Vector3f(heightScale * 0.84f, heightScale * 1.36f, 0));
        // snow
        Texture snow = this.assetManager.loadTexture("Textures/snow.jpg");
        snow.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region4ColorMap", snow);
        planetMaterial.setVector3("region4", new Vector3f(heightScale * 0.94f, heightScale * 1.5f, 0));
        
        //planetMaterial = new Material(this.assetManager, "MatDefs/Test.j3md");
        //planetMaterial.setColor("Color", ColorRGBA.Green);

         // Create height data source
        FractalDataSource dataSource = new FractalDataSource(seed);
        dataSource.setHeightScale(heightScale);
        
        // create planet
        Planet planet = new Planet("Planet", radius, planetMaterial, dataSource);
        
        // create ocean
        Material oceanmat = assetManager.loadMaterial("Materials/Ocean.j3m");
        //Material oceanmat = new Material(this.assetManager, "MatDefs/Test.j3md");
        //oceanmat.setColor("Color", ColorRGBA.Blue);
        planet.createOcean(oceanmat);
        
        return planet;
    }
    
    private Planet createMoonLikePlanet(float radius, float heightScale, int seed) {
         // planet material
        Material planetMaterial = new Material(this.assetManager, "MatDefs/PlanetTerrain.j3md");
        
         // shore texture
        Texture dirt = this.assetManager.loadTexture("Textures/moon.jpg");
        dirt.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region1ColorMap", dirt);
        planetMaterial.setVector3("region1", new Vector3f(0, heightScale * 0.2f, 0));
        // grass texture
        Texture grass = this.assetManager.loadTexture("Textures/moon.jpg");
        grass.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region2ColorMap", grass);
        planetMaterial.setVector3("region2", new Vector3f(heightScale * 0.16f, heightScale * 0.88f, 0));
        // rock texture
        Texture rock = this.assetManager.loadTexture("Textures/rock.jpg");
        rock.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region3ColorMap", rock);
        planetMaterial.setVector3("region3", new Vector3f(heightScale * 0.84f, heightScale * 1.36f, 0));
        // snow
        Texture snow = this.assetManager.loadTexture("Textures/rock.jpg");
        snow.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region4ColorMap", snow);
        planetMaterial.setVector3("region4", new Vector3f(heightScale * 0.94f, heightScale * 1.5f, 0));

         // Create height data source
        FractalDataSource dataSource = new FractalDataSource(seed);
        dataSource.setHeightScale(heightScale);
        
        // create planet
        Planet planet = new Planet("Planet", radius, planetMaterial, dataSource);
        
        return planet;
    }
    

    
}