package com.processing.rocket;

import processing.core.*;

public class Camera {
    PVector eye, center, up, prevMouse;
    float velMag, angVel, veldt;
    MainPApplet p;
    boolean[] moving = {false, false, false, false, false, false};

    public Camera(MainPApplet parent) {
        p = parent;

        velMag = 20*p.scl;
        veldt = velMag*p.dt;
        angVel = 5;

        // Cámara empieza cerca
        eye = new PVector(-100*p.scl, 20*p.scl, 0);
        center = new PVector(0, 20*p.scl, 0);
        up = new PVector(0,-1,0);

        // Cámara empieza lejos
        eye = new PVector(-199*p.scl,53*p.scl,0);
        center = new PVector(-51*p.scl,77*p.scl,0);
        up = new PVector(0,-1,0);

        p.camera(eye.x, eye.y, eye.z,
                 center.x, center.y, center.z,
                 up.x, up.y, up.z);

        prevMouse = new PVector(p.mouseX, p.mouseY);
    }

    public void update() {
        moveField();
        movePlace();
        p.camera(eye.x, eye.y, eye.z,
                center.x, center.y, center.z,
                up.x, up.y, up.z);
        prevMouse.x = p.mouseX;
        prevMouse.y = p.mouseY;
    }

    public void moveField() {
        float fieldScl = 4f;
        float xMov = p.mouseX/fieldScl - prevMouse.x/fieldScl;
        float yMov = p.mouseY/fieldScl - prevMouse.y/fieldScl;

        if(yMov > 400f/fieldScl || yMov < -400f/fieldScl) {
            yMov = 0;
            xMov = 0;
        }
        if(xMov > 300f/fieldScl || xMov < -300f/fieldScl) {
            xMov = 0;
            yMov = 0;
        }

        PVector sight = PVector.sub(center, eye);
        PVector sightXZ = new PVector(sight.x, sight.z);
        float magXZ = sightXZ.mag();
        float thetaX = p.atan(xMov/magXZ);
        sightXZ.rotate(-thetaX*angVel);
        sightXZ.z = sightXZ.y;
        sightXZ.y = sight.y;
        center = PVector.add(eye, sightXZ);

        sight = PVector.sub(center, eye);
        float thetaXZ = p.atan(sight.z/sight.x);
        PVector sightMY = new PVector(magXZ, sight.y);
        float magMY = sightMY.mag();
        float thetaY = p.atan(yMov/magMY);
        sightMY.rotate(-thetaY*angVel);

        center.x = eye.x + sightMY.x*p.cos(thetaXZ);
        center.y = eye.y + sightMY.y;
        center.z = eye.z + sightMY.x*p.sin(thetaXZ);
    }

    public void movePlace() {
        boolean isMoving = false;
        for (boolean dirMove : moving) {
            if(dirMove) {
                isMoving = true;
            }
        }

        if(isMoving) {
            PVector[] sights = {new PVector(0,0), new PVector(0,0), new PVector(0,0), new PVector(0,0), new PVector(0,0), new PVector(0,0)};
            PVector sight = PVector.sub(center, eye);
            sight = new PVector(sight.x, sight.z);
            for (int i = 0; i < moving.length; i++) {
                if(moving[i]) {
                    switch (i) {
                        case 0: sights[i] = sight.copy().rotate(p.HALF_PI); break;
                        case 1: sights[i] = sight.copy().rotate(-p.HALF_PI); break;
                        case 2: sights[i] = sight.copy(); break;
                        case 3: sights[i] = sight.copy().mult(-1); break;
                        case 4: sights[i] = new PVector(0,1,0).mult(sight.mag()); break;
                        case 5: sights[i] = new PVector(0, -1, 0).mult(sight.mag()); break;
                    }
                }
            }

            PVector vel = new PVector(0, 0);
            for (int i = 0; i < sights.length; i++) {
                if(i < 4) {
                    vel.add(new PVector(sights[i].x, 0, sights[i].y));
                } else {
                    vel.add(sights[i]);
                }
            }

            vel.normalize().mult(veldt);
            center.add(vel);
            eye.add(vel);

        }
    }

    public void keyInteract(boolean pressed) {
        switch (p.key) {
            case 'a': moving[0] = pressed; break;
            case 'd': moving[1] = pressed; break;
            case 'w': moving[2] = pressed; break;
            case 's': moving[3] = pressed; break;
            case ' ': moving[4] = pressed; break;
            case 'z':
                for (int i = 0; i < moving.length; i++) {
                    moving[i] = false;
                }
                break;
        }

        if(p.key == p.CODED) {
            if (p.keyCode == p.SHIFT) {
                moving[5] = pressed;
            }
        }
    }
}
