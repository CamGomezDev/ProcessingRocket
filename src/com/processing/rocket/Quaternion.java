package com.processing.rocket;

import processing.core.*;
import java.lang.Math;

class Quaternion {
    float x = 0f; float y = 0f; float z = 0f;
    float w = 1f;

    Quaternion() { }

    Quaternion(float x, float y, float z, float w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }

    Quaternion(Quaternion q) {
        x = q.x; y = q.y; z = q.z; w = q.w;
    }

    // Construct from axis-angle.
    Quaternion(float angle, PVector axis) {
        set(angle, axis);
    }

    Quaternion(PVector v) {
        x = v.x; y = v.y; z = v.z; w = 0;
    }

    String convertToString() {
        return String.format("[ %+.2f, %+.2f, %+.2f, %+.2f ]",
                x, y, z, w);
    }

    Quaternion add(Quaternion q) {
        x += q.x; y += q.y; z += q.z; w += q.w;
        return this;
    }

    boolean approx(Quaternion q, float tolerance) {
        return Math.abs(x - q.x) <= tolerance
                && Math.abs(y - q.y) <= tolerance
                && Math.abs(z - q.z) <= tolerance
                && Math.abs(w - q.w) <= tolerance;
    }

    float dot(Quaternion b) {
        return x * b.x + y * b.y + z * b.z + w * b.w;
    }

    Quaternion mult(float scalar) {
        x *= scalar; y *= scalar; z *= scalar; w *= scalar;
        return this;
    }

    // Apply to point.
    PVector mult(PVector v) {
        return mult(v, new PVector());
    }

    PVector mult(float x, float y, float z) {
        return mult(new PVector(x, y, z));
    }

    PVector mult(PVector v, PVector out) {
        float ix = w * v.x + y * v.z - z * v.y;
        float iy = w * v.y + z * v.x - x * v.z;
        float iz = w * v.z + x * v.y - y * v.x;
        float iw = -x * v.x - y * v.y - z * v.z;
        out.x = ix * w + iw * -x + iy * -z - iz * -y;
        out.y = iy * w + iw * -y + iz * -x - ix * -z;
        out.z = iz * w + iw * -z + ix * -y - iy * -x;
        return out;
    }

    Quaternion mult(Quaternion q) {
        float ix = w * q.x + q.w * x + y * q.z - z * q.y;
        float iy = w * q.y + q.w * y + z * q.x - x * q.z;
        float iz = w * q.z + q.w * z + x * q.y - y * q.x;
        float iw = w * q.w - (x * q.x + y * q.y + z*q.z);
        x = ix;
        y = iy;
        z = iz;
        w = iw;
        return this;
    }

    Quaternion normalize() {
        float mag = x * x + y * y + z * z + w * w;
        if (mag != 0.0 && mag != 1.0) {
            mag = 1f / (float)Math.sqrt(mag);
            x *= mag; y *= mag; z *= mag; w *= mag;
        }
        return this;
    }

    Quaternion rescale(float scalar) {
        float mag = x * x + y * y + z * z + w * w;
        if (mag == 0.0) {
            return this;
        } else if (mag == 1.0) {
            x *= scalar; y *= scalar; z *= scalar; w *= scalar;
            return this;
        }
        mag = scalar / (float)Math.sqrt(mag);
        x *= mag; y *= mag; z *= mag; w *= mag;
        return this;
    }

    Quaternion set(float x, float y, float z, float w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
        return this;
    }

    Quaternion set(Quaternion q) {
        x = q.x; y = q.y; z = q.z; w = q.w;
        return this;
    }

    // Set from axis-angle.
    Quaternion set(float angle, PVector axis) {
        float halfangle = 0.5f * angle;
        float sinhalf = (float)Math.sin(halfangle);
        x = axis.x * sinhalf; y = axis.y * sinhalf;
        z = axis.z * sinhalf; w = (float)Math.cos(halfangle);
        return this;
    }

    Quaternion sub(Quaternion q) {
        x -= q.x; y -= q.y; z -= q.z; w -= q.w;
        return this;
    }

    PMatrix3D toMatrix() {
        return toMatrix(new PMatrix3D());
    }

    PMatrix3D toMatrix(PMatrix3D out) {
        float x2 = x + x; float y2 = y + y; float z2 = z + z;
        float xsq2 = x * x2; float ysq2 = y * y2; float zsq2 = z * z2;
        float xy2 = x * y2; float xz2 = x * z2; float yz2 = y * z2;
        float wx2 = w * x2; float wy2 = w * y2; float wz2 = w * z2;
        out.set(
                1f - (ysq2 + zsq2), xy2 - wz2, xz2 + wy2, 0f,
                xy2 + wz2, 1f - (xsq2 + zsq2), yz2 - wx2, 0f,
                xz2 - wy2, yz2 + wx2, 1f - (xsq2 + ysq2), 0f,
                0f, 0f, 0f, 1f
        );
        return out;
    }
}
