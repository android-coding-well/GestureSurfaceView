package com.hwj.gsv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.junmeng.gsv.GestureSurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * 集成GestureSurfaceView，然后实现自己的业务逻辑
 * Created by HWJ on 2016/12/16.
 */

public class BusinessSurfaceView extends GestureSurfaceView {

    List<PointF> pointList = new ArrayList<>();

    boolean isPointToCenter=false;

    public BusinessSurfaceView(Context context) {
        this(context, null, 0);
    }

    public BusinessSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BusinessSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(0xff0fffff);
    }


    @Override
    public void drawOther(Canvas c) {
        for (PointF p : pointList) {
            PointF pp = convertToNeedPoint(p, false);
            c.drawCircle(pp.x, pp.y, 5, paint);
            if(isPointToCenter){
                c.drawLine(pp.x,pp.y,screenCenterX,screenCenterY,paint);
            }

        }



    }

    public void addNewPoint() {
        pointList.add(getCurrentPoint());
    }

    /**
     * 是否所有点指向中心点
     * @param bool
     */
    public void isPointToCenter(boolean bool){
        this.isPointToCenter=bool;
    }


}
