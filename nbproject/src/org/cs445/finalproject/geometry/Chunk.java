/**
 * *************************************************************
 * file: Chunk.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project
 * date last modified: 11/20/16
 *
 * purpose: Representation of a group of Voxels
 *
 ***************************************************************
 */
package org.cs445.finalproject.geometry;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs445.finalproject.noise.SimplexNoise;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {

    public static final int SIZE = 100;
    public static final int LENGTH = 2;

    private int maxHeight = 30;
    private int midHeight; // calculated based on noise
    private int minHeight = 15;
    private Set<Integer> surfaceLevels;
    private Block[][][] blocks;
    private int totalBlocks;
    private int vboVertexHandle;
    private int vboColorHandle;
    private int vboTextureHandle;
    private final int startX, startY, startZ;

    private final static Block GRASS = new Block(Block.Type.Grass);
    private final static Block SAND = new Block(Block.Type.Sand);
    private final static Block WATER = new Block(Block.Type.Water);
    private final static Block DIRT = new Block(Block.Type.Dirt);
    private final static Block STONE = new Block(Block.Type.Stone);
    private final static Block BEDROCK = new Block(Block.Type.Bedrock);

    private final Random random;

    private static final Logger LOGGER = Logger.getLogger(Chunk.class.getName());

    public Chunk(int startX, int startY, int startZ) {
        midHeight = maxHeight;
        random = new Random();
        try {
            TextureLoader.getTexture("PNG",
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
        GRASS.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Grass));
        SAND.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Sand));
        WATER.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Water));
        DIRT.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Dirt));
        STONE.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Stone));
        BEDROCK.setTexCoords(createTexCube(0.0f, 0.0f, Block.Type.Bedrock));
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
        Block[][][] blocks = rebuildMesh(startX, startY, startZ);
        LOGGER.log(Level.INFO, "Surface noise levels: {0}", surfaceLevels.size());
        createVertexBuffers(blocks);
    }

    // method: rebuildMesh
    // purpose: Build the mesh of blocks using a simplex noise
    private Block[][][] rebuildMesh(float startX, float startY, float startZ) {
        blocks = new Block[SIZE][maxHeight][SIZE];
        totalBlocks = 0;

        int seed = random.nextInt(SIZE * SIZE);
        LOGGER.log(Level.INFO, "Noise 1 seed: {0}", seed);
        SimplexNoise surfaceNoise1 = new SimplexNoise(30000, 0.66, seed);
        seed = random.nextInt(SIZE * SIZE);
        LOGGER.log(Level.INFO, "Noise 2 seed: {0}", seed);
        SimplexNoise surfaceNoise2 = new SimplexNoise(300000, 0.77, seed);

        seed = new Random().nextInt(SIZE * SIZE);
        SimplexNoise deepNoise = new SimplexNoise(30000, 0.77, seed);

        surfaceLevels = new TreeSet<>();
        for (float x = 0.0f; x < SIZE; x++) {
            for (float z = 0.0f; z < SIZE; z++) {
                int i = (int) (startX + x);
                int k = (int) (startZ + z);

                int surfaceHeight = Math.min(Math.max(maxHeight
                        - (int) (100 * surfaceNoise1.getNoise(i, k)), 1), maxHeight)
                        + Math.min(Math.max(maxHeight
                                - (int) (100 * surfaceNoise2.getNoise(i, k)), 1), maxHeight);
                surfaceHeight /= 2;

                int deepHeight = Math.min(Math.max(minHeight
                        - (int) (100 * deepNoise.getNoise(i, k)), 1), minHeight);

                surfaceLevels.add(surfaceHeight);
                if (surfaceHeight < midHeight) {
                    midHeight = surfaceHeight;
                }
                for (float y = 0; y < surfaceHeight; y++) {
                    blocks[(int) x][(int) y][(int) z] = setBlock(y, deepHeight, surfaceHeight - 1);
                }
            }
        }

        int waterCount = 0;
        int maxIter = 20;
        int i = 0;
        while (waterCount < 10000 && i < maxIter) {
            int x = random.nextInt(SIZE);
            int z = random.nextInt(SIZE);
            int y;
            for (y = maxHeight - 1; y >= midHeight; y--) {
                if (blocks[x][y][z] != null) {
                    break;
                }
            }
            LOGGER.log(Level.INFO, "Water level: {0}", y);
            waterCount = fillLevelBlocks2D(x, y, z, WATER, SAND);
            LOGGER.log(Level.INFO, "{0} Water blocks", waterCount);
            i++;
        }

        Block[][][] blocks = new Block[SIZE][maxHeight][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < maxHeight; y++) {
                    if (isVisible(x, y, z)) {
                        blocks[x][y][z] = this.blocks[x][y][z];
                        totalBlocks++;
                    }
                }
            }
        }
        return blocks;
    }

    // method: tooClose
    // purpose: Given a point in space, check if there is a block close to the point. 
    //          Used for collision checking.
    public boolean tooClose(float cameraX, float cameraY, float cameraZ) {
        cameraX += 100;
        cameraX = 200 - cameraX;
        cameraZ += 100;
        cameraZ = 200 - cameraZ;
        cameraY = 65 - cameraY;
        return (hasBlock(cameraX - 0.5f, cameraY, cameraZ - 0.5f) || hasBlock(cameraX + 0.5f, cameraY, cameraZ - 0.5f) || hasBlock(cameraX - 0.5f, cameraY, cameraZ + 0.5f) || hasBlock(cameraX + 0.5f, cameraY, cameraZ + 0.5f));
    }

    // method: hasBlock
    // purpose: takes in augmented camera coordinates and checks if that coordinate is inside a block on our grid.
    private boolean hasBlock(float cameraX, float cameraY, float cameraZ) {
        int gridX = (int) (cameraX / 2 + 0.5f);
        int gridZ = (int) (cameraZ / 2 + 0.5f);
        int gridY = (int) (cameraY / 2);
        try {
            return blocks[gridX][gridY][gridZ] != null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    // method: createVertexBuffers
    // purpose: Build object, color, and texture vertex buffers
    private void createVertexBuffers(Block[][][] blocks) {
        vboColorHandle = glGenBuffers();
        vboVertexHandle = glGenBuffers();
        vboTextureHandle = glGenBuffers();
        FloatBuffer vertexPositionData
                = BufferUtils.createFloatBuffer(totalBlocks * 6 * 12); // size^3 * (6 vertices) * (12 edges)
        FloatBuffer vertexColorData
                = BufferUtils.createFloatBuffer(totalBlocks * 6 * 12);
        FloatBuffer vertexTextureData
                = BufferUtils.createFloatBuffer(totalBlocks * 6 * 12);
        for (float x = 0.0f; x < SIZE; x++) {
            for (float z = 0.0f; z < SIZE; z++) {
                for (float y = 0; y < maxHeight; y++) {
                    Block block = blocks[(int) x][(int) y][(int) z];
                    if (block == null) {
                        continue;
                    }
                    vertexPositionData.put(createCube(
                            startX + x * LENGTH,
                            startY + y * LENGTH,
                            startZ + z * LENGTH));
                    vertexColorData.put(createCubeVertexCol(
                            getCubeColor(block)));
                    vertexTextureData.put(block.getTexCoords());
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

    // method: createBlock
    // purpose: Create a block with a type based on some height and level
    private Block setBlock(float level, float minHeight, float maxHeight) {
        if (level == maxHeight) {
            return GRASS;
        }
        if (level < minHeight) {
            return BEDROCK;
        }
        if (level == maxHeight - 1) {
            return DIRT;
        }
        if (level < maxHeight - 1) {
            int random = this.random.nextInt(2);
            switch (random) {
                case 0:
                    return DIRT;
                default:
                    return STONE;
            }
        }
        return GRASS;
    }

    // method: fillLevelBlocks2D
    // purpose: perform fill for all blocks at a level that is not a boundary
    private int fillLevelBlocks2D(int startX, int startY, int startZ,
            Block fillBlock,
            Block boundaryFill) {
        int count = 0;
        if (isBoundaryBlock2D(startX, startY, startZ)) {
            return count;
        }
        Set<Vector3i> visited = new HashSet<>();
        LinkedList<Vector3i> queue = new LinkedList<>();
        blocks[startX][startY][startZ] = fillBlock;
        count++;
        queue.add(new Vector3i(startX, startY, startZ));
        while (!queue.isEmpty()) {
            Vector3i head = queue.removeFirst();
            if (!visited.contains(head)) {
                for (Vector3i neighbor : getNeighbors2D(head.x, head.y, head.z)) {
                    if (!visited.contains(neighbor)) {
                        if (!isBoundaryBlock2D(neighbor.x, neighbor.y, neighbor.z)) {
                            blocks[neighbor.x][neighbor.y][neighbor.z] = fillBlock;
                            count++;
                            queue.push(neighbor);
                        } else {
                            blocks[neighbor.x][neighbor.y][neighbor.z] = boundaryFill;
                        }
                    }
                }
            }
            visited.add(head);
        }
        return count;
    }

    // method: isBoundaryBlock2D
    // purpose: check if a block is a boundary
    private boolean isBoundaryBlock2D(int x, int y, int z) {
        return x < 1 || z < 1 || x == SIZE - 1 || z == SIZE - 1
                || y == maxHeight - 1
                || // same level
                blocks[x - 1][y][z] == null
                || blocks[x][y][z - 1] == null
                || blocks[x][y][z + 1] == null
                || blocks[x + 1][y][z] == null
                || // above level
                blocks[x - 1][y + 1][z] != null
                || blocks[x][y + 1][z - 1] != null
                || blocks[x][y + 1][z + 1] != null
                || blocks[x + 1][y + 1][z] != null;
    }

    // method: isSurfaceBlock2D
    private boolean isVisible(int x, int y, int z) {
        return x == 0
                || x == SIZE - 1
                || y == 0
                || y == maxHeight - 1
                || z == 0
                || z == SIZE - 1
                || blocks[x][y][z] == null
                || blocks[x - 1][y][z] == null
                || blocks[x + 1][y][z] == null
                || blocks[x][y][z - 1] == null
                || blocks[x][y][z + 1] == null
                || blocks[x][y + 1][z] == null;
    }

    // method: getNeighbors2D
    // purpose: Given a block (x,y,z), return its 8 neighbor block positions
    private List<Vector3i> getNeighbors2D(int x, int y, int z) {
        List<Vector3i> neighbors = new LinkedList<>();
        if (blocks[x - 1][y][z] != null) {
            neighbors.add(new Vector3i(x - 1, y, z));
        }
        if (blocks[x - 1][y][z - 1] != null) {
            neighbors.add(new Vector3i(x - 1, y, z - 1));
        }
        if (blocks[x][y][z - 1] != null) {
            neighbors.add(new Vector3i(x, y, z - 1));
        }
        if (blocks[x + 1][y][z - 1] != null) {
            neighbors.add(new Vector3i(x + 1, y, z - 1));
        }
        if (blocks[x + 1][y][z] != null) {
            neighbors.add(new Vector3i(x + 1, y, z));
        }
        if (blocks[x + 1][y][z + 1] != null) {
            neighbors.add(new Vector3i(x + 1, y, z + 1));
        }
        if (blocks[x][y][z + 1] != null) {
            neighbors.add(new Vector3i(x, y, z + 1));
        }
        if (blocks[x - 1][y][z + 1] != null) {
            neighbors.add(new Vector3i(x - 1, y, z + 1));
        }
        return neighbors;
    }

    // method: createCube
    // purpose: Create a cube at a location
    private float[] createCube(float x, float y, float z) {
        int offset = LENGTH / 2;
        return new float[]{
            // top quad
            offset + x, offset + y, offset + z,
            offset + x, offset + y, -offset + z,
            -offset + x, offset + y, -offset + z,
            -offset + x, offset + y, offset + z,
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
        return new float[]{1.0f, 1.0f, 1.0f};
    }

    private float[] createTexCube(float x, float y, Block.Type type) {
        float offset = (1024 / 16) / 1024.0f;
        switch (type) {
            case Dirt:
                return new float[]{
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
            case Bedrock:
                return new float[]{
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
            case Sand:
                return new float[]{
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
            case Stone:
                return new float[]{
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
            case Water:
                return new float[]{
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
            case Grass:
            default:
                return new float[]{
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
                    x + offset * 4, y + offset * 1,};
        }
    }
}
