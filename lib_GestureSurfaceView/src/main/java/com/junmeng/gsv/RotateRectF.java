package com.junmeng.gsv;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;


/**
 * 带旋转角度的矩形
 */
public class RotateRectF extends RectF {

    private float rotate = 0;

    public RotateRectF() {
        super();
    }

    public RotateRectF(RectF rectF) {
        super(rectF);
    }

    public RotateRectF(RectF rectF, float rotate) {
        super(rectF);
        this.rotate = rotate;
    }

    public RotateRectF(Rect rect) {
        super(rect);
    }

    public RotateRectF(Rect rect, float rotate) {
        super(rect);
        this.rotate = rotate;
    }

    public RotateRectF(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    public RotateRectF(float left, float top, float right, float bottom, float rotate) {
        super(left, top, right, bottom);
        this.rotate = rotate;
    }

    /**
     * 设置旋转角度，正为顺时针，负为逆时针
     *
     * @param angle
     */
    public void setRotate(float angle) {
        this.rotate = angle;
    }

    /**
     * 获得旋转角度，正为顺时针，负为逆时针
     */
    public float getRotate() {
        return this.rotate;
    }

    @Override
    public boolean contains(float x, float y) {
        float angle = rotate % 360;
        if (angle < 0.000005 && angle > -0.000005) {
            return super.contains(x, y);
        }
        PointF p = CalculateUtil.getPointAfterRotate(new PointF(x, y), getCenterPointF(), -angle);
        return super.contains(p.x, p.y);

    }

    /**
     * 获得当前矩形宽度
     *
     * @return
     */
    public float getWidth() {
        return Math.abs(right - left);
    }

    /**
     * 获得当前矩形高度
     *
     * @return
     */
    public float getHeight() {
        return Math.abs(bottom - top);
    }

    /**
     * 获得当前矩形的中心点坐标
     *
     * @return
     */
    public PointF getCenterPointF() {
        PointF p = new PointF();
        p.x = left + getWidth() / 2.0f;
        p.y = top + getHeight() / 2.0f;
        return p;
    }

    /**
     * 获得矩形左上点坐标
     *
     * @return
     */
    public PointF getLeftTopPoint() {

        PointF p = CalculateUtil.getPointAfterRotate(new PointF(left, top), getCenterPointF(), this.rotate);
        return p;
    }

    public PointF getLeftDownPoint() {

        PointF p = CalculateUtil.getPointAfterRotate(new PointF(left, bottom), getCenterPointF(), rotate);
        return p;
    }

    public PointF getRightTopPoint() {

        PointF p = CalculateUtil.getPointAfterRotate(new PointF(right, top), getCenterPointF(), rotate);
        return p;
    }

    public PointF getRightDownPoint() {

        PointF p = CalculateUtil.getPointAfterRotate(new PointF(right, bottom), getCenterPointF(), rotate);
        return p;
    }


    @Override
    public boolean contains(float left, float top, float right, float bottom) {
        float angle = rotate % 360;
        if (angle < 0.000005 && angle > -0.000005) {
            return super.contains(left, top, right, bottom);
        }
        PointF p =CalculateUtil. getPointAfterRotate(new PointF(left, top), getCenterPointF(), -angle);
        PointF p2 = CalculateUtil.getPointAfterRotate(new PointF(right, bottom), getCenterPointF(), -angle);
        return super.contains(p.x, p.y, p2.x, p2.y);
    }

    @Override
    public boolean contains(RectF r) {
        return contains(r.left, r.top, r.right, r.bottom);
    }
}