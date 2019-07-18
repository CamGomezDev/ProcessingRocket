package com.processing.rocket;

import org.ejml.simple.SimpleMatrix;
import processing.core.*;
import java.util.ArrayList;

public abstract class RigidBody {
    PMatrix3D liiT, wiiT;
    float mass, invMass;
    PVector pos, vel, dir, rot;
    Quaternion orien;
    ArrayList<PVector> colPts = new ArrayList<PVector>();

    PVector forces, torques;
    MainPApplet p;

    public RigidBody() {
        forces = new PVector(0,0,0);
        torques = new PVector(0,0,0);

        pos = new PVector(0,0,0);
        vel = new PVector(0,0,0);
        orien = new Quaternion();
        dir = new PVector(0,1,0);
        rot = new PVector(0,0,0);
    }

    public void addTorque(PVector force, PVector location) {
        torques.add(location.cross(force));
    }

    public void update() {
        updateOrientations();
        addEasyForcesAndTorques();
        processCollisionsAndFriction();

        // Actualizar posición con aceleración
        PVector acc = PVector.mult(forces, invMass);
        vel.add(PVector.mult(acc, MainPApplet.dt));
        pos.add(PVector.mult(vel, MainPApplet.dt));

        // Actualizar orientación usando torques
        calcWorldInverseInertiaTensor();
        PVector angAcc = new PVector();
        angAcc = wiiT.mult(torques, angAcc); // angAcc = I^-1 * torque, I = inertia tensor
        rot.add(PVector.mult(angAcc, MainPApplet.dt));
        Quaternion wdt = new Quaternion(rot);
        wdt.mult(orien).rescale(0.25f*MainPApplet.dt*rot.mag());
        orien.add(wdt);

        // Obtener puntos de colisión
        updateCollisionPoints();

        // This because i'm fucking lazy
        avoidSink();
        stopIfVelLow();

        // Restart shit
        orien.normalize();
        dir = orien.mult(0,1,0);
        forces = new PVector(0,0,0);
        torques = new PVector(0,0,0);
    }

    public abstract void updateOrientations();

    public abstract void addEasyForcesAndTorques();

    public void processCollisionsAndFriction() {
        int n = colPts.size();

        /* NORMALES PARA COLISIONES */

        SimpleMatrix A1 = new SimpleMatrix(n, n);
        SimpleMatrix b1 = new SimpleMatrix(n, 1);

        // Valores de la matriz de inercia inversa
        float I_11 = wiiT.m00;
        float I_12 = wiiT.m01;
        float I_13 = wiiT.m02;
        float I_31 = wiiT.m20;
        float I_32 = wiiT.m21;
        float I_33 = wiiT.m22;

        for (int i = 0; i < n; ++i) {
            float r_ix = colPts.get(i).x;
            float r_iz = colPts.get(i).z;
            for (int j = 0; j < n; ++j) {
                float r_jx = colPts.get(j).x;
                float r_jz = colPts.get(j).z;
                float coeff = (invMass - r_ix*I_31*r_jz + r_ix*I_33*r_jx + r_iz*I_11*r_jz - r_iz*I_13*r_jx)*MainPApplet.dt;
                A1.setRow(i, j, coeff);
            }
            float coeff = vel.y + forces.y*invMass*MainPApplet.dt
                    + rot.z*r_ix + I_31*r_ix*torques.z*MainPApplet.dt + I_32*r_ix*torques.y*MainPApplet.dt + I_33*r_ix*torques.x*MainPApplet.dt
                    - rot.x*r_iz - I_11*r_iz*torques.z*MainPApplet.dt - I_12*r_iz*torques.y*MainPApplet.dt - I_13*r_iz*torques.x*MainPApplet.dt;
            b1.setRow(i,0,-coeff);
        }

        SimpleMatrix N1 = new SimpleMatrix(n,1);
        try {
            N1 = A1.solve(b1);
            for (int i = 0; i < n; ++i) {
                if(N1.get(i,0) < 0) {
                    N1.setRow(i,0,0);
                }
                if(N1.get(i) > Math.pow(10,10)) {
                    throw new Exception("Normal muy grande.");
                }
            }
        } catch (Exception e)  {
            System.out.println(e);
            float normalPerColPt = ((0f - vel.y)*mass/MainPApplet.dt - forces.y)/n;
            N1 = new SimpleMatrix(n,1);
            for (int i = 0; i < n; ++i) {
                N1.setRow(i,0,normalPerColPt);
            }
        }

        // Añade fuerza de colisión
        for (int i = 0; i < n; ++i) {
            forces.add(new PVector(0f, (float)N1.get(i,0), 0f));
            addTorque(new PVector(0f, (float)N1.get(i,0), 0f), colPts.get(i));
        }

        /* FRICCIÓN (tal vez innecesaria) */
        PVector friction = new PVector(0,0,0);
        for (int i = 0; i < n; ++i) {
            PVector frictionPt = new PVector(vel.x + rot.y*colPts.get(i).z - rot.z*colPts.get(i).y,
                                             0,
                                             vel.z + rot.x*colPts.get(i).y - rot.y*colPts.get(i).x);

            if(Math.abs(frictionPt.x) > 0.01*MainPApplet.scl || Math.abs(frictionPt.z) > 0.01*MainPApplet.scl) {
                frictionPt.normalize();
                frictionPt.mult((float)-N1.get(i,0)*p.floor.fricCoeff);

                friction.add(frictionPt);
            }
        }
        forces.add(friction);
    }

    public void calcWorldInverseInertiaTensor() {
        PMatrix3D transfMatrix = orien.toMatrix();

        PMatrix3D wiiTtemp = new PMatrix3D();
        wiiTtemp.set(transfMatrix);

        PMatrix3D transfMatrixTrans = new PMatrix3D(); // transpose of tranformation matrix
        transfMatrixTrans.set(transfMatrix);
        transfMatrixTrans.transpose();

        PMatrix3D liiTtemp = new PMatrix3D();
        liiTtemp.set(liiT);

        liiTtemp.apply(transfMatrixTrans);  // I^-1 * T^t  -> I = inertia tensor, T = transformation matrix
        wiiTtemp.apply(liiTtemp); // T * I^-1 * T^t

        wiiT = wiiTtemp.get();
    }

    public abstract void updateCollisionPoints();

    void avoidSink() {
        boolean foundOne = false;
        PVector lowest = new PVector(0,0,0);

        for (int i = 0; i < colPts.size(); ++i) {
            PVector ptPos = PVector.add(pos, colPts.get(i));
            if(p.floor.collided(ptPos)) {
                foundOne = true;
                if(ptPos.y < lowest.y) {
                    lowest = ptPos.copy();
                }
            }
        }
        if(foundOne) {
            // p.println("Avoid sink", p.floor.pos.y + p.floor.height/2 - lowest.y);
            pos.add(new PVector(0,p.floor.pos.y + p.floor.height/2 - lowest.y,0));
        }
    }

    void stopIfVelLow() {
        if(Math.abs(vel.x) < 0.01*MainPApplet.scl && Math.abs(vel.z) < 0.01*MainPApplet.scl) {
            // println("Stop if vel low");
            vel.x = 0;
            vel.z = 0;
        }
    }
}
