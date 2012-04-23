/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmeplanet;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aaron
 */
public class PlanetController extends AbstractControl {

    protected Planet planet;
    protected Camera camera;
    
    public PlanetController() {
    }

    public PlanetController(Planet planet, Camera camera) {
        this.planet = planet;
        this.camera = camera;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    @Override
    protected void controlUpdate(float tpf) {
        planet.setCameraPosition(camera.getLocation());
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
