package com.junmeng.gsv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

import static com.junmeng.gsv.CalculateUtil.getPointAfterRotate;

/**
 * 此类封装了对设置的图片的手势操作，包括拖动、缩放、旋转
 * 同时为了提供更大的灵活性，开放了大量的接口让用户进行自定义
 * 如果用户有自己的业务逻辑处理，建议继承此类进行扩展
 * 用户如果想自定义绘制效果，可覆盖drawOther方法进行自定义绘制
 * 在此类中已经默认绘制好了地图和中心图标，为了提供更大的灵活性加入drawOther让用户自行绘制，如果用户想全部重绘，则可覆盖doDraw方法
 * Created by HWJ on 2016/12/9.
 */

public class GestureSurfaceView extends BaseSurfaceView {

    private static final String TAG = "GestureSurfaceView";

    public interface OnGestureListener {
        /**
         * @param gesture
         * @param dragX   拖动手势在x上的变化
         * @param dargY   拖动手势在y上的变化
         * @param scale   缩放手势的缩放比例，大于1为放大
         * @param rotate  旋转手势的角度，正为顺时针
         */
        void onGesture(@GestureAction int gesture, float dragX, float dargY, float scale, float rotate);
    }

    /**
     * 拖动
     */
    public static final int GESTURE_DRAG = 0;
    /**
     * 缩放
     */
    public static final int GESTURE_SCALE = 1;
    /**
     * 旋转
     */
    public static final int GESTURE_ROTATE = 2;

    @IntDef({GESTURE_DRAG, GESTURE_SCALE, GESTURE_ROTATE})
    public @interface GestureAction {
    }

    /**
     * 拖动
     */
    public static final int ONTOUCH_MODE_DRAG = 0;
    /**
     * 缩放或旋转
     */
    public static final int ONTOUCH_MODE_SCALE_OR_ROTATE = 1;
    /**
     * 地图坐标系以左上为原点
     */
    public static final int COORDINATE_MAP_LEFT_TOP = 0;
    /**
     * 地图坐标系以左下为原点
     */
    public static final int COORDINATE_MAP_LEFT_DOWN = 1;

    @IntDef({COORDINATE_MAP_LEFT_TOP, COORDINATE_MAP_LEFT_DOWN})
    public @interface CoordinateMap {
    }

    /**
     * 中心点图标以图标中心为中心点
     */
    public static final int CENTER_BITMAP_POSITION_CENTER = 0;
    /**
     * 中心点图标以底部为中心点
     */
    public static final int CENTER_BITMAP_POSITION_BOTTOM = 1;

    @IntDef({CENTER_BITMAP_POSITION_CENTER, CENTER_BITMAP_POSITION_BOTTOM})
    public @interface CenterBitmapPosition {
    }

    private Matrix matrix = new Matrix();

    private Bitmap centerBitmap;//中心点定位位图
    private int centerBitmapPosition = CENTER_BITMAP_POSITION_CENTER;

    private Bitmap mapBitmap;//地图位图
    private RotateRectF mapRectf = new RotateRectF();//地图的rect
    private int srcMapWidth, srcMapHeight;//原地图宽高
    private int mapWidth, mapHeight;//当前图实际宽高（经过缩放的）
    private float mapLeftX, mapLeftY;//图片左上角坐标
    private float mapCenterX, mapCenterY;//图片中心点
    private int centerColor = 0xff0000ff;//中心点颜色
    private int centerSize = 7;//中心点半径
    private float mapRotate = 0;//地图旋转角度
    private float mapScale = 1.0f;//地图缩放比例
    private float minMapScale = 0.5f;
    private float maxMapScale = 6.0f;

    private int mapCoordinate = COORDINATE_MAP_LEFT_TOP;//地图的坐标系原点
    private boolean isShowReference = true;//是否显示坐标系
    private boolean isFirstMapBestFit = true;
    private boolean isMapInit = false;//地图是否已经初始化

    private int scaleSsensitivity = 30;//缩放灵敏度，100最高
    private OnGestureListener onGestureListener;
    private List<PointF> points = new ArrayList<>();//添加在地图上的点,注意此List保存的数据不是真实的点的坐标，需要根据坐标系计算才能得到对应的坐标
    private boolean isDrawAddPoints = true;//是否绘制添加点
    private boolean isDrawCenterIcon = true;//是否绘制中心点图标

    public GestureSurfaceView(Context context) {
        this(context, null, 0);
    }

    public GestureSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGestureSurfaceView();
    }

    private void initGestureSurfaceView() {
        //设置画笔
        paint.setColor(paintColor);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setTextSize(20);
    }

    /**
     * 是否绘制添加的点,默认会把添加的点绘制出来，如果用户想自己处理点的绘制，可在此设置为false
     *
     * @param bool
     */
    public void isDrawAddPoints(boolean bool) {
        isDrawAddPoints = bool;
    }

    /**
     * 是否绘制中心图标，如果用户想自己处理点的绘制，可在此设置为false
     *
     * @param bool
     */
    public void isDrawCenterIcon(boolean bool) {
        isDrawCenterIcon = bool;
    }

    /**
     * 获得添加点的列表
     *
     * @return
     */
    public List<PointF> getPointsList() {
        return points;
    }

    /**
     * 获得最后添加的一个点
     *
     * @return
     */
    public PointF getLastPoint() {
        int size = points.size();
        if (size > 0) {
            return points.get(size - 1);
        }
        return null;
    }

    /**
     * 设置中心点颜色
     */
    public void setCenterColor(@ColorInt int color) {
        this.centerColor = color;
    }

    /**
     * 设置中心点的大小
     *
     * @param radius
     */
    public void setCenterSize(int radius) {
        this.centerSize = radius;
    }

    /**
     * 设置中心点图标
     *
     * @param bitmap
     * @param centerBitmapPosition 指定图标的中心位置
     */
    public void setCenterBitmap(Bitmap bitmap, @CenterBitmapPosition int centerBitmapPosition) {
        Log.i(TAG, "setCenterBitmap:mapWidth=" + bitmap);
        this.centerBitmap = bitmap;
        this.centerBitmapPosition = centerBitmapPosition;
    }

    /**
     * 设置中心点图标，默认以图标中心点为中心位置
     *
     * @param bitmap
     */
    public void setCenterBitmap(Bitmap bitmap) {
        Log.i(TAG, "setCenterBitmap:mapWidth=" + bitmap);
        this.centerBitmap = bitmap;
    }

    /**
     * 设置手势监听器
     *
     * @param listener
     */
    public void setOnGestureListener(OnGestureListener listener) {
        onGestureListener = listener;
    }


    //***********************地图接口**************************start

    /**
     * 将地图缩放到适合窗体大小
     */
    public void setMapBestFit() {

        float sx = screenWidth * 1.0f / mapWidth;
        float sy = screenHeight * 1.0f / mapHeight;
        float s = Math.min(sx, sy);
        Log.i(TAG, "setMapBestFit:s=" + s);
        zoom(s);
        mapMoveToCenter();
    }

    /**
     * 设置是否首次将地图缩放到适合窗体
     */
    public void isFirstMapBestFit(boolean isFit) {
        isFirstMapBestFit = isFit;
    }

    /**
     * 设置地图Bitmap
     *
     * @param bitmap
     */
    public void setMapBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        this.mapBitmap = bitmap;
        srcMapWidth = mapWidth = mapBitmap.getWidth();
        srcMapHeight = mapHeight = mapBitmap.getHeight();
        Log.i(TAG, "setMapBitmap:mapWidth=" + mapWidth + ",mapHeight=" + mapHeight);
        mapRectf.left = 0;
        mapRectf.top = 0;
        mapRectf.right = mapWidth;
        mapRectf.bottom = mapHeight;

        initMapBitmap();
    }

    /**
     * 设置地图的坐标系原点位置(目前提供左上和左下两个原点)
     *
     * @param coordinate
     */
    public void setMapCoordinate(@CoordinateMap int coordinate) {
        mapCoordinate = coordinate;
    }

    /**
     * 将地图移到中心点,保持旋转角度
     */
    public void mapMoveToCenterKeepRotate() {

        float left = (screenWidth - mapWidth) / 2.0f;
        float top = (screenHeight - mapHeight) / 2.0f;
        float dx = left - mapRectf.left;
        float dy = top - mapRectf.top;
        translateMap(dx, dy);
    }

    /**
     * 移动地图
     *
     * @param dx x方向的距离，正表示向右，负表示向左
     * @param dy
     */
    private void translateMap(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        mapCenterX += dx;
        mapCenterY += dy;

        mapLeftX = mapCenterX - mapWidth / 2;
        mapLeftY = mapCenterY - mapHeight / 2;
        mapRectf.left = mapLeftX;
        mapRectf.top = mapLeftY;
        mapRectf.right = mapLeftX + mapWidth;
        mapRectf.bottom = mapLeftY + mapHeight;
    }

    /**
     * 将地图移到中心点，旋转角度为0
     */
    public void mapMoveToCenter() {
        mapMoveToCenterKeepRotate();
        rotate(-mapRotate);
    }

    /**
     * 获得地图矩阵(暂不对外提供)
     *
     * @return
     */
    private Matrix getMapMatrix() {
        return matrix;
    }

    //**************************地图接口********************end

    private void initMapBitmap() {
        if (screenWidth == 0 || screenHeight == 0) {
            return;
        }
        if (isMapInit) {
            return;
        }
        isMapInit = true;
        mapCenterX = screenWidth / 2.0f;
        mapCenterY = screenHeight / 2.0f;
        mapLeftX = (screenWidth - mapWidth) / 2.0f;
        mapLeftY = (screenHeight - mapHeight) / 2.0f;
        Log.i(TAG, "setMapBitmap:mapLeftX=" + mapLeftX + ",mapLeftY=" + mapLeftY);
        matrix.setTranslate(mapLeftX, mapLeftY);
        mapRectf.left = mapLeftX;
        mapRectf.top = mapLeftY;
        mapRectf.right = mapLeftX + mapWidth;
        mapRectf.bottom = mapLeftY + mapHeight;
        if (isFirstMapBestFit) {
            setMapBestFit();
        }
    }


    /**
     * 是否显示坐标系，此坐标系可便于观察和计算
     *
     * @param isShow
     */
    public void showReference(boolean isShow) {
        isShowReference = isShow;
    }

    /**
     * 缩放
     *
     * @param scale 大于0则放大，小于0则缩小
     */
    public void zoom(float scale) {

        float s = this.mapScale * scale;
        if (s < minMapScale || s > maxMapScale) {
            return;
        }
        this.mapScale = s;
        matrix.postScale(scale, scale, mapCenterX, mapCenterY);
        mapWidth = (int) (srcMapWidth * this.mapScale);
        mapHeight = (int) (srcMapHeight * this.mapScale);
        mapLeftX = mapCenterX - mapWidth / 2.0f;
        mapLeftY = mapCenterY - mapHeight / 2.0f;
        mapRectf.left = mapLeftX;
        mapRectf.top = mapLeftY;
        mapRectf.right = mapLeftX + mapWidth;
        mapRectf.bottom = mapLeftY + mapHeight;
    }

    /**
     * 获得缩放值
     *
     * @return
     */
    public float getZoomScale() {
        return this.mapScale;
    }

    /**
     * 获得旋转角度，+表示顺时针 -表示逆时针
     *
     * @return
     */
    public float getRotate() {
        return this.mapRotate;
    }

    /**
     * 设置最小缩放比例
     *
     * @param min
     */
    public void setMinZoomScale(float min) {
        minMapScale = min;
    }

    /**
     * 设置最大缩放比例
     *
     * @param max
     */
    public void setMaxZoomScale(float max) {
        maxMapScale = max;
    }

    /**
     * 设置缩放手势的灵敏度，默认30
     *
     * @param sensitivity
     */
    public void setScaleSensitivity(@IntRange(from = 0, to = 100) int sensitivity) {
        scaleSsensitivity = sensitivity;
    }


    /**
     * 旋转地图
     *
     * @param angle 角度，正则顺时针旋转
     */
    public void rotate(float angle) {
        matrix.postRotate(angle, mapCenterX, mapCenterY);
        this.mapRotate += angle;
        mapRectf.setRotate(this.mapRotate);
    }


    //***********************添加点接口*************************start
    //注意此点是相对点，用于绘制，实际需要convertToUsePoint接口转化为绝对坐标点或相对原图坐标点才有意义
    //同时也提供了反向转换接口，即通过convertToDrawPoint接口将原图坐标转为绘制坐标
    //这里虽然提供了对添加点的相关操作，但实际上希望用户根据自己业务需求保存添加点

    /**
     * 转换添加点的坐标
     *
     * @param point
     * @param isRelative 是否是相对原图坐标，是的话则将点转为原图坐标，否则转为绝对坐标
     */
    public PointF convertToNeedPoint(PointF point, boolean isRelative) {
        PointF calP = new PointF();
        if (isRelative) {
            PointF oCenter = new PointF(mapWidth / 2.0f, mapHeight / 2.0f);//相对中心点
            if (mapCoordinate == COORDINATE_MAP_LEFT_DOWN) {
                calP.x = (oCenter.x + point.x * mapScale) / mapScale;
                calP.y = (oCenter.y - point.y * mapScale) / mapScale;
            } else {
                calP.x = (oCenter.x + point.x * mapScale) / mapScale;
                calP.y = (oCenter.y + point.y * mapScale) / mapScale;
            }
        } else {
            PointF ap = getPointAfterRotate(new PointF(mapCenterX + point.x * mapScale, mapCenterY + point.y * mapScale), new PointF(mapCenterX, mapCenterY), mapRotate);
            calP.x = ap.x;
            calP.y = ap.y;
        }
        return calP;
    }

    /**
     * 将原图坐标转换为绘制用的点坐标
     *
     * @param point
     * @return
     */
    public PointF convertToDrawPoint(PointF point) {
        if (mapCoordinate == COORDINATE_MAP_LEFT_DOWN) {
            return new PointF((point.x - srcMapWidth / 2.0f), (srcMapHeight - point.y - srcMapHeight / 2.0f));
        } else {
            return new PointF((point.x - srcMapWidth / 2.0f), (point.y - srcMapHeight / 2.0f));
        }
    }

    /**
     * 获得当前点
     *
     * @return
     */
    public PointF getCurrentPoint() {
        if (judgePointInside(screenCenterX, screenCenterY)) {
            Log.i(TAG, "addPoint:点在矩形内");
            PointF p = CalculateUtil.getPointAfterRotate(new PointF(screenCenterX, screenCenterY), new PointF(mapCenterX, mapCenterY), -mapRotate);
            float dw = (p.x - mapCenterX) / mapScale;
            float dh = (p.y - mapCenterY) / mapScale;

            Log.i(TAG, "dw=" + dw + ",dh=" + dh);
            return new PointF(dw, dh);

        }
        Log.i(TAG, "addPoint:点不在矩形内");
        return null;
    }

    /**
     * 判断点（绝对坐标）是否在地图的矩形区域内
     *
     * @param x
     * @param y
     * @return
     */
    public boolean judgePointInside(float x, float y) {
        return mapRectf.contains(x, y);
    }


    /**
     * 判断surfaceview中心点是否在地图的矩形区域内
     *
     * @return
     */
    public boolean judgeCenterPointInside() {
        return mapRectf.contains(screenCenterX, screenCenterY);
    }


    /**
     * 添加点
     *
     * @return 点如果在地图区域内则添加成功
     */
    public boolean addPoint() {
        PointF point = getCurrentPoint();
        if (point != null) {
            points.add(point);
            return true;
        }
        return false;
    }

    /**
     * 撤销添加的点
     */
    public void backAddPoint() {
        if (points.size() > 0) {
            points.remove(points.size() - 1);
        }
    }

    /**
     * 清除添加的所有点
     */
    public void cleanAllAddPoints() {
        points.clear();
    }


    //***********************添加点接口*************************end


    private PointF clcikPointf = new PointF();//点击点
    private boolean canDrag = false;//判断是否点击在图片上，否则拖动无效
    private int pointerCount;//手指的个数
    private int mode = ONTOUCH_MODE_DRAG;//0 拖动 1缩放或旋转
    float oldRotation;
    float oldDist;
    float lastDist;//上一次的距离

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Log.i(TAG, "onTouchEvent ");
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onTouchEvent:ACTION_DOWN ");
                clcikPointf.x = event.getX();
                clcikPointf.y = event.getY();
                if (mapRectf.contains(clcikPointf.x, clcikPointf.y)) {
                    canDrag = true;
                }
                pointerCount = 1;
                mode = ONTOUCH_MODE_DRAG;
                break;

            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onTouchEvent:ACTION_UP ");
                canDrag = false;
                pointerCount = 0;
                mode = ONTOUCH_MODE_DRAG;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "onTouchEvent:ACTION_POINTER_UP ");
                pointerCount--;
                if (pointerCount > 1) {
                    mode = ONTOUCH_MODE_SCALE_OR_ROTATE;
                } else {
                    mode = ONTOUCH_MODE_DRAG;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "onTouchEvent:ACTION_POINTER_DOWN ");
                pointerCount++;
                mode = ONTOUCH_MODE_SCALE_OR_ROTATE;//多于一个手指则有可能是缩放或旋转
                oldRotation = rotation(event);
                lastDist = oldDist = spacing(event);
                break;

            case MotionEvent.ACTION_MOVE:
                //Log.i(TAG, "onTouchEvent:ACTION_MOVE ");
                if (mode == ONTOUCH_MODE_DRAG) {
                    if (canDrag) {
                        float dx = event.getX() - clcikPointf.x;
                        float dy = event.getY() - clcikPointf.y;
                        clcikPointf.x = event.getX();
                        clcikPointf.y = event.getY();
                        translateMap(dx, dy);
                        onGesture(dx, dy, 1, 0);

                    }
                } else {
                    canDrag = false;
                    float newDist = spacing(event);
                    float dist = Math.abs(newDist - lastDist);
                    lastDist = newDist;
                    Log.i(TAG, "两手指变化的距离dist：" + dist);
                    if (dist < 10) {//两手指变化的距离少于次数则认为是在旋转
                        float newRotation = rotation(event);
                        float rotate = newRotation - oldRotation;
                        oldRotation = newRotation;
                        Log.i(TAG, "旋转角度：" + rotate);
                        rotate(rotate);
                        onGesture(0, 0, 1, rotate);

                    } else {//否则则是缩放
                        float scale = newDist / oldDist;
                        Log.i(TAG, "缩放比例：" + scale);
                        if (scale < 1) {//缩小
                            zoom(1.0f - scaleSsensitivity / 1000.0f);
                        } else {//放大
                            zoom(1.0f + scaleSsensitivity / 1000.0f);
                        }
                        onGesture(0, 0, scale, 0);
                    }
                }
                break;
        }
        return true;
    }

    private void onGesture(float dx, float dy, float scale, float rotate) {
        if (onGestureListener != null) {
            onGestureListener.onGesture(GESTURE_DRAG, dx, dy, scale, rotate);
        }
    }

    /**
     * 取旋转角度
     *
     * @param event
     * @return
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 触碰两点间距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        super.surfaceChanged(surfaceHolder, i, i1, i2);
        Log.i(TAG, "surfaceChanged=" + i1 + "," + i2);
        if (mapBitmap != null) {
            initMapBitmap();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
    }


    @Override
    public void doDraw(Canvas c) {
        if (mapBitmap != null) {
            c.drawBitmap(mapBitmap, matrix, null);
        }

        if (isDrawAddPoints) {
            drawAddPoints(c);
        }

        drawOther(c);

        if (isDrawCenterIcon) {
            drawCenterIcon(c);
        }

        if (isShowReference) {
            drawReference(c);
        }

    }

    /**
     * 绘制添加的点
     *
     * @param c
     */
    private void drawAddPoints(Canvas c) {
        paint.setColor(pointColor);
        paint.setStyle(Paint.Style.FILL);
        for (PointF p : getPointsList()) {
            PointF pf = convertToNeedPoint(p, false);
            c.drawCircle(pf.x, pf.y, pointSize, paint);
        }
    }

    /**
     * 绘制中心点图标
     *
     * @param c
     */
    private void drawCenterIcon(Canvas c) {
        //画中心点图标
        if (centerBitmap != null) {
            if (centerBitmapPosition == CENTER_BITMAP_POSITION_CENTER) {
                c.drawBitmap(centerBitmap, (screenWidth - centerBitmap.getWidth()) / 2.0f, (screenHeight - centerBitmap.getHeight()) / 2.0f, null);
            } else {
                c.drawBitmap(centerBitmap, (screenWidth - centerBitmap.getWidth()) / 2.0f, screenCenterY - centerBitmap.getHeight(), null);
            }
        } else {
            paint.setColor(centerColor);
            paint.setStyle(Paint.Style.FILL);
            c.drawCircle(screenCenterX, screenCenterY, pointSize + 2, paint);
        }
    }

    /**
     * 用户自定义绘制
     *
     * @param c
     */
    public void drawOther(Canvas c) {
    }


    /**
     * 绘制参考系，便于观察和计算
     *
     * @param c
     */
    private void drawReference(Canvas c) {

        paint.setColor(Color.RED);//红色坐标系
        //画原点
        c.drawText("(0,0)", 0, 20, paint);

        //画图片中心点及坐标
        c.drawCircle(mapCenterX, mapCenterY, pointSize, paint);
        c.drawText("(" + mapCenterX + "," + mapCenterY + ")", mapCenterX, mapCenterY, paint);

        //画旋转角度
        c.drawText("rotate=" + mapRotate, 100, 20, paint);

        //画缩放比例
        c.drawText("scale=" + mapScale, 300, 20, paint);

        //画图片边框
        paint.setStyle(Paint.Style.STROKE);
        Path path = new Path();
        PointF dLeftTop = mapRectf.getLeftTopPoint();
        PointF dLeftDown = mapRectf.getLeftDownPoint();
        PointF dRightTop = mapRectf.getRightTopPoint();
        PointF dRightDown = mapRectf.getRightDownPoint();
        path.moveTo(dLeftTop.x, dLeftTop.y);
        path.lineTo(dLeftDown.x, dLeftDown.y);
        path.lineTo(dRightDown.x, dRightDown.y);
        path.lineTo(dRightTop.x, dRightTop.y);
        path.close();
        c.drawPath(path, paint);
        //画矩形左上角坐标
        c.drawText("(" + dLeftTop.x + "," + dLeftTop.y + ")", dLeftTop.x, dLeftTop.y, paint);


        //画点
        for (PointF p : points) {
            PointF pf = convertToNeedPoint(p, false);
            c.drawText("(" + pf.x + "," + pf.y + ")", pf.x, pf.y, paint);
        }
        //画屏幕中心点坐标
        c.drawText("(" + screenCenterX + "," + screenCenterY + ")", screenCenterX, screenCenterY, paint);

        drawMapReference(c, dLeftTop, dLeftDown, dRightTop);


    }

    /**
     * 绘制地图参考系
     *
     * @param c
     * @param dLeftTop
     * @param dLeftDown
     * @param dRightTop
     */
    private void drawMapReference(Canvas c, PointF dLeftTop, PointF dLeftDown, PointF dRightTop) {
        if (mapBitmap == null) {
            return;
        }
        paint.setColor(Color.BLUE);//蓝色坐标系

        //画原点
        if (mapCoordinate == COORDINATE_MAP_LEFT_TOP) {
            c.drawText("(0,0)", dLeftTop.x, dLeftTop.y + 20, paint);
        } else {
            c.drawText("(0,0)", dLeftDown.x, dLeftDown.y, paint);
        }


        //画图片中心点坐标
        PointF oCenter = new PointF(mapWidth / 2.0f, mapHeight / 2.0f);//相对中心点
        c.drawText("(" + oCenter.x + "," + oCenter.y + ")", mapCenterX, mapCenterY + 20, paint);
        paint.setColor(Color.GREEN);
        c.drawText("(" + oCenter.x / mapScale + "," + oCenter.y / mapScale + ")", mapCenterX, mapCenterY + 40, paint);

        for (PointF p : points) {

            PointF pf = convertToNeedPoint(p, false);
            PointF rp = convertToNeedPoint(p, true);
            paint.setColor(Color.BLUE);
            c.drawText("(" + rp.x * mapScale + "," + rp.y * mapScale + ")", pf.x, pf.y + 20, paint);
            paint.setColor(Color.GREEN);
            c.drawText("(" + rp.x + "," + rp.y + ")", pf.x, pf.y + 40, paint);
        }
        paint.setColor(Color.BLUE);
        c.drawText("W=" + mapRectf.getWidth(), dRightTop.x, dRightTop.y, paint);
        c.drawText("H=" + mapRectf.getHeight(), dLeftDown.x, dLeftDown.y + 20, paint);

        paint.setColor(Color.GREEN);
        c.drawText("srcW=" + srcMapWidth, dRightTop.x, dRightTop.y + 20, paint);
        c.drawText("srcH=" + srcMapHeight, dLeftDown.x, dLeftDown.y + 40, paint);
    }




}
