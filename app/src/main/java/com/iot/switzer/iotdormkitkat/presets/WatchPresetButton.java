package com.iot.switzer.iotdormkitkat.presets;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;

/**
 * Created by Lucas Switzer on 7/5/2016.
 */
public class WatchPresetButton extends Button {
    private String name;
    private static final int MAX_COLOR = 16777215;
    protected  static final float MIN_SATURATION = .7f;

    private boolean enabled;
    private int color;

    public WatchPresetButton(Context context, String name) {
        super(context);
        this.name = name;
        this.setText(name);
        color = colorFromName(name);
        //this.setBackgroundColor(color);
        this.setBackgroundColor(Color.RED);
    }
    public boolean isPresetEnabled()
    {
        return enabled;
    }

    public String getName()
    {
        return name;
    }

    public void toggle()
    {
        if(enabled)
            disable();
        else
            enable();
    }

    public void disable()
    {
        this.setBackgroundColor(color);
        enabled = false;
    }
    public void enable()
    {
        this.setBackgroundColor(Color.GREEN);
        enabled = true;
    }

    public static int colorFromName(String name)
    {
        float[] hsv = new float[3];
        Color.colorToHSV(-(name.hashCode() % MAX_COLOR),hsv);

        hsv[1] = hsv[1] < MIN_SATURATION ? MIN_SATURATION : hsv[1];

        return Color.HSVToColor(hsv);
    }
}
