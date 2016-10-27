/**
 * *************************************************************
 * file: FPCameraController.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project 
 * date last modified: 10/27/16
 *
 * purpose: Controller for the First Person Camera
 *
 ***************************************************************
 */
package org.cs445.finalproject.camera;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class FPCameraController {
    
    private final Vector3f position;
    private final Vector3f lookAt;
    
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    
    public FPCameraController(float x, float y, float z) {
        position = new Vector3f(x, y, z);
        lookAt = new Vector3f(x, y, z);
        lookAt.x = 0.0f;
        lookAt.y = 15.0f;
        lookAt.z = 0.0f;
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
    }
    
    // method: walkBackwards
    // purpose: Move the camera backward relative to its current rotation (yaw)
    public void walkBackwards(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z -= zOffset;
    }
    
    // method: strafeLeft
    // purpose: Strafes the camera left relative to its current rotation (yaw)
    public void strafeLeft(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw - 90));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw - 90));
        position.x -= xOffset;
        position.z += zOffset;
    }
    
    // method: strafeRight
    // purpose: Strafes the camera right relative to its current rotation (yaw)
    public void strafeRight(float distance) {
        float xOffset = distance * (float) Math.sin(Math.toRadians(yaw + 90));
        float zOffset = distance * (float) Math.cos(Math.toRadians(yaw + 90));
        position.x -= xOffset;
        position.z += zOffset;
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
        // translate to the position vecotr's location
        glTranslatef(position.x, position.y, position.z);
    }
    
    // method: gameLoop
    // purpose: Calls render and handles the camera movement
    public void gameLoop() {
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
            if (Keyboard.isKeyDown(Keyboard.KEY_E)) { // move down
                moveDown(movementSpeed);
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
    
    // method: render
    // purpose: Render primitives to the camera
    private void render() {
        try {
            glBegin(GL_QUADS);
                glColor3f(1.0f,0.0f,1.0f);
                glVertex3f(1.0f,-1.0f, -1.0f);
                glVertex3f(-1.0f,-1.0f, -1.0f);
                glVertex3f(-1.0f, 1.0f, -1.0f);
                glVertex3f(1.0f, 1.0f, -1.0f); 
            glEnd();
        } catch (Exception e) {
            
        }
    }
    
    // method: isCloseRequested
    // purpose: Return true if the window close event is triggered
    private boolean isCloseRequested() {
        return Display.isCloseRequested() ||
               Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }
}
