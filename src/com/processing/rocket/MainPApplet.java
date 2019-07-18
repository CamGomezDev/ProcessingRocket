package com.processing.rocket;

import processing.core.*;

public class MainPApplet extends PApplet {
    Floor floor;
    Camera cam;
    Rocket rocket;
    PerlinNoiseSea sea;
    PShape globe;
    PImage skyTxtr;
    static int scl = 20, fps = 30;
    static float dt = 1f/fps;

    public static void main(String[] args) {
	    PApplet.main(new String[] {"com.processing.rocket.MainPApplet"});
    }

    public void settings() {
        //fullScreen(P3D);
        size(1000, 600, P3D);
        smooth(8);
    }

    public void setup () {
        background(20);

        skyTxtr = loadImage("../res/sky.jpg");
        float cameraZ = ((height/2f) / tan(PI*60f/360f));
        float farthestLength = cameraZ*15f;
        perspective(PI/3f, width*1f/height, cameraZ/10f, farthestLength);
        globe = createShape(SPHERE, farthestLength-1);
        globe.setTexture(skyTxtr);
        globe.setStroke(false);

        floor = new Floor(this);
        cam = new Camera(this);
        rocket = new Rocket(this);
        sea = new PerlinNoiseSea(this);
    }

    public void draw() {
        background(20);
        cam.update();

        pushMatrix();
        translate(cam.eye.x, cam.eye.y, cam.eye.z);
        rotateX(PI);
        rotateY(PI);
        shape(globe);
        popMatrix();

        lights();
        rocket.update();
        rocket.render();
        sea.update();
        sea.render();
        floor.render();
    }

    public PVector rotateAboutAxis(PVector v, float angle, PVector axis) {
        float axisx = axis.x;
        float axisy = axis.y;
        float axisz = axis.z;

        // Find squares of each axis component.
        float xsq = axisx * axisx;
        float ysq = axisy * axisy;
        float zsq = axisz * axisz;

        // Test the axis's magnitude.
        float mag = xsq + ysq + zsq;

        // EPSILON is the smallest positive non-zero amount.
        // Return if the axis has no length or the point is < 0, 0, 0 >.
        if (mag < EPSILON || (v.x == 0.0 && v.y == 0.0 && v.z == 0.0)) {
            return v;
        } else if (mag > 1.0) {
            mag = 1f / sqrt(mag);
            axisx *= mag; axisy *= mag; axisz *= mag;
            xsq = axisx * axisx; ysq = axisy * axisy; zsq = axisz * axisz;
        }

        float cosa = cos(angle);
        float sina = sin(angle);
        float complcos = 1f - cosa;

        float complxy = complcos * axisx * axisy;
        float complxz = complcos * axisx * axisz;
        float complyz = complcos * axisy * axisz;

        float sinx = sina * axisx;
        float siny = sina * axisy;
        float sinz = sina * axisz;

        // Right on the x axis (i).
        float ix = complcos * xsq + cosa; /* m00 */
        float iy = complxy + sinz; /* m10 */
        float iz = complxz - siny; /* m20 */

        // Up on the y axis (j).
        float jx = complxy - sinz; /* m01 */
        float jy = complcos * ysq + cosa; /* m11 */
        float jz = complyz + sinx; /* m21 */

        // Forward on the z axis (k).
        float kx = complxz + siny; /* m02 */
        float ky = complyz - sinx; /* m12 */
        float kz = complcos * zsq + cosa; /* m22 */

        float tempx = v.x; float tempy = v.y;
        v.x = ix * v.x + jx * v.y + kx * v.z;
        v.y = iy * tempx + jy * v.y + ky * v.z;
        v.z = iz * tempx + jz * tempy + kz * v.z;

        return v;
    }

    public void keyPressed() {
        cam.keyInteract(true);
        rocket.keyInteract(true);
        if(key == 'r') {
            rocket = new Rocket(this);
        }
    }

    public void keyReleased() {
        cam.keyInteract(false);
        rocket.keyInteract(false);
    }
}
