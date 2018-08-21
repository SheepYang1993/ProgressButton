package me.sheepyang.progressbuttonlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * TODO 备注
 *
 * @author SheepYang
 * @since 2018/8/16 11:21
 */
public class ProgressButton extends AppCompatTextView {
    private Paint mPaint = new Paint();
    private Paint mPointPaint = new Paint();
    private Path mPath;
    private int mWidth;
    private int mHeight;

    private int mRadius;
    private RectF mLeftArcRectF;
    private RectF mRightArcRectF;
    private int mRectFWidth;
    private PathMeasure mPathMeasure;
    private ValueAnimator mValueAnimator;
    private Path mDst = new Path();
    private float mLength;
    private Point mFixBugPoint;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mRectFWidth = (int) (mHeight - mPaint.getStrokeWidth());
        mRadius = mRectFWidth / 2;

        initPath();
        startPathAnim(1000);
    }

    private void init() {
        initPaint();
    }


    /**
     * 开启路径动画
     */
    public void startPathAnim(long duration) {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
            mValueAnimator.setDuration(duration);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            // 减速插值器
//            mValueAnimator.setInterpolator(new DecelerateInterpolator());
//            mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//            mValueAnimator.setInterpolator(new AccelerateInterpolator());
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    Log.i("Animation", "onAnimationCancel");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Log.i("Animation", "onAnimationEnd");
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                    Log.i("Animation", "onAnimationRepeat");
                    changePaintColor(mPaint);
                    changePaintColor(mPointPaint);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    Log.i("Animation", "onAnimationStart");
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    super.onAnimationPause(animation);
                    Log.i("Animation", "onAnimationPause");
                }

                @Override
                public void onAnimationResume(Animator animation) {
                    super.onAnimationResume(animation);
                    Log.i("Animation", "onAnimationResume");
                }
            });
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mLength = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        } else {
            mValueAnimator.cancel();
        }
        mValueAnimator.start();
    }

    private void changePaintColor(Paint paint) {
        if (paint.getColor() == Color.WHITE) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //解决硬件加速的BUG
        mDst.reset();
        mDst.moveTo(mFixBugPoint.x, mFixBugPoint.y);
        canvas.drawPath(mPath, mPaint);
        mPathMeasure.getSegment(0, mLength, mDst, true);
        canvas.drawPath(mDst, mPointPaint);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //设置画笔颜色
        mPaint.setColor(getCurrentTextColor());
        //设置画笔模式为填充
        mPaint.setStyle(Paint.Style.STROKE);
        //设置画笔宽度为10px
        mPaint.setStrokeWidth(10f);

        mPointPaint.setAntiAlias(true);
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
        //设置画笔颜色
        mPointPaint.setColor(Color.BLACK);
        //设置画笔模式为填充
        mPointPaint.setStyle(Paint.Style.STROKE);
        //设置画笔宽度为10px
        mPointPaint.setStrokeWidth(10f);
    }

    private void initPath() {
        mLeftArcRectF = new RectF(mPaint.getStrokeWidth() / 2, mPaint.getStrokeWidth() / 2, mHeight - mPaint.getStrokeWidth() / 2, mHeight - mPaint.getStrokeWidth() / 2);
        mRightArcRectF = new RectF(mWidth - mHeight - mPaint.getStrokeWidth() / 2, mPaint.getStrokeWidth() / 2, mWidth - mPaint.getStrokeWidth() / 2, mHeight - mPaint.getStrokeWidth() / 2);
        mPath = new Path();

        mPath.moveTo(mRectFWidth / 2 + mPointPaint.getStrokeWidth() / 2, mPointPaint.getStrokeWidth() / 2);
        //绘制上边线条
        mPath.lineTo(mWidth - mHeight / 2 - mPaint.getStrokeWidth() / 2, mPointPaint.getStrokeWidth() / 2);

        mFixBugPoint = new Point((int) (mRectFWidth / 2 + mPointPaint.getStrokeWidth() / 2), (int) (mPointPaint.getStrokeWidth() / 2));

        //绘制右侧半圆
        mPath.arcTo(mRightArcRectF, -90, 180, false);
        //绘制下边线条
        mPath.lineTo(mRectFWidth / 2 + mPointPaint.getStrokeWidth() / 2, mHeight - mPaint.getStrokeWidth() / 2);
        //绘制左边半圆
        mPath.arcTo(mLeftArcRectF, 90, 180, false);
        mPathMeasure = new PathMeasure(mPath, true);
        mLength = (int) mPathMeasure.getLength();
    }
}
