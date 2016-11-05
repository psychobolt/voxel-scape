/**
 * *************************************************************
 * file: Chunk.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project
 * date last modified: 11/4/16
 *
 * purpose: Representation of a group of Voxels
 *
 ***************************************************************
 */
package org.cs445.finalproject.geometry;

import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {
    
    public static final int SIZE = 30;
    public static final int LENGTH = 2;
    
    private final Block[][][] blocks;
    private Texture texture;
    private int vboVertexHandle;
    private int vboColorHandle;
    private int vboTextureHandle;
    private final int startX, startY, startZ;
    
    private final Random random;
    
    private static final Logger LOGGER = Logger.getLogger(Chunk.class.getName());
    
    public Chunk(int startX, int startY, int startZ) {
        random = new Random();
        blocks = new Block[SIZE][SIZE][SIZE];
        try {
            texture = TextureLoader.getTexture("PNG", 
                ResourceLoader.getResourceAsStream(
                    "org/cs445/finalproject/assets/terrain.png"));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load terrain.png", e);
        }
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (random.nextFloat() > 0.7f) {
                        blocks[x][y][z] = new Block(Block.Type.Grass);
                    } else if (random.nextFloat() > 0.4f) {
                        blocks[x][y][z] = new Block(Block.Type.Dirt);
                    } else if (random.nextFloat() > 0.2f) {
                        blocks[x][y][z] = new Block(Block.Type.Water);
                    } else {
                        blocks[x][y][z] = new Block(Block.Type.Dirt);
                    }
                }
            }
        }
        vboVertexHandle = glGenBuffers();
        vboColorHandle = glGenBuffers();
        vboTextureHandle = glGenBuffers();
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }
    
    // method: render
    // purpose: Render the chunk
    public void render() {
        glPushMatrix();
            glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
            glColorPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, vboTextureHandle);
            glBindTexture(GL_TEXTURE_2D, 1);
            glTexCoordPointer(2, GL_FLOAT, 0, 0L);
            glDrawArrays(GL_QUADS, 0, SIZE * SIZE * SIZE * 24); // size^3 * (3 floats) * (6 vertices)
        glPopMatrix();
    }
    
    // method: rebuildMesh
    // purpose: Construct and bind the Voxel's vertex and color buffers
    public void rebuildMesh(float startX, float startY, float startZ) {
        vboColorHandle = glGenBuffers();
        vboVertexHandle = glGenBuffers();
        vboTextureHandle = glGenBuffers();
        FloatBuffer vertexPositionData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12); // size^3 * (6 vertices) * (12 edges)
        FloatBuffer vertexColorData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12);
        FloatBuffer vertexTextureData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12);
        for (float x = 0.0f; x < SIZE; x++) {
            for (float z = 0.0f; z < SIZE; z++) {
                for (float y = 0.0f; y < SIZE; y++) {
                    Block block = blocks[(int) x][(int) y][(int) z];
                    vertexPositionData.put(createCube(
                        startX + x * LENGTH, 
                        startY + y * LENGTH,
                        startZ + z * LENGTH));
                    vertexColorData.put(createCubeVertexCol(
                        getCubeColor(block)));
                    vertexTextureData.put(createTexCube(0.0f, 0.0f, block));
                }
            }
        }
        vertexPositionData.flip();
        vertexColorData.flip();
        vertexTextureData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vboTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    // method: createCube
    // purpose: Create a cube at a location
    private float[] createCube(float x, float y, float z) {
        int offset = LENGTH / 2;
        return new float[] {
            // top quad
            offset + x, offset + y, offset + z,
            -offset + x, offset + y, offset + z,
            -offset + x, offset + y, -offset + z,
            offset + x, offset + y, -offset + z,
            // bottom face
            offset + x, -offset + y, offset + z,
            -offset + x, -offset + y, offset + z,
            -offset + x, -offset + y, -offset + z,
            offset + x, -offset + y, -offset + z,
            // back face
            offset + x, -offset + y, -offset + z,
            -offset + x, -offset + y, -offset + z,
            -offset + x, offset + y, -offset + z,
            offset + x, offset + y, -offset + z,
            // front face
            offset + x, offset + y, offset + z,
            -offset + x, offset + y, offset + z,
            -offset + x, -offset + y, offset + z,
            offset + x, -offset + y, offset + z,
            // right face
            offset + x, offset + y, -offset + z,
            offset + x, offset + y, offset + z,
            offset + x, -offset + y, offset + z,
            offset + x, -offset + y, -offset + z,
            // left face
            -offset + x, offset + y, offset + z,
            -offset + x, offset + y, -offset + z,
            -offset + x, -offset + y, -offset + z,
            -offset + x, -offset + y, offset + z
        };
    }
    
    // method: createCubeVertexCol
    // purpose: Create the color vertex array based on the cube's colors
    private float[] createCubeVertexCol(float[] cubeColorArray) {
        float[] cubeColors = new float[cubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = cubeColorArray[i % cubeColorArray.length];
        }
        return cubeColors;
    }
    
    // method: getCubeColor
    // purpose: Returns the color of the cube
    private float[] getCubeColor(Block block) {
        return new float[] {1.0f, 1.0f, 1.0f};
    }
    
    private float[] createTexCube(float x, float y, Block block) {
        float offset = (1024 / 16) / 1024.0f;
        //if (block.getTypeId() == Block.Type.Dirt.getId()) {
            return new float[] {
                // top
                x + offset * 3, y + offset * 10,
                x + offset * 2, y + offset * 10,
                x + offset * 2, y + offset * 9,
                x + offset * 3, y + offset * 9,
                // bottom
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // back
                x + offset * 3, y + offset * 1,
                x + offset * 4, y + offset * 1,
                x + offset * 4, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // front
                x + offset * 4, y + offset * 0,
                x + offset * 3, y + offset * 0,
                x + offset * 3, y + offset * 1,
                x + offset * 4, y + offset * 1,
                // right
                x + offset * 4, y + offset * 0,
                x + offset * 3, y + offset * 0,
                x + offset * 3, y + offset * 1,
                x + offset * 4, y + offset * 1,
                // left
                x + offset * 4, y + offset * 0,
                x + offset * 3, y + offset * 0,
                x + offset * 3, y + offset * 1,
                x + offset * 4, y + offset * 1
            };
        //}
    }
}
