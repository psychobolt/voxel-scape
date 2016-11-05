/***************************************************************
* file: Main.java
* author: Loc Mai, Michael Tran, George Zhang
* class: CS 445 – Computer Graphics
*
* assignment: Final Project
* date last modified: 11/4/2016
*
* purpose: This class creates the main window, renders the scene 
* to OpenGL context, and provides First Person camera controls.
*
****************************************************************/ 
package org.cs445.finalproject;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.cs445.finalproject.camera.FPCameraController;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.GLU;

public class Main {

    // Public Main Window constants
    public static final int WINDOW_WIDTH = 640;
    public static final int WINDOW_HEIGHT = 480;
    public static final String WINDOW_TITLE = "VoxelScape";
    
    private static final Logger LOGGER = 
            Logger.getLogger(Main.class.getSimpleName());
    
    private FPCameraController fp;
    private DisplayMode displayMode;
    
    // method: main
    // purpose: Initialize and calls the start
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }
    
    // method: start
    // purpose: Create a window context with graphics capabilities and render 
    // the scene
    public void start() {
        try {
            createWindow();
            initGL();
            fp = new FPCameraController(0.0f, 0.0f, 0.0f);
            fp.gameLoop();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create window", e);
        }
    }
    
    // method: createWindow
    // purpose: Creates a window with GL context
    // Default: 640 x 480, window mode
    private void createWindow() throws Exception {
        Display.setFullscreen(false);
        DisplayMode modes[] = Display.getAvailableDisplayModes();
        for (DisplayMode mode : modes) {
            if (mode.getWidth() == 640 && 
                mode.getHeight() == 480 &&
                mode.getBitsPerPixel() == 32) {
                displayMode = mode;
                break;
            }
        }
        Display.setDisplayMode(displayMode);
        Display.setTitle(WINDOW_TITLE);
        Display.create();
    }
    
    // method: initGL
    // purpose: Initialize a OpenGL context
    private void initGL() {
        glClearDepth(1.0f);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, displayMode.getWidth() / (float) displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
}
