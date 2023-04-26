package fr.snapgames.demo.core.math;

import java.util.List;
import java.util.Objects;

/**
 * Vector2D
 */
public class Vector2D {
    public double x, y;

    public Vector2D() {
        x = 0.0f;
        y = 0.0f;
    }

    /**
     * @param x
     * @param y
     */
    public Vector2D(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    public Vector2D substract(Vector2D v1) {
        return new Vector2D(x - v1.x, y - v1.y);
    }

    public Vector2D multiply(double f) {
        return new Vector2D(x * f, y * f);
    }

    public double dot(Vector2D v1) {

        return v1.x * y + v1.y * x;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double distance(Vector2D v1) {
        return substract(v1).length();
    }

    public Vector2D divide(double f) {
        return new Vector2D(x / f, y / f);
    }

    public Vector2D normalize() {
        return divide(length());
    }

    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    public double angle(Vector2D v1) {
        double vDot = this.dot(v1) / (this.length() * v1.length());
        if (vDot < -1.0)
            vDot = -1.0;
        if (vDot > 1.0)
            vDot = 1.0;
        return Math.acos(vDot);

    }

    public Vector2D addAll(List<Vector2D> forces) {
        Vector2D sum = new Vector2D();
        for (Vector2D f : forces) {
            sum = sum.add(f);
        }
        return sum;
    }

    public String toString() {
        return String.format("{x:%04.2f,y:%04.2f}", x, y);
    }

    public Vector2D maximize(double maxAccel) {
        if (Math.abs(x) > maxAccel) {
            x = Math.signum(x) * maxAccel;
        }
        if (Math.abs(y) > maxAccel) {
            y = Math.signum(y) * maxAccel;
        }
        return this;
    }

    public Vector2D maximize(double maxX, double maxY) {
        if (Math.abs(x) > maxX) {
            x = Math.signum(x) * maxX;
        }
        if (Math.abs(y) > maxY) {
            y = Math.signum(y) * maxY;
        }
        return this;
    }

    public Vector2D ceil(double ceilThreshod) {
        x = Math.copySign((Math.abs(x) < ceilThreshod ? 0 : x), x);
        y = Math.copySign((Math.abs(x) < ceilThreshod ? 0 : y), y);
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Vector2D vo = (Vector2D) o;
        return Objects.equals(x, vo.x) && Objects.equals(y, vo.y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
