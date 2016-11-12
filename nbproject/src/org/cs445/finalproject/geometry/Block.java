/**
 * *************************************************************
 * file: Block.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project 
 * date last modified: 11/4/16
 *
 * purpose: Representation of a single Voxel
 *
 ***************************************************************
 */
package org.cs445.finalproject.geometry;

public class Block {
    
    public enum Type {
        Grass(0),
        Sand(1),
        Water(2),
        Dirt(3),
        Stone(4),
        Bedrock(5);
        
        private final int id;
        
        Type(int id) {
            this.id = id;
        }
        
        // method: getId
        // purpose: returns the id
        public int getId() {
            return id;
        }
    }
    
    private boolean active;
    private final Type type;
    private float x, y, z;
    
    public Block(Type type) {
        this.type = type;
    }
    
    // method: setCoords
    // purpose: Set the location of the Block
    public void setCoords(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // method: setActive
    // purpose: Configure if the Block should be rendered
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // method: getTypeId
    // purpose: Returns the id of the Block's type
    public int getTypeId() {
        return type.getId();
    }
}
