package com.hwj.gsv;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;

import com.hwj.gsv.databinding.ActivityMainBinding;
import com.junmeng.gsv.GestureSurfaceView;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.svMap.setCenterBitmap(getBitmap(this, R.drawable.ic_center_24dp));
        binding.svMap.setBackgroundColor(Color.WHITE);
        binding.svMap.setMapCoordinate(GestureSurfaceView.COORDINATE_MAP_LEFT_DOWN);
        binding.svMap.setMapBitmap(getBitmap(this, R.mipmap.ic_launcher));

        binding.scReference.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                binding.svMap.showReference(b);
            }
        });

        binding.scCoordinate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.svMap.setMapCoordinate(GestureSurfaceView.COORDINATE_MAP_LEFT_DOWN);
                } else {
                    binding.svMap.setMapCoordinate(GestureSurfaceView.COORDINATE_MAP_LEFT_TOP);
                }

            }
        });
        binding.scLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    binding.svMap.isPointToCenter(b);

            }
        });

    }

    /**
     * 将vector资源转为Bitmap
     *
     * @param context
     * @param vectorDrawableId
     * @return
     */
    public static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    public void onClickAddPoint(View view) {
        binding.svMap.addNewPoint();
    }

    public void onClickCenter(View view) {
        binding.svMap.mapMoveToCenter();
    }

    public void onClickFitCenter(View view) {
        binding.svMap.setMapBestFit();
    }

    public void onClickCenterKeep(View view) {
        binding.svMap.mapMoveToCenterKeepRotate();
    }


}
