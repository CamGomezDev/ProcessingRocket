package com.processing.rocket;

import processing.core.*;


public class Floor {
    PVector pos;
    float height, widthX, widthZ;
    MainPApplet p;
    float fricCoeff = 0.25f;
    PImage txtr;

    public Floor(MainPApplet parent) {
        p = parent;
        height = 8*p.scl;
        widthX = 50*p.scl;
        widthZ = 60*p.scl;
        pos = new PVector(0,-height/2,0);
        txtr = p.loadImage("../res/platform.jpg");
    }

    public void render() {
        p.strokeWeight(1);
        p.stroke(1);
        p.pushMatrix();
        p.translate(pos.x, pos.y, pos.z);
        p.fill(147);
        p.box(widthX, height, widthZ);
        p.beginShape();
        p.texture(txtr);
        // top left
        p.vertex( + widthX/2, + height/2 + 1, + widthZ/2, 0, 0);
        // top right
        p.vertex( + widthX/2, + height/2 + 1, - widthZ/2, 600, 0);
        // bottom right
        p.vertex( - widthX/2, + height/2 + 1, - widthZ/2, 600, 500);
        // bottom right
        p.vertex( - widthX/2, + height/2 + 1, + widthZ/2, 0, 500);
        p.endShape();
        p.popMatrix();
        p.noStroke();
    }

    public boolean collided(PVector pt) {
        float tol = 0.1f*p.scl;
        if(pt.y <= pos.y + height/2 + tol && pt.x > pos.x - widthX/2 && pt.x < pos.x + widthX/2 &&
                pt.z > pos.z - widthZ/2 && pt.z < pos.z + widthZ/2) {
            return true;
        }
        return false;
    }
}

