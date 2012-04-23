/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmeplanet;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.bounding.BoundingBox;
import com.jme3.terrain.heightmap.AbstractHeightMap;

import java.util.List;
import java.util.ArrayList;
import java.nio.IntBuffer;
import java.util.SortedSet;

/**
 *
 * @author aaron
 */
public class Quad {
    
    protected String name;
    protected SortedSet<String> quadNameSet;
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
    protected String leftNeighbour;
    protected String rightNeighbour;
    protected String upNeighbour;
    protected String downNeighbour;
    
    public Quad(
            String name,
            SortedSet<String> quadNameSet,
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
        this.quadNameSet = quadNameSet;
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
        if (this.depth == 0)
                show();
        
        if (this.parentNode != null) {
                leftNeighbour = getLeftNeighbourName(this.name);
                if (this.name.contains("Top") && !leftNeighbour.contains("Top")) {
                        leftNeighbour = rotateCCW(leftNeighbour);
                } else if (this.name.contains("Bottom") && !leftNeighbour.contains("Bottom")) {
                        leftNeighbour = rotateCW(leftNeighbour);
                }

                rightNeighbour = getRightNeighbourName(this.name);
                if (this.name.contains("Top") && !this.rightNeighbour.contains("Top")) {
                        rightNeighbour = rotateCW(rightNeighbour);
                } else if (this.name.contains("Bottom") && !this.rightNeighbour.contains("Bottom")) {
                        rightNeighbour = rotateCCW(rightNeighbour);
                }

                upNeighbour = getUpNeighbourName(this.name);
                if (this.name.contains("Top") && !this.upNeighbour.contains("Top")) {
                        upNeighbour = rotate180(upNeighbour);
                } else if (this.name.contains("Left") && !this.upNeighbour.contains("Left")) {
                        upNeighbour = rotateCW(upNeighbour);
                } else if (this.name.contains("Right") && !this.upNeighbour.contains("Right")) {
                        upNeighbour = rotateCCW(upNeighbour);
                } else if (this.name.contains("Back") && !this.upNeighbour.contains("Back")) {
                        upNeighbour = rotate180(upNeighbour);
                }

                downNeighbour = getDownNeighbourName(this.name);
                if (this.name.contains("Bottom") && !this.downNeighbour.contains("Bottom")) {
                        downNeighbour = rotate180(downNeighbour);
                } else if (this.name.contains("Left") && !this.downNeighbour.contains("Left")) {
                        downNeighbour = rotateCCW(downNeighbour);
                } else if (this.name.contains("Right") && !this.downNeighbour.contains("Right")) {
                        downNeighbour = rotateCW(downNeighbour);
                } else if (this.name.contains("Back") && !this.downNeighbour.contains("Back")) {
                        downNeighbour = rotate180(downNeighbour);
                }
        }
                     
    }
    
    public void setCameraPosition(Vector3f position) {

        for (int i = 0; i < 4; i++) {
            if (this.subQuad[i] != null) {
                this.subQuad[i].setCameraPosition(position);
            }
        }
        
        if (this.quadNode != null) {
            this.aabb = (BoundingBox)this.quadNode.getWorldBound();
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
                
                if (this.parentNode != null &&
                        (!quadNameSet.contains(leftNeighbour) ||
                        !quadNameSet.contains(rightNeighbour) ||
                        !quadNameSet.contains(upNeighbour) ||
                        !quadNameSet.contains(downNeighbour)))
                {
                        // Don't show children if it would cause a crack
                        //return;
                }
                
                
                
                // We are showing, but we are too close and our subPatches are ready to be shown
                // so show sub-patches and hide ourselves.
                for (int i = 0; i < 4; i++) {
                    //System.out.println(this.name + " Showing: " + this.subQuad[i].name);
                    //this.subQuad[i].show();
                }

                hide();
                
                return;
                
            } else {
                //System.out.println(this.name + " Split");
                prepareSubQuads();
            }  
            
        } else {
            
            if ((this.subQuad[0] == null || this.subQuad[0].isLeaf()) &&
                    (this.subQuad[1] == null || this.subQuad[1].isLeaf() ) &&
                    (this.subQuad[2] == null || this.subQuad[2].isLeaf() ) &&
                    (this.subQuad[3] == null || this.subQuad[3].isLeaf() ))
            {
                
                if (this.quadGeometry == null) {
                    //System.out.println(this.name + " Merge");
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
            
            Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");            
            //Material mat = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
            //mat.setColor("Color", ColorRGBA.White);
            mat.setBoolean("VertexColor", true);
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
        }
        
        this.quadNameSet.add(this.name);
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
        
        this.quadNameSet.remove(this.name);
    }
    
    public boolean isPrepared() {
        return patch.isPrepared();
    }
    
    public boolean isLeaf() {
        return (this.subQuad[0] == null && this.subQuad[1] == null && this.subQuad[2] == null && this.subQuad[3] == null);
    }
    
    public void updateStitching()
    {
        if (this.quadGeometry != null)
        {
            int index = 0;

            if (parentNode != null) {
                if (!quadNameSet.contains(leftNeighbour)) {
                    index |= Patch.STITCHING_W;
                }
                if (!quadNameSet.contains(rightNeighbour)) {
                    index |= Patch.STITCHING_E;
                }
                if (!quadNameSet.contains(upNeighbour)) {
                    index |= Patch.STITCHING_N;
                }
                if (!quadNameSet.contains(downNeighbour)) {
                    index |= Patch.STITCHING_S;
                }
            }

            //patch.setStitcthingMode(index);

        }

        for (int i = 0; i < 4; i++) {
            if (this.subQuad[i] != null) {
                this.subQuad[i].updateStitching();
            }
        }

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
                    quadNameSet,
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
                    quadNameSet,
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
                    quadNameSet,
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
                    quadNameSet,
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
    
    private String getLeftNeighbourName(String quadName)
    {
            if (quadName.endsWith("Front")) {
                    return quadName.substring(0, quadName.length()-5) + "Left";
            } else if (quadName.endsWith("Back")) {
                    return quadName.substring(0, quadName.length()-4) + "Right";
            } else if (quadName.endsWith("Left")) {
                    return quadName.substring(0, quadName.length()-4) + "Back";
            } else if (quadName.endsWith("Right")) {
                    return quadName.substring(0, quadName.length()-5) + "Front";
            } else if (quadName.endsWith("Top")) {
                    return quadName.substring(0, quadName.length()-3) + "Left";
            } else if (quadName.endsWith("Bottom")) {
                    return quadName.substring(0, quadName.length()-6) + "Left";
            }

            int nameLength = quadName.length();
            String parentName = quadName.substring(0, nameLength-1);

            String idString = quadName.substring(nameLength-1, nameLength);
            int id = Integer.valueOf(idString);

            switch (id)
            {
            case 0:
                    return getLeftNeighbourName(parentName) + "1";
            case 1:
                    return parentName + "0";
            case 2:
                    return getLeftNeighbourName(parentName) + "3";
            case 3:
                    return parentName + "2";
            }

            return null;
    }

    private String getRightNeighbourName(String quadName)
    {
            if (quadName.endsWith("Front")) {
                    return quadName.substring(0, quadName.length()-5) + "Right";
            } else if (quadName.endsWith("Back")) {
                    return quadName.substring(0, quadName.length()-4) + "Left";
            } else if (quadName.endsWith("Left")) {
                    return quadName.substring(0, quadName.length()-4) + "Front";
            } else if (quadName.endsWith("Right")) {
                    return quadName.substring(0, quadName.length()-5) + "Back";
            } else if (quadName.endsWith("Top")) {
                    return quadName.substring(0, quadName.length()-3) + "Right";
            } else if (quadName.endsWith("Bottom")) {
                    return quadName.substring(0, quadName.length()-6) + "Right";
            }

            int nameLength = quadName.length();
            String parentName = quadName.substring(0, nameLength-1);

            String idString = quadName.substring(nameLength-1, nameLength);
            int id = Integer.valueOf(idString);

            switch (id)
            {
            case 0:
                    return parentName + "1";
            case 1:
                    return getRightNeighbourName(parentName) + "0";
            case 2:
                    return parentName + "3";
            case 3:
                    return getRightNeighbourName(parentName) + "2";
            }

            return "";
    }

    private String getUpNeighbourName(String quadName)
    {
            if (quadName.endsWith("Front")) {
                    return quadName.substring(0, quadName.length()-5) + "Top";
            } else if (quadName.endsWith("Back")) {
                    return quadName.substring(0, quadName.length()-4) + "Top";
            } else if (quadName.endsWith("Left")) {
                    return quadName.substring(0, quadName.length()-4) + "Top";
            } else if (quadName.endsWith("Right")) {
                    return quadName.substring(0, quadName.length()-5) + "Top";
            } else if (quadName.endsWith("Top")) {
                    return quadName.substring(0, quadName.length()-3) + "Back";
            } else if (quadName.endsWith("Bottom")) {
                    return quadName.substring(0, quadName.length()-6) + "Front";
            }

            int nameLength = quadName.length();
            String parentName = quadName.substring(0, nameLength-1);

            String idString = quadName.substring(nameLength-1, nameLength);
            int id = Integer.valueOf(idString);

            switch (id)
            {
            case 0:
                    return getUpNeighbourName(parentName) + "2";
            case 1:
                    return getUpNeighbourName(parentName) + "3";
            case 2:
                    return parentName + "0";
            case 3:
                    return parentName + "1";
            }

            return "";
    }

    private String getDownNeighbourName(String quadName)
    {
            if (quadName.endsWith("Front")) {
                    return quadName.substring(0, quadName.length()-5) + "Bottom";
            } else if (quadName.endsWith("Back")) {
                    return quadName.substring(0, quadName.length()-4) + "Bottom";
            } else if (quadName.endsWith("Left")) {
                    return quadName.substring(0, quadName.length()-4) + "Bottom";
            } else if (quadName.endsWith("Right")) {
                    return quadName.substring(0, quadName.length()-5) + "Bottom";
            } else if (quadName.endsWith("Top")) {
                    return quadName.substring(0, quadName.length()-3) + "Front";
            } else if (quadName.endsWith("Bottom")) {
                    return quadName.substring(0, quadName.length()-6) + "Back";
            }

            int nameLength = quadName.length();
            String parentName = quadName.substring(0, nameLength-1);

            if (parentName.equals(""))
            {
                    return "";
            }

            String idString = quadName.substring(nameLength-1, nameLength);
            int id = Integer.valueOf(idString);

            switch (id)
            {
            case 0:
                    return parentName + "2";
            case 1:
                    return parentName + "3";
            case 2:
                    return getDownNeighbourName(parentName) + "0";
            case 3:
                    return getDownNeighbourName(parentName) + "1";
            }

            return "";
    }
    
    private String rotateCW(String quadName)
    {
        if (quadName.endsWith("0")) {
                return rotateCW(quadName.substring(0, quadName.length()-1)) + "1";
        } else if (quadName.endsWith("1")) {
                return rotateCW(quadName.substring(0, quadName.length()-1)) + "3";
        } else if (quadName.endsWith("2")) {
                return rotateCW(quadName.substring(0, quadName.length()-1)) + "0";
        } else if (quadName.endsWith("3")) {
                return rotateCW(quadName.substring(0, quadName.length()-1)) + "2";
        }

        return quadName;
    }

    private String rotateCCW(String quadName)
    {
        if (quadName.endsWith("0")) {
                return rotateCCW(quadName.substring(0, quadName.length()-1)) + "2";
        } else if (quadName.endsWith("1")) {
                return rotateCCW(quadName.substring(0, quadName.length()-1)) + "0";
        } else if (quadName.endsWith("2")) {
                return rotateCCW(quadName.substring(0, quadName.length()-1)) + "3";
        } else if (quadName.endsWith("3")) {
                return rotateCCW(quadName.substring(0, quadName.length()-1)) + "1";
        }

        return quadName;
    }

    private String rotate180(String quadName)
    {
        if (quadName.endsWith("0")) {
                return rotate180(quadName.substring(0, quadName.length()-1)) + "3";
        } else if (quadName.endsWith("1")) {
                return rotate180(quadName.substring(0, quadName.length()-1)) + "2";
        } else if (quadName.endsWith("2")) {
                return rotate180(quadName.substring(0, quadName.length()-1)) + "1";
        } else if (quadName.endsWith("3")) {
                return rotate180(quadName.substring(0, quadName.length()-1)) + "0";
        }

        return quadName;
    }
    
    private boolean findQuadName(String quadName) {
        java.util.Iterator<String> itr = quadNameSet.iterator();
        while (itr.hasNext()) {
            String i = itr.next();
                if (quadName.equals(i))
                        return true;
        }
        return false;    
    }

}
