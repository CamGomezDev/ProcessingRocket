package com.processing.rocket;

import com.sun.tools.javac.Main;
import processing.core.*;

public class PerlinNoiseSea {
    PVector pos;
    int height, widthX, widthZ;
    int num = 90;
    MainPApplet p;

    PVector[][] terrain = new PVector[num][num];

    float offsetX = 0, offsetZ = 0;
    float flyingX = 0, flyingZ = 0;

    public PerlinNoiseSea(MainPApplet parent) {
        p = parent;

        pos = p.floor.pos.copy();

        height = 10*p.scl;
        widthX = 600*p.scl;
        widthZ = 600*p.scl;

        for (int i = 0; i < num; i++) {
            for (int k = 0; k < num; k++) {
                terrain[i][k] = new PVector(i*widthX/num,0,k*widthZ/num);
            }
        }

    }

    public void update() {
        offsetX = flyingX;

        for (int i = 0; i < num; i++) {
            offsetZ = flyingZ;
            for (int k = 0; k < num; k++) {
                terrain[i][k].y = p.map(p.noise(offsetX, offsetZ),0,1,-height/2,height/2);
                offsetZ += 0.15;
            }
            offsetX += 0.18;
        }

        flyingX -= 0.01;
        flyingZ += 0.005;
    }

    public void render() {
        p.pushMatrix();
        p.translate(pos.x - widthX/2, pos.y, pos.z - widthZ/2);
        p.strokeWeight(1);
        p.fill(p.color(63, 128, 234));
        for (int i = 0; i < num - 1; i++) {
            p.beginShape(p.TRIANGLE_STRIP);
            for (int k = 0; k < num; k++) {
                float col = p.map(terrain[i][k].y,-height/2,height/2,100,255);
                p.stroke(col, col, col, 40);
                p.vertex(terrain[i][k].x, terrain[i][k].y, terrain[i][k].z);
                p.vertex(terrain[i+1][k].x, terrain[i+1][k].y, terrain[i][k].z);
            }
            p.endShape();
        }
        p.popMatrix();
    }
}
