package com.junmeng.gsv;

import android.graphics.PointF;

/**
 * Created by HWJ on 2016/12/13.
 */

public class CalculateUtil {

    /**
     * 求a点绕o点旋转rotate度后的点的坐标
     *
     * @param a      原始点
     * @param o      圆点，即围绕的中心点
     * @param rotate 旋转度数，正为顺时针，负为逆时针
     * @return 旋转后的点的坐标
     */
    public static PointF getPointAfterRotate(PointF a, PointF o, float rotate) {
        PointF b = new PointF();
        if (a == null || o == null) {
            return b;
        }
        // double angle = rotate * Math.PI / 180;
        double angle = Math.toRadians(rotate);//将角度转为弧度
        b.x = (float) ((a.x - o.x) * Math.cos(angle) - (a.y - o.y) * Math.sin(angle) + o.x);
        b.y = (float) ((a.x - o.x) * Math.sin(angle) + (a.y - o.y) * Math.cos(angle) + o.y);
        return b;
    }
}
