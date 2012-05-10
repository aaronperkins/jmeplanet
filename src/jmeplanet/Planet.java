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

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * Quad
 * 
 * Credits
 * This code has been adapted from OgrePlanet
 * Copyright (c) 2010 Anders Lingfors
 * https://bitbucket.org/lingfors/ogreplanet/
 */
public class Planet extends Node {
    
    protected Material terrainMaterial;
    protected Material oceanMaterial;
    protected Material atmosphereMaterial;
    protected Node planetNode;
    protected Node terrainNode;
    protected Node oceanNode;
    protected Node atmosphereNode;
    protected float baseRadius;
    protected float atmosphereRadius;
    protected HeightDataSource dataSource;
    // Number of planer quads per patch. This value directly controls the 
    // complexity of the geometry generated.
    protected int quads = 32;
    // Minimal depth for spliting. The planet will start at this depth
    // no matter the distance from camera
    protected int minDepth = 0;
    // Max depth for splitting. The planet will only split down to this depth
    // no matter the distance from the camera
    protected int maxDepth = 10;
    protected Quad[] terrainSide = new Quad[6];
    protected Quad[] oceanSide = new Quad[6];
    protected boolean wireframeMode;
    protected boolean oceanFloorCulling;
    protected Vector3f planetToCamera;
    protected float distanceToCamera;
    
    /**
    * <code>Planet</code>
    * @param name Name of the node
    * @param baseRadius The radius of the planet
    * @param material The material applied to the planet
    * @param dataSource The <code>HeightDataSource</code> used for the terrain
    * @param quads Number of planer quads per patch.
    * @param minDepth Minimal depth for spliting.
    * @param maxDepth Max depth for splitting.
    * @return true if inside or intersecting camera frustum
    */
    public Planet(String name, float baseRadius, Material material, HeightDataSource dataSource, int quads, int minDepth, int maxDepth) {
        super(name);
        this.terrainMaterial = material;
        this.baseRadius = baseRadius;
        this.dataSource = dataSource;
        this.quads = quads;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        
        this.planetNode = new Node("PlanetNode");
        this.attachChild(planetNode);
        
        prepareTerrain();
    }
    
    /**
    * <code>Planet</code>
    * @param name Name of the node
    * @param baseRadius The radius of the planet
    * @param material The material applied to the planet
    * @param dataSource The <code>HeightDataSource</code> used for the terrain
    */
    public Planet(String name, float baseRadius, Material material, HeightDataSource dataSource) {
        super(name);
        this.terrainMaterial = material;
        this.baseRadius = baseRadius;
        this.dataSource = dataSource;
        
        this.planetNode = new Node("PlanetNode");
        this.attachChild(planetNode);
        
        prepareTerrain();
    }
    
    public void createOcean(Material material) {
        this.oceanMaterial = material;
        
        if (oceanNode == null)
            prepareOcean();
    }
    
    public void createAtmosphere(Material material, float atmosphereRadius) {
        this.atmosphereMaterial = material;
        this.atmosphereRadius = atmosphereRadius;
        
        if (atmosphereNode == null)
            prepareAtmosphere();
    }
    
    public void setCameraPosition(Vector3f position) {
        // get vector between planet and camera
        this.planetToCamera = position.subtract(this.getLocalTranslation());
        // get distance to surface
        this.distanceToCamera= this.planetToCamera.length() - this.baseRadius;
        
        // Update camera positions for all quads
        int currentTerrainMaxDepth = 0;
        for (int i = 0; i < 6; i++) {
            if (terrainSide[i] != null) {
                terrainSide[i].setCameraPosition(position);
                // get current max depth of quad for skirt toggling
                currentTerrainMaxDepth = Math.max(currentTerrainMaxDepth, terrainSide[i].getCurrentMaxDepth());                
            }
            if (oceanSide[i] != null) {
                oceanSide[i].setCameraPosition(position);
            }
        }
        
        boolean skirting;
        // Are we at minDepth?
        if (currentTerrainMaxDepth == this.minDepth ) {
            // Turn off skirting if entire terrain is at minDepth
            skirting = false;
        } else {
            //otherwise turn on skirting
            skirting = true;
        }
        // Go through and set skirting on all terrain quads
        for (int i = 0; i < 6; i++) {
            if (terrainSide[i] != null)
                terrainSide[i].setSkirting(skirting);
        }   
    }
    
    public Node getPlanetNode() {
        return this.planetNode;
    }
    
    public Node getTerrainNode() {
        return this.terrainNode;
    }
    
    public Node getOceanNode() {
        return this.oceanNode;
    }
    
    public Node getAtmosphereNode() {
        return this.atmosphereNode;
    }
    
    public float getRadius() {
        return this.baseRadius;
    }
    
    public float getHeightScale() {
        return dataSource.getHeightScale();
    }
    
    public Vector3f getPlanetToCamera() {
        return this.planetToCamera;
    }
    
    public float getDistanceToCamera() {
        return this.distanceToCamera;
    }
    
    public void toogleWireframe() {
        if (this.wireframeMode)
            wireframeMode = false;
        else
            wireframeMode = true;
        
        if (oceanNode != null)
            this.oceanMaterial.getAdditionalRenderState().setWireframe(wireframeMode);
        
        for (int i = 0; i < 6; i++) {
            if (terrainSide[i] != null)
                terrainSide[i].setWireframe(wireframeMode);
            if (oceanSide[i] != null)
                oceanSide[i].setWireframe(wireframeMode);
        }      
    }
    
    private void prepareTerrain() {

        this.terrainNode = new Node("TerrainNode");
        this.planetNode.attachChild(terrainNode);
        
        Vector3f rightMin = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f rightMax = new Vector3f(1.0f, -1.0f, -1.0f);
        terrainSide[0] = new Quad(
                "TerrainRight",
                this.terrainMaterial,
                this.terrainNode,
                rightMin,
                rightMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
   
        Vector3f leftMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f leftMax = new Vector3f(-1.0f, -1.0f, 1.0f);
        terrainSide[1] = new Quad(
                "TerrainLeft",
                this.terrainMaterial,
                this.terrainNode,
                leftMin,
                leftMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f topMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f topMax = new Vector3f(1.0f, 1.0f, 1.0f);
        terrainSide[2] = new Quad(
                "TerrainTop",
                this.terrainMaterial,
                this.terrainNode,
                topMin,
                topMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f bottomMin = new Vector3f(-1.0f, -1.0f, 1.0f);
        Vector3f bottomMax = new Vector3f(1.0f, -1.0f, -1.0f);
        terrainSide[3] = new Quad(
                "TerrainBottom",
                this.terrainMaterial,
                this.terrainNode,
                bottomMin,
                bottomMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
      
        Vector3f backMin = new Vector3f(1.0f, 1.0f, -1.0f);
        Vector3f backMax = new Vector3f(-1.0f, -1.0f, -1.0f);
        terrainSide[5] = new Quad(
                "TerrainBack",
                this.terrainMaterial,
                this.terrainNode,
                backMin,
                backMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
        
        Vector3f frontMin = new Vector3f(-1.0f, 1.0f, 1.0f);
        Vector3f frontMax = new Vector3f(1.0f, -1.0f, 1.0f);
        terrainSide[4] = new Quad(
                "TerrainFront",
                this.terrainMaterial,
                this.terrainNode,
                frontMin,
                frontMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);  
    }
 
    private void prepareOcean() {        
        this.oceanNode = new Node("OceanNode");
        planetNode.attachChild(oceanNode);
        
        int quads = this.quads;
        int minDepth = 0;
        int maxDepth = this.maxDepth;
        
        SimpleHeightDataSource dataSource = new SimpleHeightDataSource();
        
        Vector3f rightMin = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f rightMax = new Vector3f(1.0f, -1.0f, -1.0f);
        oceanSide[0] = new Quad(
                "OceanRight",
                this.oceanMaterial,
                this.oceanNode,
                rightMin,
                rightMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0);
   
        Vector3f leftMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f leftMax = new Vector3f(-1.0f, -1.0f, 1.0f);
        oceanSide[1] = new Quad(
                "OceanLeft",
                this.oceanMaterial,
                this.oceanNode,
                leftMin,
                leftMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0);

        Vector3f topMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f topMax = new Vector3f(1.0f, 1.0f, 1.0f);
        oceanSide[2] = new Quad(
                "OceanTop",
                this.oceanMaterial,
                this.oceanNode,
                topMin,
                topMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0);

        Vector3f bottomMin = new Vector3f(-1.0f, -1.0f, 1.0f);
        Vector3f bottomMax = new Vector3f(1.0f, -1.0f, -1.0f);
        oceanSide[3] = new Quad(
                "OceanBottom",
                this.oceanMaterial,
                this.oceanNode,
                bottomMin,
                bottomMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0);
      
        Vector3f backMin = new Vector3f(1.0f, 1.0f, -1.0f);
        Vector3f backMax = new Vector3f(-1.0f, -1.0f, -1.0f);
        oceanSide[5] = new Quad(
                "OceanBack",
                this.oceanMaterial,
                this.oceanNode,
                backMin,
                backMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0);
        
        Vector3f frontMin = new Vector3f(-1.0f, 1.0f, 1.0f);
        Vector3f frontMax = new Vector3f(1.0f, -1.0f, 1.0f);
        oceanSide[4] = new Quad(
                "OceanFront",
                this.oceanMaterial,
                this.oceanNode,
                frontMin,
                frontMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                dataSource,
                quads,
                0,
                minDepth,
                maxDepth,
                null,
                0); 
    } 
    
    private void prepareAtmosphere() {        
        this.atmosphereNode = new Node("AtmosphereNode");
        planetNode.attachChild(atmosphereNode);
        
        Mesh sphere = new Sphere(100, 100, this.atmosphereRadius, true, false);
        Geometry atmosphere = new Geometry("Atmosphere", sphere);
        
        atmosphere.setQueueBucket(Bucket.Transparent);
        
        atmosphere.setMaterial(this.atmosphereMaterial);
        //atmosphere.rotate( 0, FastMath.HALF_PI, 0);
        
        this.atmosphereNode.attachChild(atmosphere);  
    }
     
}
