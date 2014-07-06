package org.abrantix.twitterdrawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fabrantes on 01/07/14.
 */
public class TwitterDrawable extends Drawable {

    private Rect mBounds;
    private Path mPath;
    private Paint mPathPaint;
    private long mLastStepTime = 0;
    private AnimState mAnimState = AnimState.IDLE;
    private float mWingCounter = 0;
    private float mWingTime = 119;
    private int mWingDirection = 1;

    private static final int TWITTER_BLUE = Color.argb(0xff, 0x40, 0x99, 0xff);

    private static Interpolator sOutInterpolator = new AccelerateDecelerateInterpolator();
    private static Interpolator sInInterpolator = new AccelerateDecelerateInterpolator();
    private static final int sWingStart = 6;
    private static final int sWingStop = 14;
    private static float sWingDx = 0;
    private static float sWingDy = 0;
    private static PointF sGetWingDeltaResponse = new PointF();
    private static Set<Integer> sWing = new HashSet<Integer>(30);
    static {
        sWing.add(6);
        sWing.add(7);
        sWing.add(8);
        sWing.add(9);
        sWing.add(10);
        sWing.add(11);
        sWing.add(12);
        sWing.add(13);
    }
    // static Set<Integer> sBill = new HashSet<Integer>(10);
    // static {
    //    sBill.add(0);
    //    sBill.add(1);
    //}

    private static enum AnimState {
        IDLE,
        FINISH_FLYING,
        FLYING
        // etc, etc, ...
    }

    public void setFlying (boolean flag) {
        if (flag) {
            mAnimState = AnimState.FLYING;
            invalidateSelf();
        } else {
            mAnimState = mAnimState == AnimState.FLYING ? AnimState.FINISH_FLYING : AnimState.IDLE;
        }
    }

    public boolean isFlying () {
        return mAnimState == AnimState.FLYING;
    }

    @Override
    public void draw(Canvas canvas) {
        float dt = mLastStepTime == 0 ? 1 : System.currentTimeMillis() - mLastStepTime;
        mLastStepTime = System.currentTimeMillis();

        if (mBounds == null || boundsAreDifferent(mBounds, getBounds())) {
            mBounds = getBounds();
            mPath = stepPath(mBounds, dt);
            mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPathPaint.setColor(TWITTER_BLUE);
        }

        if (mAnimState != AnimState.IDLE)
            mPath = stepPath(mBounds, dt);

        canvas.drawPath(mPath, mPathPaint);

        if (mAnimState != AnimState.IDLE)
            invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {  }

    @Override
    public void setColorFilter(ColorFilter cf) {  }

    @Override
    public int getOpacity() {
        return 0;
    }

    private Path stepPath(Rect bounds, float dt) {
        Path p = new Path();
        float scaleX = bounds.width() / sPathDimensions.x;
        float scaleY = bounds.height() / sPathDimensions.y;

        float scale = Math.min(scaleX, scaleY);

        float offsetX = 0.5f * (bounds.width() - (sPathDimensions.x * scale));
        float offsetY = 0.5f * (bounds.height() - (sPathDimensions.y * scale));
        float extraX;
        float extraY;

        PointF lastPoint = new PointF();
        PointF nextPoint = new PointF();
        PointF control1 = new PointF();
        PointF control2 = new PointF();
        lastPoint.x = scale * (sTranslation.x + sInitialPoint.x) + offsetX;
        lastPoint.y = scale * (sTranslation.y + sInitialPoint.y) + offsetY;
        p.moveTo(lastPoint.x, lastPoint.y);

        mWingCounter += dt;
        mWingCounter = Math.min(mWingTime, mWingCounter);
        float progress = mWingDirection > 0 ? mWingCounter / mWingTime : 1 - (mWingCounter /
                mWingTime);
        if (mWingCounter >= mWingTime) {
            mWingDirection = -mWingDirection;
            mWingCounter = 0;

            if (mWingDirection > 0 && mAnimState == AnimState.FINISH_FLYING) {
                mAnimState = AnimState.IDLE;
            }
        }
        if (mWingDirection > 0) progress = sInInterpolator.getInterpolation(progress);
        else progress = sOutInterpolator.getInterpolation(progress);

        for (int k = 0; k < sPathCurvePoints.size()/3; k++) {
            PointF pp = sPathCurvePoints.get(k*3 + 2);
            PointF c1 = sPathCurvePoints.get(k*3 + 0);
            PointF c2 = sPathCurvePoints.get(k*3 + 1);

            extraX = 0;
            extraY = 0;

            // if (sBill.contains(k)) {
            //      extraY = -bounds.height() * 0.05f;
            // }

            PointF wingDelta = getWingDelta(k, progress, bounds);
            extraY += wingDelta.y;
            extraX += wingDelta.x;

            control1.x = lastPoint.x + scale * c1.x + extraX;
            control1.y = lastPoint.y + scale * c1.y + extraY;

            control2.x = lastPoint.x + scale * c2.x + extraX;
            control2.y = lastPoint.y + scale * c2.y + extraY;

            nextPoint.x = lastPoint.x + scale * pp.x + extraX;
            nextPoint.y = lastPoint.y + scale * pp.y + extraY;

            p.cubicTo(control1.x, control1.y, control2.x, control2.y, nextPoint.x, nextPoint.y);

            lastPoint.x = nextPoint.x - extraX;
            lastPoint.y = nextPoint.y - extraY;
        }
        p.close();
        return p;
    }

    private static boolean boundsAreDifferent(Rect b, Rect b2) {
        return b.width() != b2.width() || b.height() != b2.height();
    }

    private static PointF getWingDelta(int k, float fraction, Rect bounds) {
        if (k < sWingStart || k > sWingStop) {
            sWingDx = 0;
        } else {
            sWingDx = ((sWingStop - sWingStart +1)-(k- sWingStart +1)) / (float) (sWingStop -
                    sWingStart +1) * 0.1f * fraction * bounds.height();
        }
        if (k < sWingStart || k > sWingStop) {
            sWingDy = 0;
        } else {
            sWingDy = ((sWingStop - sWingStart)-(k- sWingStart)) / (float) (sWingStop - sWingStart)
                    * 0.2f * fraction * bounds.height();
        }
        sGetWingDeltaResponse.x = sWingDx;
        sGetWingDeltaResponse.y = sWingDy;
        return sGetWingDeltaResponse;
    }

    /**
     * SVG definition
     */
    private static PointF sPathDimensions = new PointF(171.5054f, 139.37839f);
    // t -282.32053,-396.30734
    private static PointF sTranslation = new PointF(-282.32053f,-396.30734f);
    // m 453.82593,412.80619
    private static PointF sInitialPoint = new PointF(453.82593f,412.80619f);
    // c
    static ArrayList<PointF> sPathCurvePoints = new ArrayList<PointF>();
    static {
        sPathCurvePoints.clear();
        sPathCurvePoints.add(new PointF(-6.3097f,2.79897f));
        sPathCurvePoints.add(new PointF(-13.09189f,4.68982f));
        sPathCurvePoints.add(new PointF(-20.20852f,5.54049f));
        sPathCurvePoints.add(new PointF(7.26413f,-4.35454f));
        sPathCurvePoints.add(new PointF(12.84406f,-11.24992f));
        sPathCurvePoints.add(new PointF(15.47067f,-19.46675f));
        sPathCurvePoints.add(new PointF(-6.79934f,4.03295f));
        sPathCurvePoints.add(new PointF(-14.3293f,6.96055f));
        sPathCurvePoints.add(new PointF(-22.34461f,8.53841f));
        sPathCurvePoints.add(new PointF(-6.41775f,-6.83879f));
        sPathCurvePoints.add(new PointF(-15.56243f,-11.111f));
        sPathCurvePoints.add(new PointF(-25.68298f,-11.111f));
        sPathCurvePoints.add(new PointF(-19.43159f,0f));
        sPathCurvePoints.add(new PointF(-35.18696f,15.75365f));
        sPathCurvePoints.add(new PointF(-35.18696f,35.18525f));
        sPathCurvePoints.add(new PointF(0,2.75781f));
        sPathCurvePoints.add(new PointF(0.31128f,5.44359f));
        sPathCurvePoints.add(new PointF(0.91155f,8.01875f));
        sPathCurvePoints.add(new PointF(-29.24344f,-1.46723f));
        sPathCurvePoints.add(new PointF(-55.16995f,-15.47582f));
        sPathCurvePoints.add(new PointF(-72.52461f,-36.76396f));
        sPathCurvePoints.add(new PointF(-3.02879f,5.19662f));
        sPathCurvePoints.add(new PointF(-4.76443f,11.24048f));
        sPathCurvePoints.add(new PointF(-4.76443f,17.6891f));
        sPathCurvePoints.add(new PointF(0f,12.20777f));
        sPathCurvePoints.add(new PointF(6.21194f,22.97747f));
        sPathCurvePoints.add(new PointF(15.65332f,29.28716f));
        sPathCurvePoints.add(new PointF(-5.76773f,-0.18265f));
        sPathCurvePoints.add(new PointF(-11.19331f,-1.76565f));
        sPathCurvePoints.add(new PointF(-15.93716f,-4.40083f));
        sPathCurvePoints.add(new PointF(-0.004f,0.14663f));
        sPathCurvePoints.add(new PointF(-0.004f,0.29412f));
        sPathCurvePoints.add(new PointF(-0.004f,0.44248f));
        sPathCurvePoints.add(new PointF(0f,17.04767f));
        sPathCurvePoints.add(new PointF(12.12889f,31.26806f));
        sPathCurvePoints.add(new PointF(28.22555f,34.50266f));
        sPathCurvePoints.add(new PointF(-2.95247f,0.80436f));
        sPathCurvePoints.add(new PointF(-6.06101f,1.23398f));
        sPathCurvePoints.add(new PointF(-9.26989f,1.23398f));
        sPathCurvePoints.add(new PointF(-2.2673f,0f));
        sPathCurvePoints.add(new PointF(-4.47114f,-0.22124f));
        sPathCurvePoints.add(new PointF(-6.62011f,-0.63114f));
        sPathCurvePoints.add(new PointF(4.47801f,13.97857f));
        sPathCurvePoints.add(new PointF(17.47214f,24.15143f));
        sPathCurvePoints.add(new PointF(32.86992f,24.43441f));
        sPathCurvePoints.add(new PointF(-12.04227f,9.43796f));
        sPathCurvePoints.add(new PointF(-27.21366f,15.06335f));
        sPathCurvePoints.add(new PointF(-43.69965f,15.06335f));
        sPathCurvePoints.add(new PointF(-2.84014f,0f));
        sPathCurvePoints.add(new PointF(-5.64082f,-0.16722f));
        sPathCurvePoints.add(new PointF(-8.39349f,-0.49223f));
        sPathCurvePoints.add(new PointF(15.57186f,9.98421f));
        sPathCurvePoints.add(new PointF(34.06703f,15.8094f));
        sPathCurvePoints.add(new PointF(53.93768f,15.8094f));
        sPathCurvePoints.add(new PointF(64.72024f,0f));
        sPathCurvePoints.add(new PointF(100.11301f,-53.61524f));
        sPathCurvePoints.add(new PointF(100.11301f,-100.11387f));
        sPathCurvePoints.add(new PointF(0f,-1.52554f));
        sPathCurvePoints.add(new PointF(-0.0343f,-3.04251f));
        sPathCurvePoints.add(new PointF(-0.10204f,-4.55261f));
        sPathCurvePoints.add(new PointF(6.87394f,-4.95995f));
        sPathCurvePoints.add(new PointF(12.83891f,-11.15646f));
        sPathCurvePoints.add(new PointF(17.55618f,-18.21305f));
    }
}

