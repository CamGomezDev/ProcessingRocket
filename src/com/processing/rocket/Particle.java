package com.processing.rocket;

import processing.core.*;

public class Particle {
    PVector veldt, orig, pos;
    float velMag, rad;
    int life = 0;
    int clr;
    MainPApplet p;

    public Particle(float origX, float origZ, float areaRad, float spreadAngle, float partRad, int col, MainPApplet parent) {
        p = parent;
        clr = col;
        rad = partRad;
        velMag = 15*p.scl;
        orig = new PVector(origX, 0, origZ);
        pos = orig.copy();
        float angleX = orig.x*spreadAngle/areaRad;
        float angleZ = orig.z*spreadAngle/areaRad;
        veldt = new PVector(p.sin(angleX), 1, p.sin(angleZ));
        veldt.normalize().mult(velMag*p.dt);
    }

    void update(int partLife) {
        pos.add(veldt);
        if (life < partLife) {
            life = life + 1;
        }
        if (life == partLife) {
            pos = orig.copy();
            life = 0;
        }
    }

    void render(Quaternion dir, float angle, PVector axis) {
        p.pushMatrix();
        PVector relPos = dir.mult(pos);
        relPos = p.rotateAboutAxis(relPos, angle, axis);
        p.translate(relPos.x, relPos.y, relPos.z);
        p.fill(clr);
        p.sphere(rad);
        p.popMatrix();
    }
}
