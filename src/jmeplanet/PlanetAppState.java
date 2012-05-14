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
package jmeplanet;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * PlanetAppState
 * 
 */
public class PlanetAppState extends AbstractAppState {
    
    protected Application app;
    protected List<Planet> planets;
    protected FilterPostProcessor fpp;
    protected PlanetFogFilter fog;
    
    public PlanetAppState() {
        this.planets = new ArrayList<Planet>(); 
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = app;
        
        fpp=new FilterPostProcessor(app.getAssetManager());
        app.getViewPort().addProcessor(fpp);
        
        fog= new PlanetFogFilter();
        fpp.addFilter(fog);

        BloomFilter bloom=new BloomFilter();
        bloom.setDownSamplingFactor(2);
        bloom.setBlurScale(1.37f);
        bloom.setExposurePower(3.30f);
        bloom.setExposureCutOff(0.1f);
        bloom.setBloomIntensity(1.45f);
        fpp.addFilter(bloom);
        
    }
            
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
    
    @Override
    public void update(float tpf) {
        for (Planet planet: this.planets ) {
            planet.setCameraPosition(this.app.getCamera().getLocation());
        }

        checkFog();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    public void addPlanet(Planet planet) {
        this.planets.add(planet);
    }
    
    public List<Planet> getPlanets() {
        return this.planets;
    }
    
    public Planet getClosestPlanet() {
        Planet cPlanet = null;
        for (Planet planet: this.planets ) {
            if (cPlanet == null || cPlanet.getDistanceToCamera() > planet.getDistanceToCamera()) {
                cPlanet = planet;
            }
        }
        return cPlanet;
    }
    
    protected void checkFog() {
        Planet planet = getClosestPlanet();
        if (planet.getAtmosphereNode() != null) {
            if (planet.getDistanceToCamera() < planet.getAtmosphereRadius() - planet.getRadius()) {
                //if (!fog.isEnabled()) {
                    fog.setFogColor(planet.getAtmosphereFogColor());
                    fog.setFogDistance(planet.getAtmosphereFogDistance());
                    fog.setFogDensity(planet.getAtmosphereFogDensity());
                    
                    if (planet.getDistanceToCamera() <= 2f) {
                        fog.setFogColor(planet.getUnderwaterFogColor());
                        fog.setFogDistance(planet.getUnderwaterFogDistance());
                        fog.setFogDensity(planet.getUnderwaterFogDensity());                        
                    }
                    
                    fog.setEnabled(true);                    
                //}
            }
            else {
                //if (fog.isEnabled()) {
                    fog.setEnabled(false);
                //}
            }
        }
    }
  
}
