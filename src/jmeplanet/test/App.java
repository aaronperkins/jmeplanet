package jmeplanet.test;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
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
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

import jmeplanet.Planet;
import jmeplanet.FractalDataSource;
import jmeplanet.PlanetAppState;
 
public class App extends SimpleApplication {
    
    private PlanetAppState planetAppState;
    private Geometry mark;
    
    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        App app = new App();
        
        app.setSettings(settings);
        app.showSettings = true;
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
        this.getCamera().setFrustumNear(1.0f);
        
        // In orbit
        this.getCamera().setLocation(new Vector3f(0f, 0f, 180000f));
        
        // On surface
        //this.getCamera().setLocation(new Vector3f(-6657.5254f, 27401.822f, 57199.777f));
        //this.getCamera().lookAtDirection(new Vector3f(0.06276598f, 0.94458306f, -0.3222158f), Vector3f.UNIT_Y);
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(new ColorRGBA(0.45f,0.5f,0.6f,1.0f));
        sun.setDirection(new Vector3f(1f, -1f, 0f));
        rootNode.addLight(sun);
        
        // Add sky
        Node sceneNode = new Node("Scene");
        sceneNode.attachChild(Utility.createSkyBox(this.getAssetManager(), "Textures/blue-glow-1024.dds"));
        rootNode.attachChild(sceneNode);
        
        // Create collision test mark
        Sphere sphere = new Sphere(30, 30, 5f);
        mark = new Geometry("mark", sphere);
        Material mark_mat = new Material(assetManager, "MatDefs/LogarithmicDepthBufferSimple.j3md");
        mark_mat.setBoolean("LogarithmicDepthBuffer", true);
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
        
        // Add planet app state
        planetAppState = new PlanetAppState();
        stateManager.attach(planetAppState);
        
        // Add planet
        Planet planet = createEarthLikePlanet(63710.0f, 800f, 4);
        planetAppState.addPlanet(planet);
        rootNode.attachChild(planet);
        
        // Add moon
        Planet moon = createMoonLikePlanet(20000, 300, 5);
        planetAppState.addPlanet(moon);
        rootNode.attachChild(moon);
        moon.setLocalTranslation(-150000f, 0f, 0f);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // slow camera down as we approach a planet
        Planet planet = planetAppState.getClosestPlanet();
        if (planet != null && planet.getPlanetToCamera() != null) {
            //System.out.println(planet.getName() + ": " + planet.getDistanceToCamera());
            this.getFlyByCamera().setMoveSpeed(
                    FastMath.clamp(planet.getDistanceToCamera(), 5, 100000));
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

            if (name.equals("COLLISION_TEST") && !pressed) {
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                
                // Test collision with closest planet's terrain only
                planetAppState.getClosestPlanet().getTerrainNode().collideWith(ray, results);

                System.out.println("----- Collisions? " + results.size() + "-----");
                for (int i = 0; i < results.size(); i++) {
                  // For each hit, we know distance, impact point, name of geometry.
                  float dist = results.getCollision(i).getDistance();
                  Vector3f pt = results.getCollision(i).getContactPoint();
                  String hit = results.getCollision(i).getGeometry().getName();
                  System.out.println("* Collision #" + i);
                  System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                }

                if (results.size() > 0) {
                  // The closest collision point is what was truly hit:
                  CollisionResult closest = results.getClosestCollision();
                  // Let's interact - we mark the hit with a red dot.
                  mark.setLocalTranslation(closest.getContactPoint());
                  rootNode.attachChild(mark);
                } else {
                  // No hits? Then remove the red mark.
                  rootNode.detachChild(mark);
                }
            }  
             
        }
    }; 
    
    private Planet createEarthLikePlanet(float radius, float heightScale, int seed) {
        boolean logarithmicDepthBuffer = true;
        
        // Prepare planet material
        Material planetMaterial = new Material(this.assetManager, "MatDefs/PlanetTerrain.j3md");
        
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
        
        //planetMaterial = new Material(this.assetManager, "MatDefs/LogarithmicDepthBufferSimple.j3md");
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
        //oceanMaterial = new Material(this.assetManager, "MatDefs/LogarithmicDepthBufferSimple.j3md");
        //oceanMaterial.setColor("Color", ColorRGBA.Blue);
        oceanMaterial.setBoolean("LogarithmicDepthBuffer", logarithmicDepthBuffer);
        planet.createOcean(oceanMaterial);
        
        // create atmosphere
        Material atmosphereMaterial = new Material(this.assetManager, "MatDefs/PlanetAtmosphere.j3md");
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
        Material planetMaterial = new Material(this.assetManager, "MatDefs/PlanetTerrain.j3md");
        
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