package jmeplanet;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.material.Material;
import com.jme3.bounding.BoundingBox;
import com.jme3.terrain.heightmap.AbstractHeightMap;

public class Quad {
    
    protected String name;
    protected AssetManager assetManager;
    protected Vector3f min;
    protected Vector3f max;
    protected float texXMin;
    protected float texXMax;
    protected float texYMin;
    protected float texYMax;
    protected float baseRadius;
    protected float scalingFactor;
    protected HeightDataSource dataSource;
    protected int quads;
    protected int depth;
    protected int minDepth;
    protected int maxDepth;
    protected Quad parentQuad;
    protected Node parentNode;
    protected int position;
    protected Node quadNode;
    protected Geometry quadGeometry;
    protected Vector3f quadCenter;
    protected Patch patch;
    protected BoundingBox aabb;
    protected AbstractHeightMap heightMap;
    protected Quad[] subQuad = new Quad[4];
    
    public Quad(
            String name,
            AssetManager assetManager,
            Node parentNode,
            Vector3f min,
            Vector3f max,
            float texXMin,
            float texXMax,
            float texYMin,
            float texYMax,
            float baseRadius,
            float scalingFactor,
            HeightDataSource dataSource,
            int quads,
            int depth,
            int minDepth,
            int maxDepth,
            Quad parentQuad,
            int position) {
        
        this.name = name;
        this.assetManager = assetManager;
        this.min = min;
        this.max = max;
        this.texXMin = texXMin;
        this.texXMax = texXMax;
        this.texYMin = texYMin;
        this.texYMax = texYMax;
        this.baseRadius = baseRadius;
        this.scalingFactor = scalingFactor;
        this.dataSource = dataSource;
        this.quads = quads;
        this.depth = depth;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.parentQuad = parentQuad;
        this.position = position;
        this.parentNode = parentNode;
        
        this.patch = new Patch(
                this.quads,
                this.min,
                this.max,
                this.texXMin,
                this.texXMax,
                this.texYMin,
                this.texYMax,
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.position);
        
        this.patch.prepare();
        this.quadCenter = this.patch.getCenter();
        this.aabb = this.patch.getAABB();
    }
    
    public void setCameraPosition(Vector3f position) {

        for (int i = 0; i < 4; i++) {
            if (this.subQuad[i] != null) {
                this.subQuad[i].setCameraPosition(position);
            }
        }
        
        float distanceToEdge = this.aabb.distanceToEdge(position);
        float aabbLength = this.aabb.getExtent(null).length();
        
        if ((this.quadGeometry != null || 
                (this.subQuad[0] != null && 
                this.subQuad[1] != null && 
                this.subQuad[2] != null && 
                this.subQuad[3] != null)) &&
                (this.depth < this.minDepth || (this.depth < this.maxDepth && distanceToEdge < aabbLength)))
        {
            
            if ((this.subQuad[0] != null &&
                    this.subQuad[1] != null &&
                    this.subQuad[2] != null &&
                    this.subQuad[3] != null)) 
            {
                hide();              
            } else {
                prepareSubQuads();
            }  
            
        } else {
            
            if ((this.subQuad[0] == null || this.subQuad[0].isLeaf()) &&
                    (this.subQuad[1] == null || this.subQuad[1].isLeaf() ) &&
                    (this.subQuad[2] == null || this.subQuad[2].isLeaf() ) &&
                    (this.subQuad[3] == null || this.subQuad[3].isLeaf() ))
            {
                
                if (this.quadGeometry == null) {
                    show();
                }
                
                for (int i = 0; i < 4; i++) {
                    if (this.subQuad[i] != null) {
                        this.subQuad[i].hide();
                        this.subQuad[i] = null;
                    }
                } 
            }
            
        }
        
    }

    public void show()
    {
        if (this.quadGeometry == null) {
            System.out.println(this.name + " Show");
            this.quadGeometry = new Geometry(this.name + "Geometry", patch.getMesh());
            
            //Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");            
            //Material mat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
            Material mat = assetManager.loadMaterial("Materials/grass.j3m");
            //mat.setColor("Color", ColorRGBA.White);
            //mat.setBoolean("VertexColor", true);
            //mat.setBoolean("UseVertexColor", true);
            //mat.getAdditionalRenderState().setWireframe(true);
            this.quadGeometry.setMaterial(mat);
        }
        
        if (this.quadNode == null) {
            this.quadNode = new Node(this.name);
            this.parentNode.attachChild(this.quadNode);
            this.quadNode.setLocalTranslation(this.quadCenter);
        }
        
        if (this.quadGeometry.getParent() == null) {
           this.quadNode.attachChild(this.quadGeometry);
           this.aabb = (BoundingBox)this.quadNode.getWorldBound();
        }
        
    }
    
    public void hide()
    {
        if (this.quadGeometry != null) {
            System.out.println(this.name + " Hide");
            this.quadGeometry.removeFromParent();
            this.quadGeometry = null;
        }
        if (this.quadNode != null) {
            this.quadNode.removeFromParent();
            this.quadNode = null;
        }

    }
    
    public boolean isPrepared() {
        return patch.isPrepared();
    }
    
    public boolean isLeaf() {
        return (this.subQuad[0] == null && this.subQuad[1] == null && this.subQuad[2] == null && this.subQuad[3] == null);
    }
    
    protected void prepareSubQuads() {
        
        Vector3f center = new Vector3f(
                this.min.x + (this.max.x - this.min.x)/2,
                this.min.y + (this.max.y - this.min.y)/2,
                this.min.z + (this.max.z - this.min.z)/2);

        Vector3f topCenter = new Vector3f();
        Vector3f bottomCenter = new Vector3f();
        Vector3f leftCenter = new Vector3f();
        Vector3f rightCenter = new Vector3f();

        if (this.min.x == this.max.x)
        {
            // This quad is perpendicular to the x axis
            // (right/left patches)
            topCenter = new Vector3f(this.min.x, this.min.y, center.z);
            bottomCenter = new Vector3f(this.max.x, this.max.y, center.z);
            leftCenter = new Vector3f(this.min.x, center.y, this.min.z);
            rightCenter = new Vector3f(this.max.x, center.y, this.max.z);
        }
        else if (this.min.y == this.max.y)
        {
            // This quad is perpendicular to the y axis
            // (top/bottom patches)
            topCenter = new Vector3f(center.x, this.min.y, this.min.z);
            bottomCenter = new Vector3f(center.x, this.max.y, this.max.z);
            leftCenter = new Vector3f(this.min.x, this.min.y, center.z);
            rightCenter = new Vector3f(this.max.x, this.max.y, center.z);
        }
        else if (this.min.z == this.max.z)
        {
            // This quad is perpendicular to the z axis
            // (front/back patches)
            topCenter = new Vector3f(center.x, this.min.y, this.min.z);
            bottomCenter = new Vector3f(center.x, this.max.y, this.max.z);
            leftCenter = new Vector3f(this.min.x, center.y, this.min.z);
            rightCenter = new Vector3f(this.max.x, center.y, this.max.z);
        }
        else
        {
                //assert(false);
        }
        
        if (this.subQuad[0] == null)
        {
            // "Upper left" quad
            this.subQuad[0] = new Quad(
                    this.name + "0",
                    this.assetManager,
                    this.parentNode,
                    this.min,
                    center,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texXMin,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texXMin + (this.texXMax - this.texXMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texYMin,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texYMin + (this.texYMax - this.texYMin) / 2.0f,
                    this.baseRadius,
                    this.scalingFactor,
                    this.dataSource,
                    this.quads,
                    this.depth+1,
                    this.minDepth,
                    this.maxDepth,
                    this,
                    0);
        }

        if (this.subQuad[1] == null)
        {
            // "Upper right" quad
            this.subQuad[1] = new Quad(
                    this.name + "1",
                    this.assetManager,
                    this.parentNode,
                    topCenter,
                    rightCenter,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texXMin + (this.texXMax - this.texXMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texXMax,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texYMin,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texYMin + (this.texYMax - this.texYMin) / 2.0f,
                    this.baseRadius,
                    this.scalingFactor,
                    this.dataSource,
                    this.quads,
                    this.depth+1,
                    this.minDepth,
                    this.maxDepth,
                    this,
                    1);
        }

        if (this.subQuad[2] == null)
        {
            // "Lower left" quad
            this.subQuad[2] = new Quad(
                    this.name + "2",
                    this.assetManager,
                    this.parentNode,
                    leftCenter,
                    bottomCenter,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texXMin,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texXMin + (this.texXMax - this.texXMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texYMin + (this.texYMax - this.texYMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texYMax,
                    this.baseRadius,
                    this.scalingFactor,
                    this.dataSource,
                    this.quads,
                    this.depth+1,
                    this.minDepth,
                    this.maxDepth,
                    this,
                    2);
        }

        if (this.subQuad[3] == null)
        {
            // "Lower right" quad
            this.subQuad[3] = new Quad(
                    this.name + "3",
                    this.assetManager,
                    this.parentNode,
                    center,
                    this.max,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texXMin + (this.texXMax - this.texXMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texXMax,
                    (this.depth < this.maxDepth - 9) ? 0f : this.texYMin + (this.texYMax - this.texYMin) / 2.0f,
                    (this.depth < this.maxDepth - 9) ? FastMath.pow(2.0f, this.maxDepth - this.depth - 1.0f) : this.texYMax,
                    this.baseRadius,
                    this.scalingFactor,
                    this.dataSource,
                    this.quads,
                    this.depth+1,
                    this.minDepth,
                    this.maxDepth,
                    this,
                    3);
        }
    }

}
