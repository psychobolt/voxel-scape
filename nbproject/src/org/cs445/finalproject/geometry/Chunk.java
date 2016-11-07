/**
 * *************************************************************
 * file: Chunk.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project
 * date last modified: 11/5/16
 *
 * purpose: Representation of a group of Voxels
 *
 ***************************************************************
 */
package org.cs445.finalproject.geometry;

import java.nio.FloatBuffer;
import java.util.HashSet;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs445.finalproject.noise.SimplexNoise;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {
    
    public static final int SIZE = 30;
    public static final int LENGTH = 2;
   
    private int maxHeight;
    private int minHeight;
    private final Block[][][] blocks;
    private Texture texture;
    private int vboVertexHandle;
    private int vboColorHandle;
    private int vboTextureHandle;
    private final int startX, startY, startZ;
    
    private final Random random;
    private int noiseLevel;
    
    private static final Logger LOGGER = Logger.getLogger(Chunk.class.getName());
    
    public Chunk(int startX, int startY, int startZ) {
        maxHeight = SIZE;
        minHeight = SIZE;
        random = new Random();
        blocks = new Block[SIZE][SIZE][SIZE];
        try {
            texture = TextureLoader.getTexture("PNG", 
                ResourceLoader.getResourceAsStream(
                    "org/cs445/finalproject/assets/terrain.png"));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load terrain.png", e);
        }
        vboVertexHandle = glGenBuffers();
        vboColorHandle = glGenBuffers();
        vboTextureHandle = glGenBuffers();
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        randomize();
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
    
    // method: randomize
    // purpose: Find a good random chunk
    public void randomize() {
        do {
            rebuildMesh(startX, startY, startZ);
        } while (noiseLevel < 5);
        LOGGER.log(Level.INFO, "Noise level: " + noiseLevel);
        noiseLevel = 0;
    }
    
    // method: rebuildMesh
    // purpose: Construct and bind the Voxel's vertex and color buffers
    private void rebuildMesh(float startX, float startY, float startZ) {
        noiseLevel = 0;
        vboColorHandle = glGenBuffers();
        vboVertexHandle = glGenBuffers();
        vboTextureHandle = glGenBuffers();
        FloatBuffer vertexPositionData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12); // size^3 * (6 vertices) * (12 edges)
        FloatBuffer vertexColorData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12);
        FloatBuffer vertexTextureData = 
            BufferUtils.createFloatBuffer(SIZE * SIZE * SIZE * 6 * 12);
        int seed =  new Random().nextInt(7000);
        LOGGER.log(Level.INFO, "Noise seed: " + seed);
        SimplexNoise noise = new SimplexNoise(50000, 0.66, seed);
        Set<Integer> heights = new HashSet<>();
        for (float x = 0.0f; x < SIZE; x++) {
            for (float z = 0.0f; z < SIZE; z++) {
                int i = (int) (startX + x);
                int k = (int) (startZ + z);
                int height = Math.min(
                    Math.max(maxHeight - (int) (100 * noise.getNoise(i, k)), 1), 
                    maxHeight);
                heights.add(height);
                if (height < minHeight) {
                    minHeight = height;
                }
                for (float y = 0; y < height; y++) {
                    Block block = createBlock(y, height);
                    blocks[(int) x][(int) y][(int) z] = block;
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
        noiseLevel = heights.size();
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
    
    // method: createBlock
    // purpose: Create a block with a type based on some height and level
    private Block createBlock(float level, float maxHeight) {
        // TODO: Make the thing random
        // Level and maxHeight will be used to determine type later.
        int random = this.random.nextInt(6);
        switch (random) {
            case 0:
                return new Block(Block.Type.Dirt);
            case 1:
                return new Block(Block.Type.Bedrock);
            case 2:
                return new Block(Block.Type.Grass);
            case 3:
                return new Block(Block.Type.Sand);
            case 4:
                return new Block(Block.Type.Water);
            default:
                return new Block(Block.Type.Stone);
        }
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
        if (block.getTypeId() == Block.Type.Dirt.getId()) {
            return new float[] {
                // top
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // bottom
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // back
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // front
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // right
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0,
                // left
                x + offset * 3, y + offset * 1,
                x + offset * 2, y + offset * 1,
                x + offset * 2, y + offset * 0,
                x + offset * 3, y + offset * 0
            };
        } else if (block.getTypeId() == Block.Type.Bedrock.getId()) {
            return new float[] {
                // top
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1,
                // bottom
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1,
                // back
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1,
                // front
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1,
                // right
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1,
                // left
                x + offset * 2, y + offset * 2,
                x + offset * 1, y + offset * 2,
                x + offset * 1, y + offset * 1,
                x + offset * 2, y + offset * 1
            };
        } else if (block.getTypeId() == Block.Type.Grass.getId()) {
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
                x + offset * 4, y + offset * 1,
                x + offset * 3, y + offset * 1,
                x + offset * 3, y + offset * 0,
                x + offset * 4, y + offset * 0,
                // front
                x + offset * 4, y + offset * 1,
                x + offset * 3, y + offset * 1,
                x + offset * 3, y + offset * 0,
                x + offset * 4, y + offset * 0,
                // right
                x + offset * 4, y + offset * 1,
                x + offset * 3, y + offset * 1,
                x + offset * 3, y + offset * 0,
                x + offset * 4, y + offset * 0,
                // left
                x + offset * 4, y + offset * 1,
                x + offset * 3, y + offset * 1,
                x + offset * 3, y + offset * 0,
                x + offset * 4, y + offset * 0
            };
        } else if (block.getTypeId() == Block.Type.Sand.getId()) {
            return new float[] {
                // top
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1,
                // bottom
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1,
                // back
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1,
                // front
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1,
                // right
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1,
                // left
                x + offset * 3, y + offset * 2,
                x + offset * 2, y + offset * 2,
                x + offset * 2, y + offset * 1,
                x + offset * 3, y + offset * 1
            };
        } else if (block.getTypeId() == Block.Type.Stone.getId()) {
            return new float[] {
                // top
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0,
                // bottom
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0,
                // back
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0,
                // front
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0,
                // right
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0,
                // left
                x + offset * 2, y + offset * 1,
                x + offset * 1, y + offset * 1,
                x + offset * 1, y + offset * 0,
                x + offset * 2, y + offset * 0
            };
        } else if (block.getTypeId() == Block.Type.Water.getId()) {
            return new float[] {
                // top
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9,
                // bottom
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9,
                // back
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9,
                // front
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9,
                // right
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9,
                // left
                x + offset * 1, y + offset * 10,
                x + offset * 0, y + offset * 10,
                x + offset * 0, y + offset * 9,
                x + offset * 1, y + offset * 9
            };
        }
        return null;
    }
}
