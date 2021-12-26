package com.baidu.idl.main.facesdk.attendancelibrary.attendance;

public class Location {
    /**
     * x 坐标位置
     */
    private double x;
    /**
     * y 坐标位置
     */
    private double y;

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    @Override
    public String toString() {
        return "FaceDetectLandmark_Location [x=" + x + ", y=" + y + "]";
    }
}
