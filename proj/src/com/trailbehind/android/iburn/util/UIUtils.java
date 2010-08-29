package com.trailbehind.android.iburn.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.trailbehind.android.iburn.R;

/**
 * The Class UIUtils.
 */
public class UIUtils {

    /**
     * Show default toast.
     * 
     * @param context
     *            the context
     * @param id
     *            the id
     * @param top
     *            the top
     */
    static public void showDefaultToast(Context context, int id, boolean top) {
        showDefaultToast(context, id, false, top);
    }

    /**
     * Show default toast.
     * 
     * @param context
     *            the context
     * @param msg
     *            the msg
     * @param top
     *            the top
     */
    static public void showDefaultToast(Context context, String msg, boolean top) {
        showDefaultToast(context, msg, false, top);
    }

    /**
     * Show default toast.
     * 
     * @param context
     *            the context
     * @param id
     *            the id
     * @param longToast
     *            the long toast
     * @param top
     *            the top
     */
    static public void showDefaultToast(Context context, int id, boolean longToast, boolean top) {
        final Toast toast = Toast.makeText(context, id, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        if (top) {
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 75);
        }
        toast.show();
    }

    /**
     * Show default toast.
     * 
     * @param context
     *            the context
     * @param msg
     *            the msg
     * @param longToast
     *            the long toast
     * @param top
     *            the top
     */
    static public void showDefaultToast(Context context, String msg, boolean longToast, boolean top) {
        final Toast toast = Toast.makeText(context, msg, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        if (top) {
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 75);
        }
        toast.show();
    }

    /**
     * Show formatted default toast.
     * 
     * @param context
     *            the context
     * @param id
     *            the id
     * @param args
     *            the args
     * @param top
     *            the top
     */
    static public void showFormattedDefaultToast(Context context, int id, boolean top, Object... args) {
        final Toast toast = Toast.makeText(context, String.format(context.getText(id).toString(), args),
                Toast.LENGTH_LONG);
        if (top) {
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 75);
        }
        toast.show();
    }

    /**
     * Show panel.
     * 
     * @param context
     *            the context
     * @param panel
     *            the panel
     * @param slideUp
     *            the slide up
     */
    static public void showPanel(Context context, View panel, boolean slideUp) {
        if (panel.getVisibility() != View.VISIBLE) {
            panel.startAnimation(AnimationUtils
                    .loadAnimation(context, slideUp ? R.anim.slide_in : R.anim.slide_out_top));
            panel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide panel.
     * 
     * @param context
     *            the context
     * @param panel
     *            the panel
     * @param slideDown
     *            the slide down
     */
    static public void hidePanel(Context context, View panel, boolean slideDown) {
        if (panel.getVisibility() != View.GONE) {
            panel.startAnimation(AnimationUtils.loadAnimation(context, slideDown ? R.anim.slide_out
                    : R.anim.slide_in_top));
            panel.setVisibility(View.GONE);
        }
    }

    /**
     * Show panel h.
     * 
     * @param context
     *            the context
     * @param panel
     *            the panel
     * @param slideLeft
     *            the slide left
     */
    static public void showPanelH(Context context, View panel, boolean slideLeft) {
        if (panel.getVisibility() != View.VISIBLE) {
            panel.startAnimation(AnimationUtils.loadAnimation(context, slideLeft ? R.anim.slide_in_left
                    : R.anim.slide_in_right));
            panel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide panel h.
     * 
     * @param context
     *            the context
     * @param panel
     *            the panel
     * @param slideRight
     *            the slide right
     */
    static public void hidePanelH(Context context, View panel, boolean slideRight) {
        if (panel.getVisibility() != View.GONE) {
            panel.startAnimation(AnimationUtils.loadAnimation(context, slideRight ? R.anim.slide_out_left
                    : R.anim.slide_out_right));
            panel.setVisibility(View.GONE);
        }
    }

    /**
     * Fade view.
     * 
     * @param view
     *            the view
     * @param visibility
     *            the visibility
     * @param startAlpha
     *            the start alpha
     * @param endAlpha
     *            the end alpha
     */
    static public void fadeView(View view, int visibility, float startAlpha, float endAlpha) {
        final AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
        anim.setDuration(500);
        view.startAnimation(anim);
        view.setVisibility(visibility);
    }
}
