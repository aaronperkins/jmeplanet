package jmeplanet;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Planet extends Node {
    
    protected AssetManager assetManager;
    protected float baseRadius = 50.0f;
    protected float scalingFactor = 1f;
    protected int quads = 16;
    protected int minDepth = 0;
    protected int maxDepth = 10;
    protected Quad[] surfaceSide = new Quad[6];
    protected HeightDataSource dataSource;
    protected float heightScale;
    
    public Planet(AssetManager assetManager, float baseRadius, float heightScale) {
        
        this.assetManager = assetManager;
        this.baseRadius = baseRadius;
        this.heightScale = heightScale;
        
        dataSource = new FractalDataSource();
        dataSource.setHeightScale(this.heightScale);

        Vector3f rightMin = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f rightMax = new Vector3f(1.0f, -1.0f, -1.0f);
        surfaceSide[0] = new Quad(
                "SurfaceRight",
                this.assetManager,
                this,
                rightMin,
                rightMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
   
        Vector3f leftMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f leftMax = new Vector3f(-1.0f, -1.0f, 1.0f);
        surfaceSide[1] = new Quad(
                "SurfaceLeft",
                this.assetManager,
                this,
                leftMin,
                leftMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f topMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f topMax = new Vector3f(1.0f, 1.0f, 1.0f);
        surfaceSide[2] = new Quad(
                "SurfaceTop",
                this.assetManager,
                this,
                topMin,
                topMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f bottomMin = new Vector3f(-1.0f, -1.0f, 1.0f);
        Vector3f bottomMax = new Vector3f(1.0f, -1.0f, -1.0f);
        surfaceSide[3] = new Quad(
                "SurfaceBottom",
                this.assetManager,
                this,
                bottomMin,
                bottomMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f frontMin = new Vector3f(-1.0f, 1.0f, 1.0f);
        Vector3f frontMax = new Vector3f(1.0f, -1.0f, 1.0f);
        surfaceSide[4] = new Quad(
                "SurfaceFront",
                this.assetManager,
                this,
                frontMin,
                frontMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
        
        Vector3f backMin = new Vector3f(1.0f, 1.0f, -1.0f);
        Vector3f backMax = new Vector3f(-1.0f, -1.0f, -1.0f);
        surfaceSide[5] = new Quad(
                "SurfaceBack",
                this.assetManager,
                this,
                backMin,
                backMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
        
    }
    
    public void setCameraPosition(Vector3f position) {
        
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null)
                surfaceSide[i].setCameraPosition(position);
        }
        
    }
    
    public float getRadius() {
        return this.baseRadius;
    }
    
    public float getHeightScale() {
        return this.heightScale;
    }
     
}
