# VoxelScape

A 3D game composed of Voxels, similiar to MineCraft, made for a Computer Graphics course.

## Development

### Requirements
- [NetBeans 8.1](https://netbeans.org/downloads/)
- [LWJGL 2.9.2](https://sourceforge.net/projects/java-game-lib/files/Official%20Releases/LWJGL%202.9.2/)
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Importing project

> Libraries and be found in the "libaries/" folder when you clone this repository.

1. Clone this (https://github.com/psychobolt/voxelscape.git) repository.
2. Open "nbproject".
3. Add library LWJGL-2.9.2. See [wiki guide](http://wiki.lwjgl.org/wiki/Setting_Up_LWJGL_with_NetBeans).
4. Go to Tools > Libraries > Select LWJGL-2.9.2 > Add Folder e.g. "[...]voxelScape/libraries/lwjgl-2.9.2/native/[yourPlatform]/".
5. Go to Tools > Libraries > Create a new libarary named "Slick2D" > Add JAR e.g. "[...]/voxelscape/libraries/slick-util.jar".

## Controls

| Key        | Action          |
| -----------|:---------------:|
| W          | Move forward    |
| S          | Move backward   |
| A          | Strafe left     |
| D          | Strafe right    |
| Space      | Move up         |
| Left-Shift | Move down       |
| R          | Randomize world |