/**
 * *************************************************************
 * file: Vector3i.java
 * author: Loc Mai, Michael Tran, George Zhang
 * class: CS 445 – Computer Graphics
 *
 * assignment: Final Project 
 * date last modified: 11/20/16
 *
 * purpose: Representation of a integer vector (x, y, z)
 *
 ***************************************************************
 */
package org.cs445.finalproject.geometry;


public class Vector3i {
    
    public int x;
    public int y;
    public int z;
    
    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.y;
        hash = 41 * hash + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vector3i other = (Vector3i) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }
}
