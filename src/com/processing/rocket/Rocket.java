package com.processing.rocket;

import processing.core.*;
import java.util.ArrayList;

public class Rocket extends RigidBody {
    // Variables importantes
    float lon, rad;
    boolean[] thrustActive = {false, false, false, false, false};
    PVector axisX, axisY, axisZ;
    PVector[] thrustPos = new PVector[5];
    PVector[] stands      = new PVector[4];
    PVector[] worldStands = new PVector[4];
    PVector[] topCollisionPoints      = new PVector[8];
    PVector[] worldTopCollisionPoints = new PVector[8];

    // Variables para el renderizado
    ParticleSystem[] thrustRender = new ParticleSystem[5];
    PImage txtr;

    public Rocket(MainPApplet parent) {
        p = parent;

        lon = 37*p.scl;
        rad = 1.85f*p.scl;
        mass = 540000f;
        invMass = 1f/mass;

        // init fácil
        // pos = new PVector(0, lon/2 + lon/5, 0);

        // init 2D
        initRandom2D();

        // init 3D
        //initRandom3D();

        liiT = new PMatrix3D(
                (float)(1/(0.25*mass*(Math.pow(lon,2)/3 + Math.pow(rad,2)))), 0, 0, 0,
                0, (float)(1/(0.5*mass*Math.pow(rad, 2))), 0, 0,
                0, 0, (float)(1/(0.25*mass*(Math.pow(lon,2)/3 + Math.pow(rad,2)))), 0,
                0, 0, 0, 1
        );
        calcWorldInverseInertiaTensor();

        thrustRender[0] = new ParticleSystem(rad, (float)Math.PI/8f, true, p);
        for (int i = 1; i < thrustRender.length; i++) {
            thrustRender[i] = new ParticleSystem(0.2f*p.scl, (float)Math.PI/8, false, p);
        }

        txtr = p.loadImage("../res/rocket.jpg");
    }

    public void initRandom2D() {
        // Posición y velocidad inicial arbitraria
        if(Math.random() < 0.5) {
            pos = new PVector(0, lon/2 + lon/5 + 4*lon, p.random(-lon,-lon/2));
            vel = new PVector(0,0,p.random(-5*p.scl,0));
            rot = new PVector(p.random(-0.15f,-0.1f),0,0);
            orien = new Quaternion();
        } else {
            pos = new PVector(0, lon/2 + lon/5 + 4*lon, p.random(lon/2,lon));
            vel = new PVector(0,0,p.random(0,5*p.scl));
            rot = new PVector(p.random(0.1f,0.15f),0,0);
            orien = new Quaternion();
        }

        // Orientación inicial arbitraria
        PVector rot_ini;
        if(rot.x > 0) {
            rot_ini = new PVector(p.random(0.2f,0.3f), 0, 0); // initial random angle
        } else {
            rot_ini = new PVector(p.random(-0.3f,-0.2f), 0, 0); // initial random angle
        }
        Quaternion wdt = new Quaternion(rot_ini); // random angle being multiplied by ang
        wdt.mult(orien).rescale(p.random(0.3f)); // gives a small initial rotation
        orien.add(wdt);
        dir = orien.mult(0,1,0);
    }

    public void initRandom3D() {
        // Posición y vel inicial arbitrarias
        pos = new PVector(p.random(-lon,lon), lon/2 + lon/5 + 4*lon, p.random(-lon,lon));
        vel = new PVector(p.random(-5*p.scl,5*p.scl),0,p.random(-5*p.scl,5*p.scl));
        rot = new PVector(p.random(-0.3f,0.3f),0,p.random(-0.3f,0.3f));
        orien = new Quaternion();

        // Orientación inicial arbitraria
        PVector rot_ini = new PVector(p.random(-1,1), 0, p.random(-1,1)); // initial random angle
        Quaternion wdt = new Quaternion(rot_ini); // random angle being multiplied by ang
        wdt.mult(orien).rescale(p.random(0.3f)); // gives a small initial rotation
        orien.add(wdt);
        dir = orien.mult(0,1,0);
    }

    // Método abstracto en cuerpo rígido
    public void updateOrientations() {
        // Actualizar posición de propulsión
        thrustPos[0] = orien.mult(0, -lon/2f, 0);
        thrustPos[1] = orien.mult(rad, lon*9f/20f, 0);
        thrustPos[2] = orien.mult(0, lon*9f/20f, rad);
        thrustPos[3] = orien.mult(-rad, lon*9f/20f, 0);
        thrustPos[4] = orien.mult(0, lon*9f/20f, -rad);
        // Actualizar ejes
        axisX = orien.mult(1, 0, 0);
        axisY = orien.mult(0, 1, 0);
        axisZ = orien.mult(0, 0, 1);
        // Actualizar soportes y puntos de colisión
        for (int i = 0; i < 4; i++) {
            stands[i] = orien.mult(3*rad*(float)Math.cos(2*i*Math.PI/4 + Math.PI/4), -lon/2 - lon/10, 3*rad*(float)Math.sin(2*i*p.PI/4 + p.PI/4));
            worldStands[i] = PVector.add(stands[i], pos);
        }
        for (int i = 0; i < 8; ++i) {
            topCollisionPoints[i] = orien.mult((float)(rad*Math.cos(i*2*Math.PI/8)), lon/2, (float)(rad*Math.sin(i*2*Math.PI/8)));
            worldTopCollisionPoints[i] = PVector.add(topCollisionPoints[i], pos);
        }
    }

    // Método abstracto en cuerpo rígido
    public void addEasyForcesAndTorques() {
        // Gravedad
        forces.add(new PVector(0, -9.8f*p.scl*mass, 0));

        PVector thrust;
        // Propulsión
        if(thrustActive[0]) {
            float thrustValue = 8000000*p.scl;
            forces.add(dir.copy().mult(thrustValue));
        }

        if(thrustActive[1]) {
            float thrustValue = 800000*p.scl;
            thrust = p.rotateAboutAxis(dir.copy().mult(thrustValue), p.HALF_PI/2f, axisZ);
            forces.add(thrust);
            addTorque(thrust, thrustPos[1]);
        }
        if(thrustActive[2]) {
            float thrustValue = 800000*p.scl;
            thrust = p.rotateAboutAxis(dir.copy().mult(thrustValue), -p.HALF_PI/2f, axisX);
            forces.add(thrust);
            addTorque(thrust, thrustPos[2]);
        }
        if(thrustActive[3]) {
            float thrustValue = 800000*p.scl;
            thrust = p.rotateAboutAxis(dir.copy().mult(thrustValue), -p.HALF_PI/2f, axisZ);
            forces.add(thrust);
            addTorque(thrust, thrustPos[3]);
        }
        if(thrustActive[4]) {
            float thrustValue = 800000*p.scl;
            thrust = p.rotateAboutAxis(dir.copy().mult(thrustValue), p.HALF_PI/2f, axisX);
            forces.add(thrust);
            addTorque(thrust, thrustPos[4]);
        }
    }

    // Método abstracto en cuerpo rígido
    public void updateCollisionPoints() {
        colPts = new ArrayList<PVector>();

        for (int i = 0; i < stands.length; ++i) {
            if(p.floor.collided(worldStands[i])) {
                colPts.add(stands[i].copy());
            }
        }
        for (int i = 0; i < topCollisionPoints.length; ++i) {
            if(p.floor.collided(worldTopCollisionPoints[i])) {
                colPts.add(topCollisionPoints[i].copy());
            }
        }
    }

    public void render() {
        p.pushMatrix();
        p.translate(pos.x, pos.y, pos.z);

        // Ejes de orientación
        p.stroke(255, 0, 0);
        p.line(-10 * p.scl, 0, 0, 10 * p.scl, 0, 0);
        p.stroke(0, 255, 0);
        p.line(0, -10 * p.scl, 0, 0, 10 * p.scl, 0);
        p.stroke(0, 0, 255);
        p.line(0, 0, -10 * p.scl, 0, 0, 10 * p.scl);

        p.fill(204);
        p.noStroke();

        int verts = 64;

        // Parte superior
        PVector top = orien.mult(0, lon/2 + p.scl, 0);
        for (int i = 0; i < verts; i++) {
            p.beginShape();
            PVector firstCorner = orien.mult(rad*(float)Math.cos(2f*p.PI*i/verts), lon/2, rad*(float)Math.sin(2f*p.PI*i/verts));
            p.vertex(firstCorner.x, firstCorner.y, firstCorner.z);
            PVector secCorner = orien.mult(rad*(float)Math.cos(2f*p.PI*(i+1)/verts), lon/2, rad*(float)Math.sin(2f*p.PI*(i+1)/verts));
            p.vertex(secCorner.x, secCorner.y, secCorner.z);
            p.vertex(top.x, top.y, top.z);
            p.endShape();
        }

        // Fuselaje
        for (int i = 0; i < verts; i++) {
            float ang1 = 2f*p.PI*i/verts;
            float ang2 = 2f*p.PI*(i+1)/verts;
            p.beginShape(p.QUADS);
            p.texture(txtr);
            PVector topLeft = orien.mult(rad*(float)Math.cos(ang1), lon/2, rad*(float)Math.sin(ang1));
            p.vertex(topLeft.x, topLeft.y, topLeft.z, i*txtr.width/verts, 0);
            PVector topRight = orien.mult(rad*(float)Math.cos(ang2), lon/2, rad*(float)Math.sin(ang2));
            p.vertex(topRight.x, topRight.y, topRight.z, (i+1)*txtr.width/verts, 0);
            PVector botRight = orien.mult(rad*(float)Math.cos(ang2), -lon/2, rad*(float)Math.sin(ang2));
            p.vertex(botRight.x, botRight.y, botRight.z, (i+1)*txtr.width/verts, 1090);
            PVector botLeft = orien.mult(rad*(float)Math.cos(ang1), -lon/2, rad*(float)Math.sin(ang1));
            p.vertex(botLeft.x, botLeft.y, botLeft.z, i*txtr.width/verts, 1090);
            p.endShape();
        }

        // Inferior
        p.beginShape();
        for (int i = 0; i < verts; ++i) {
            PVector botPoint = orien.mult(rad*(float)Math.cos(2f*p.PI*i/verts), -lon/2, rad*(float)Math.sin(2f*p.PI*i/verts));
            p.vertex(botPoint.x, botPoint.y, botPoint.z);
        }
        p.endShape();

        // Soportes
        p.noFill();
        p.stroke(30);
        p.strokeWeight(2);
        for (int i = 0; i < 4; i++) {
            p.beginShape();
            PVector topVertex = orien.mult(rad*(float)Math.cos(2*i*p.PI/4 + p.PI/4), -lon/2 + lon/10, rad*(float)Math.sin(2*i*p.PI/4 + p.PI/4));
            p.vertex(topVertex.x, topVertex.y, topVertex.z);
            PVector botVertex = stands[i];
            p.vertex(botVertex.x, botVertex.y, botVertex.z);
            PVector midVertex = orien.mult(rad*(float)Math.cos(2*i*p.PI/4 + p.PI/4), -lon/2, rad*(float)Math.sin(2*i*p.PI/4 + p.PI/4));
            p.vertex(midVertex.x, midVertex.y, midVertex.z);
            p.endShape();
        }

        // Mostrar todos los propulsores
        p.noStroke();
        if(thrustActive[0]) {
            thrustRender[0].updateNrender(new PVector(thrustPos[0].x, thrustPos[0].y, thrustPos[0].z), orien, p.PI, axisX);
        }
        if(thrustActive[1]) {
            thrustRender[1].updateNrender(new PVector(thrustPos[1].x, thrustPos[1].y, thrustPos[1].z), orien, -3f*p.PI/4f, axisZ);
        }
        if(thrustActive[2]) {
            thrustRender[2].updateNrender(new PVector(thrustPos[2].x, thrustPos[2].y, thrustPos[2].z), orien, 3f*p.PI/4f, axisX);
        }
        if(thrustActive[3]) {
            thrustRender[3].updateNrender(new PVector(thrustPos[3].x, thrustPos[3].y, thrustPos[3].z), orien, 3f*p.PI/4f, axisZ);
        }
        if(thrustActive[4]) {
            thrustRender[4].updateNrender(new PVector(thrustPos[4].x, thrustPos[4].y, thrustPos[4].z), orien, -3f*p.PI/4f, axisX);
        }

        p.popMatrix();
    }

    void keyInteract(boolean pressed) {
        if(p.key == 'k') {
            if(pressed) {
                thrustActive[0] = pressed;
            } else {
                thrustRender[0].stop();
                thrustActive[0] = false;
            }
        }
        if(p.key == 'i') {
            if(pressed) {
                thrustActive[1] = pressed;
            } else {
                thrustRender[1].stop();
                thrustActive[1] = false;
            }
        }
        if(p.key == 'j') {
            if(pressed) {
                thrustActive[2] = pressed;
            } else {
                thrustRender[2].stop();
                thrustActive[2] = false;
            }
        }
        if(p.key == 'm') {
            if(pressed) {
                thrustActive[3] = pressed;
            } else {
                thrustRender[3].stop();
                thrustActive[3] = false;
            }
        }
        if(p.key == 'l') {
            if(pressed) {
                thrustActive[4] = pressed;
            } else {
                thrustRender[4].stop();
                thrustActive[4] = false;
            }
        }
    }
}