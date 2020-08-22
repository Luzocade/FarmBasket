package com.smartagriculture;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jackandphantom.blurimage.BlurImage;

public class UiEnhancement {

    public void show(boolean animate, long startDelay, long duration, View... views) {
        for (View v : views) {
            if (v.getAlpha() != 0)
                continue;
            if (animate)
                v.animate().alpha(1f).setStartDelay(startDelay).setDuration(duration);
            else
                v.setAlpha(1f);
        }
    }

    public void hide(boolean animate, long startDelay, long duration, View... views) {
        for (View v : views) {
            if (v.getAlpha() != 1)
                continue;
            if (animate)
                v.animate().alpha(0f).setStartDelay(startDelay).setDuration(duration);
            else
                v.setAlpha(0f);
        }
    }

    public void blur(Context context, int res, int intensity, boolean async, View imageView) {
        BlurImage.with(context).load(res).intensity(intensity).Async(async).into((ImageView) imageView);
    }

    public void invisible(View... views) {
        for (View v : views)
            v.setVisibility(View.INVISIBLE);
    }

    public void gone(View... views) {
        for (View v : views)
            v.setVisibility(View.GONE);
    }

    public void visible(View... views) {
        for (View v : views)
            v.setVisibility(View.VISIBLE);
    }

    public void fadeSwitch(final View fromView, final View toView, final long duration) {
        hide(true, 0, duration, fromView);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                invisible(fromView);
                hide(false, 0, 0, toView);
                visible(toView);
                show(true, 0, duration, toView);
            }
        }, duration);
    }

    public void setDrawable(char position, Drawable drawable, Button... buttons) {
        for (Button b : buttons) {
            switch (position) {
                case 's': //start
                    b.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
                    break;
                case 'e': //end
                    b.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
                    break;
                case 't': //top
                    b.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                    break;
                case 'b': //bottom
                    b.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, drawable);
                    break;
            }
        }
    }

    public void removeAllDrawables(Button... buttons){
        for(Button b:buttons){
            b.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
        }
    }

    public void setBgImage(Drawable drawable, View... views) {
        for (View v : views) {
            v.setBackground(drawable);
        }
    }

    public void removeBgImage(View... views) {
        for (View v : views) {
            v.setBackground(null);
        }
    }

}
