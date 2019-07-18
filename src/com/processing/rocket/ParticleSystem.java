package com.processing.rocket;

import processing.core.*;

public class ParticleSystem {
    int iter, partNum, partLife, partsIter, partColor;
    float partRad;
    boolean allActive = false;
    Particle[] parts;
    MainPApplet p;


    public ParticleSystem(float radArea, float spreadAngle, boolean main, MainPApplet parent) {
        p = parent;
        iter = 0;
        if (main) {
            partLife = p.max(1, (int)(7f*p.fps/12f));
            partNum = 100;
            partRad = 0.5f*p.scl;
            partColor = p.color(255, 196, 58);
        } else {
            partLife = p.max(1, (int)(p.fps/6f));
            partNum = 30;
            partRad = 0.2f*p.scl;
            partColor = 220;
        }

        parts = new Particle[partNum];
        partsIter = partNum/partLife;

        for (int i = 0; i < parts.length; i++) {
            float origX = p.randomGaussian()*radArea/2f;
            float origZ = p.randomGaussian()*p.sqrt(p.pow(radArea, 2) - p.pow(origX, 2f))/2f;
            parts[i] = new Particle(origX, origZ, radArea, spreadAngle, partRad, partColor, p);
        }
    }

    public void updateNrender(PVector pos, Quaternion dir, float angle, PVector axis) {
        p.pushMatrix();
        p.translate(pos.x, pos.y, pos.z);
        int fin ;
        if(allActive) {
            fin = parts.length;
        } else {
            fin = (iter + 1)*partsIter;
        }

        for (int i = 0; i < fin; i++) {
            parts[i].update(partLife);
            parts[i].render(dir, angle, axis);
        }

        if(iter < partLife - 1) {
            iter = iter + 1;
        } else {
            allActive = true;
        }
        p.popMatrix();
    }

    void stop() {
        for (int i = 0; i < parts.length; i++) {
            parts[i].life = 0;
            parts[i].pos = parts[i].orig.copy();
        }
        allActive = false;
        iter = 0;
    }
}
