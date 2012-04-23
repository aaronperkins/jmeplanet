/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmeplanet;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.math.ColorRGBA;
import com.jme3.material.Material;
import com.jme3.terrain.heightmap.AbstractHeightMap;

import com.jme3.bounding.BoundingBox;

import java.util.List;
import java.util.ArrayList;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;


/**
 *
 * @author aaron
 */
public class Patch {
       
    protected int quads;
    protected Vector3f min;
    protected Vector3f max;
    protected float texXMin;
    protected float texXMax;
    protected float texYMin;
    protected float texYMax;
    protected float baseRadius;
    protected float scalingFactor;
    protected HeightDataSource dataSource;
    protected int position;
    protected int padding = 2;
    
    protected Mesh mesh;
    protected BoundingBox aabb;
    protected Vector3f center;
    
    protected int[] indexBuffer;
    protected int[] edgeVertexIndex;
   
    public Patch(
            int quads,
            Vector3f min,
            Vector3f max,
            float texXMin,
            float texXMax,
            float texYMin,
            float texYMax,
            float baseRadius,
            float scalingFactor,
            HeightDataSource dataSource,
            int position) {
        
        this.quads = quads;
        this.min = min;
        this.max = max;
        this.texXMin = texXMin;
        this.texXMax = texXMax;
        this.texYMin = texYMin;
        this.texYMax = texYMax;
        this.baseRadius = baseRadius;
        this.scalingFactor = scalingFactor;
        this.dataSource = dataSource;
        this.position = position;
    }
    
    public Mesh prepare() {
        
        mesh = new Mesh();
        
        Vector3f[] vertexPosition = new Vector3f[(this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1)];
        float[] vertexColor = new float[vertexPosition.length * 4];
        Vector3f[] vertexNormal = new Vector3f[(this.quads + 1) * (this.quads + 1)];
        
        generateVertexPositions(vertexPosition, vertexColor);
        generateVertexNormals(vertexNormal, vertexPosition);
        
        // Load data into final buffers
        int skirtVertexCount = this.quads * 4;
        int vertexCount = (this.quads + 1) * (this.quads + 1);
        
        Vector3f[] vertexData = new Vector3f[vertexCount + skirtVertexCount];
        float[] colorData = new float[vertexData.length * 4];
        Vector3f[] normalData = new Vector3f[vertexData.length];
        
        int index = 0;
        for (int y = 0; y < (this.quads + 1); y++)
        {
            for (int x = 0; x < (this.quads + 1); x++)
            {
                int index1 = (this.quads + 2 * this.padding + 1) * (y + this.padding) + (x + this.padding);
                int index2 = (this.quads + 1) * y + x;

                // Vertex pos
                vertexData[index] = vertexPosition[index1];

                colorData[index * 4 ] = vertexColor[index1 * 4 ];
                colorData[index * 4 +1] = vertexColor[index1 * 4 +1];
                colorData[index * 4 +2] = vertexColor[index1 * 4 +2];
                colorData[index * 4 +3] = vertexColor[index1 * 4 +3];

                // Vertex normal
                normalData[index] = vertexNormal[index2];

                index++;
            }
        }
        
        // Get the patch's edge vertex indexes going clockwise
        int indexEdgeVertexIndex = 0;
        int verticesPerSide = this.quads + 1;
        edgeVertexIndex = new int[skirtVertexCount];
        for (int i = 0; i < verticesPerSide; i++)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = verticesPerSide + this.quads; i < vertexCount + 1; i+=verticesPerSide)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = vertexCount - 2; i >= verticesPerSide * quads; i--)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = verticesPerSide * quads - verticesPerSide; i > 0; i-=verticesPerSide)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        
        // Add skirt vertexes to end of vertex buffer
        int indexSkirt = vertexCount;
        for (int i = 0; i < skirtVertexCount; i++)
        {
            
            vertexData[indexSkirt] = vertexData[edgeVertexIndex[i]].subtract(this.center.normalize().mult(300f));
            

            normalData[indexSkirt] = vertexNormal[edgeVertexIndex[i]];
            
            /*
            colorData[indexSkirt * 4 ] = colorData[edgeVertexIndex[i] * 4 ];
            colorData[indexSkirt * 4 +1] = colorData[edgeVertexIndex[i] * 4 +1];
            colorData[indexSkirt * 4 +2] = colorData[edgeVertexIndex[i] * 4 +2];
            colorData[indexSkirt * 4 +3] = colorData[edgeVertexIndex[i] * 4 +3];
            
             * 
             */
            colorData[indexSkirt * 4 ] = 1f;
            colorData[indexSkirt * 4 +1] = 0;
            colorData[indexSkirt * 4 +2] = 0;
            colorData[indexSkirt * 4 +3] = 1f;

            indexSkirt++;         
        }
      
        generateIndices();
        
//        System.out.println("vc: " + vertexData.length);
//        for (int i = 0; i < vertexData.length; i++) {
//            System.out.println(String.valueOf(i) + ": " + vertexData[i]);
//        }
//        
//        System.out.println("ic: " + indexBuffer.length);
//        for (int i = 0; i < indexBuffer.length; i+=3) {
//            System.out.print(String.valueOf(indexBuffer[i]) + ",");
//            System.out.print(String.valueOf(indexBuffer[i+1]) + ",");
//            System.out.println(String.valueOf(indexBuffer[i+2]));
//        }
        

        // Set mesh buffers
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertexData));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normalData));
        mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(colorData));
        mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indexBuffer));  
        mesh.updateBound();
        
        //mesh.setMode(Mesh.Mode.Points);
        //mesh.setPointSize(5f);
        //mesh.setStatic();
        mesh.setDynamic();
        
        return mesh; 
    }
    
    protected void generateIndices() {
        int skirtTriangles = edgeVertexIndex.length * 2;
        int maxTriangles = (2 * quads * quads) + skirtTriangles;
        int triangles = maxTriangles;
        
        indexBuffer = new int[3 * triangles];
        
        int index = 0;
        for (int y = 0; y < quads; y++) {
            for (int x = 0; x < quads; x++) {
                indexBuffer[index++] = y * (quads + 1) + x;
                indexBuffer[index++] = (y + 1) * (quads + 1) + x;
                indexBuffer[index++] = y * (quads + 1) + x + 1;
                indexBuffer[index++] = (y + 1) * (quads + 1) + x;
                indexBuffer[index++] = (y + 1) * (quads + 1) + x + 1;
                indexBuffer[index++] = y * (quads + 1) + x + 1;
            }
        }  
        
        int skirtOffset = (this.quads + 1) * (this.quads + 1);
        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < edgeVertexIndex.length; x++) {
                if (x != edgeVertexIndex.length - 1) {
                    indexBuffer[index++] = edgeVertexIndex[x];
                    indexBuffer[index++] = edgeVertexIndex[x  + 1];
                    indexBuffer[index++] = x + skirtOffset;                
                    indexBuffer[index++] = x + skirtOffset;
                    indexBuffer[index++] = edgeVertexIndex[x + 1];       
                    indexBuffer[index++] = x + 1 + skirtOffset;
                } else {
                    indexBuffer[index++] = edgeVertexIndex[x];
                    indexBuffer[index++] = edgeVertexIndex[0];
                    indexBuffer[index++] = x + skirtOffset;                
                    indexBuffer[index++] = x + skirtOffset;
                    indexBuffer[index++] = edgeVertexIndex[0];       
                    indexBuffer[index++] = skirtOffset;
                }
                
            }
        }
        
    }
    
    public boolean isPrepared() {
        return this.mesh != null;
    }
    
    public Mesh getMesh() {
        return this.mesh;
    }
    
    public Vector3f getCenter() {
        return this.center;
    }
    
    public BoundingBox getAABB() {
        return this.aabb;
    }
    
    protected void generateVertexPositions(Vector3f[] vertexPosition, float[] vertexColor) {
        
       // Use "texture coordinates"
        // _xPos is where U axis changes
        float xPos = 0;
        // _yPos is where V axis changes
        float yPos = 0;
        // _zPos is where the normal would be
        float zPos = 0;

        Vector2f startPos = new Vector2f();
        Vector2f endPos = new Vector2f();

        int a = 0;
        int b = 0;
        int c = 0;
        int side = 0;
        
        if (this.min.x == 1.0 && this.max.x == 1.0) {
            // Patch on right side
            // On right side, T coordinate lies in Z axis
            // U coordinate lies in Y axis
            // X axis is normal
            side = 0;
            startPos.x = this.min.z;
            startPos.y = this.min.y;
            endPos.x = this.max.z;
            endPos.y = this.max.y;
            a = -1;
            b = -1;
            c = 1;
        }
        if (this.min.x == -1.0 && this.max.x == -1.0) {
            // Patch on left side
            side = 1;
            startPos.x = this.min.z;
            startPos.y = this.min.y;
            endPos.x = this.max.z;
            endPos.y = this.max.y;
            a = 1;
            b = -1;
            c = -1;
        } else if (this.min.y == 1.0 && this.max.y == 1.0) {
            // Patch on top side
            side = 2;
            startPos.x = this.min.x;
            startPos.y = this.min.z;
            endPos.x = this.max.x;
            endPos.y = this.max.z;
            a = 1;
            b = 1;
            c = 1;
        } else if (this.min.y == -1.0 && this.max.y == -1.0) {
            // Patch on bottom side
            side = 3;
            startPos.x = this.min.x;
            startPos.y = this.min.z;
            endPos.x = this.max.x;
            endPos.y = this.max.z;
            a = 1;
            b = -1;
            c = -1;
        } else if (this.min.z == 1.0 && this.max.z == 1.0) {
            // Patch on front side
            side = 4;
            startPos.x = this.min.x;
            startPos.y = this.min.y;
            endPos.x = this.max.x;
            endPos.y = this.max.y;
            a = 1;
            b = -1;
            c = 1;
        } else if (this.min.z == -1.0 && this.max.z == -1.0) {
            // Patch on back side
            side = 5;
            startPos.x = this.min.x;
            startPos.y = this.min.y;
            endPos.x = this.max.x;
            endPos.y = this.max.y;
            a = -1;
            b = -1;
            c = -1;
        }

        // Calculate unit sphere positions
        Vector3f[] unitSpherePos = new Vector3f[(this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1)];
        float[] heightData = new float[(this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1)];
        Vector3f pos = new Vector3f();
        for (int y = 0-this.padding; y <= (this.quads + this.padding); y++) {
            for (int x = 0-this.padding; x <= (this.quads + this.padding); x++) {
                int index = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding);

                xPos = (startPos.x + (endPos.x - startPos.x) * (((float) x)/this.quads));
                yPos = (startPos.y + (endPos.y - startPos.y) * (((float) y)/this.quads));
                zPos = c;
                
                switch (side) {
                    case 0: pos.x = zPos; pos.y = yPos; pos.z = xPos; break;
                    case 1: pos.x = zPos; pos.y = yPos; pos.z = xPos; break;
                    case 2: pos.x = xPos; pos.y = zPos; pos.z = yPos; break;
                    case 3: pos.x = xPos; pos.y = zPos; pos.z = yPos; break;
                    case 4: pos.x = xPos; pos.y = yPos; pos.z = zPos; break;
                    case 5: pos.x = xPos; pos.y = yPos; pos.z = zPos; break;
                }
              
                // normalize the position making it curved
                unitSpherePos[index] = pos.normalize();
                
                // get height data for given index
                heightData[index] = this.dataSource.getValue(unitSpherePos[index]) * 150f;                    
            }
        }
              
        // Now calculate vertex positions (with padding) in planet space
        Vector3f minBounds = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vector3f maxBounds = new Vector3f(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE);
        for (int y = 0; y < (this.quads + 2*this.padding + 1); y++) {
            for (int x = 0; x < (this.quads + 2*this.padding + 1); x++) {
                
                int index = (this.quads + 2*this.padding + 1) * y + x;
 
                vertexPosition[index] = unitSpherePos[index].mult(baseRadius + heightData[index] * scalingFactor);

                minBounds.x = Math.min(minBounds.x, vertexPosition[index].x);
                minBounds.y = Math.min(minBounds.y, vertexPosition[index].y);
                minBounds.z = Math.min(minBounds.z, vertexPosition[index].z);
                maxBounds.x = Math.max(maxBounds.x, vertexPosition[index].x);
                maxBounds.y = Math.max(maxBounds.y, vertexPosition[index].y);
                maxBounds.z = Math.max(maxBounds.z, vertexPosition[index].z);
                
                // Set vertex colors
                if( heightData[index] <= 0f ) {
                    vertexColor[index * 4 ]=(0.0f);
                    vertexColor[index * 4 +1]=(0.4f);
                    vertexColor[index * 4 +2]=(0.8f);
                    vertexColor[index * 4 +3]=(1.0f); // Ocean
                } else if( heightData[index] <= 15f ) {
                    vertexColor[index * 4 ]=(0.83f);
                    vertexColor[index * 4 +1]=(0.72f);
                    vertexColor[index * 4 +2]=(0.34f);
                    vertexColor[index * 4 +3]=(1.0f); // Sand
                } else if( heightData[index] <= 125f ) {
                    vertexColor[index * 4 ]=(0.2f);
                    vertexColor[index * 4 +1]=(0.6f);
                    vertexColor[index * 4 +2]=(0.1f);
                    vertexColor[index * 4 +3]=(1.0f); // Grass
                } else { 
                    vertexColor[index * 4 ]=(0.5f);
                    vertexColor[index * 4 +1]=(0.5f);
                    vertexColor[index * 4 +2]=(0.5f);
                    vertexColor[index * 4 +3]=(1.0f); // Mountains
                }
                
            }
        }
        
        // Transform vertex positions to object space (i.e. centered around origin)
        this.aabb = new BoundingBox(minBounds, maxBounds);
        this.center = aabb.getCenter();
        minBounds = minBounds.subtract(this.center);
        maxBounds = maxBounds.subtract(this.center);
        for (int y = 0; y < (this.quads + 2*this.padding + 1); y++)
        {
                for (int x = 0; x < (this.quads + 2*this.padding + 1); x++)
                {
                        int index = (this.quads + 2*this.padding + 1) * y + x;
                        vertexPosition[index] = vertexPosition[index].subtract(center);
                }
        }
        
    }
    
    protected void generateVertexNormals(Vector3f[] vertexNormal, Vector3f[] vertexPosition) {
       
        // Calculate vertex normals, texture coordinates and interpolated positions
        for (int y = 0; y < (this.quads + 1); y++)
        {
            for (int x = 0; x < (this.quads + 1); x++)
            {
                int index = (this.quads + 1) * y + x;

                // 6-connected, with triangle-area correction
                int pIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding);
                int pNextXIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding + 1);
                int pNextYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding + 1) + (x + this.padding);
                int pPrevXIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding - 1);
                int pPrevYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding - 1) + (x + this.padding);
                int pNextXPrevYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding - 1) + (x + this.padding + 1);
                int pPrevXNextYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding + 1) + (x + this.padding - 1);

                Vector3f thisVertex = vertexPosition[pIndex];
                Vector3f nextXVertex = vertexPosition[pNextXIndex];
                Vector3f nextYVertex = vertexPosition[pNextYIndex];
                Vector3f prevXVertex = vertexPosition[pPrevXIndex];
                Vector3f prevYVertex = vertexPosition[pPrevYIndex];
                Vector3f nextXPrevYVertex = vertexPosition[pNextXPrevYIndex];
                Vector3f prevXNextYVertex = vertexPosition[pPrevXNextYIndex];

                Vector3f n1 = nextXVertex.subtract(thisVertex).cross(nextXPrevYVertex.subtract(thisVertex));
                Vector3f n2 = nextXPrevYVertex.subtract(thisVertex).cross(prevYVertex.subtract(thisVertex));
                Vector3f n3 = prevYVertex.subtract(thisVertex).cross(prevXVertex.subtract(thisVertex));
                Vector3f n4 = prevXVertex.subtract(thisVertex).cross(prevXNextYVertex.subtract(thisVertex));
                Vector3f n5 = prevXNextYVertex.subtract(thisVertex).cross(nextYVertex.subtract(thisVertex));
                Vector3f n6 = nextYVertex.subtract(thisVertex).cross(nextXVertex.subtract(thisVertex));

                vertexNormal[index] = (n1.add(n2).add(n3).add(n4).add(n5).add(n6)).normalize();
            }
        }
        
    }
        
}
