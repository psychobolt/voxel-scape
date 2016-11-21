/**
 * *************************************************************
 * file: FPCameraController.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project 
 * date last modified: 11/20/16
 *
 * purpose: Controller for the First Person Camera
 *
 ***************************************************************
 */
package org.cs445.finalproject.camera;

import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs445.finalproject.geometry.Chunk;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class FPCameraController {
    
    private final Vector3f position;
    private final Vector3f lookAt;
    
    private float yaw;
    private float pitch;
    
    private Chunk world;
    private final int worldX;
    private final int worldY;
    private final int worldZ;
    
    private int lightOffsetZ;
    private enum LightMode {
        FULL_LIT,
        HALF_LIT,
        DIM_LIGHT
    }
    private int lightMode;
    
    public FPCameraController(float x, float y, float z) {
        position = new Vector3f(x, y, z);
        lookAt = new Vector3f(x, y, z);
        lookAt.x = 0.0f;
        lookAt.y = 15.0f;
        lookAt.z = 0.0f;
        yaw = 0.0f;
        pitch = 0.0f;
        worldX = -150;
        worldY = -65;
        worldZ = -150;
        lightMode = LightMode.FULL_LIT.ordinal();
        toggleLightMode();
    }
    
    // method: yaw
    // purpose: Increment the camera's current yaw rotation
    public void yaw(float amount) {
        yaw += amount;
    }
    
    // method: pitch
    // purpose: Decrement the camera's current pitch rotation
    public void pitch(float amount) {
        pitch -= amount;
    }
    
    // method: walkForward
    // purpose: Move the camera forward relative to its current rotation (yaw)
    public void walkForward(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw));
        position.x -= xOffset;
        position.z += zOffset;
        lookAt.x -= xOffset;
        lookAt.z += zOffset;
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lookAt.x).put(lookAt.y).put(lookAt.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    // method: walkBackwards
    // purpose: Move the camera backward relative to its current rotation (yaw)
    public void walkBackwards(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z -= zOffset;
        lookAt.x += xOffset;
        lookAt.z -= zOffset;
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lookAt.x).put(lookAt.y).put(lookAt.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    // method: strafeLeft
    // purpose: Strafes the camera left relative to its current rotation (yaw)
    public void strafeLeft(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw - 90));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw - 90));
        position.x -= xOffset;
        position.z += zOffset;
        lookAt.x -= xOffset;
        lookAt.z += zOffset;
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lookAt.x).put(lookAt.y).put(lookAt.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    // method: strafeRight
    // purpose: Strafes the camera right relative to its current rotation (yaw)
    public void strafeRight(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw + 90));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw + 90));
        position.x -= xOffset;
        position.z += zOffset;
        lookAt.x -= xOffset;
        lookAt.z += zOffset;
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lookAt.x).put(lookAt.y).put(lookAt.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    // method: moveUp
    // purpose: Moves the camera up
    public void moveUp(float distance) {
        position.y -= distance;
    }
    
    // method: moveDown
    // purpose: Moves the camera down
    public void moveDown(float distance) {
        position.y += distance;
    }
    
    // method: lookThrough
    // purpose: Translates and rotate the matrix so that it looks through the 
    // camera
    public void lookThrough() {
        // rotate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        // rotate the pitch around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        
        if (Keyboard.isKeyDown(Keyboard.KEY_L)) { // toggle lighting
            toggleLightMode();
        }
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lookAt.x).put(lookAt.y).put(lookAt.z + lightOffsetZ)
            .put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        
        // translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
    }
    
    // method: gameLoop
    // purpose: Calls render and handles the camera movement
    public void gameLoop() {
        world = new Chunk(worldX, worldY, worldZ);
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f; // length of the frame
        float lastTime = 0.0f; // when the last frame rendered
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed = 0.35f;
        Mouse.setGrabbed(true); // hide the mouse
        while(!isCloseRequested()) {
            time = Sys.getTime();
            lastTime = time;
            // distance in mouse movement from the last getDX() call
            dx = Mouse.getDX();
            // distance in mouse movement from the last getDY() call
            dy = Mouse.getDY();
            // control camera yaw from x movement from the mouse 
            yaw(dx * mouseSensitivity);
            pitch(dy * mouseSensitivity);
            
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) { // move foward
                walkForward(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) { // move backward
                walkBackwards(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) { // strafe left
                strafeLeft(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) { // strafe right
                strafeRight(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { // move up
                moveUp(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) { // move down
                moveDown(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_R)) { // randomize world
                world.randomize();
            }
            
            glLoadIdentity();
            lookThrough();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            render();
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }
    
    private void toggleLightMode() {
        try {
            int lightMode = this.lightMode + 1;
            switch (lightMode) {
                case 1:
                    lightOffsetZ = 0;
                    this.lightMode = LightMode.HALF_LIT.ordinal();
                    break;
                case 2:
                    lightOffsetZ = 2 * worldZ;
                    this.lightMode = LightMode.DIM_LIGHT.ordinal();
                    break;
                default:
                    lightOffsetZ = -2 * worldZ;
                    this.lightMode = LightMode.FULL_LIT.ordinal();
                    break;
            }
            Thread.sleep(250); //quick work around, should use delta time instead
        } catch (InterruptedException ex) {
            Logger.getLogger(FPCameraController.class.getName()).log(
                Level.SEVERE, "Fail to delay light toggle", ex);
        }
    }
    
    // method: render
    // purpose: Render primitives to the camera
    private void render() {
        world.render();
    }
    
    // method: isCloseRequested
    // purpose: Return true if the window close event is triggered
    private boolean isCloseRequested() {
        return Display.isCloseRequested() ||
               Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }
}
