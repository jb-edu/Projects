package com.example.modernartui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jb-edu on 15-04-04.
 *
 * A simple custom view that displays colored rectangles that can change horizontal position
 * and color based on user input to the seek bar (aka progress bar).
 *
 * NOTE: For the best visual results, the class implementing the seek bar providing progress to
 * this class must use this class's getCalculatedSeekBarMax() method to set the seek bar max value
 * to an appropriately calculated scaled value to ensure smoothness of position transition and
 * proper alignment to the edges of display. This is facilitated by the measurements made in this
 * method's onMeasure method, which means that getCalculatedSeekBarMax() must be called after the
 * measuring has been completed.
 */
public class SeekBarRectView extends View {

    private final int RECT_PADDING = 5;
    private ShapeDrawable[] mRectangles = null;
    private int[] mColors =
            new int[]{
                Color.GREEN,
                Color.BLUE,
                Color.MAGENTA,
                Color.CYAN,
                Color.YELLOW,
                Color.RED,
                Color.WHITE,
                Color.GRAY};
    private static final float COLOR_RANGE = 255f;
    private boolean mPutOnLeftSide = true;
    private int mRectWidth = 0;
    private int mRectHeight = 0;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mSeekBarMax = 0;

    public SeekBarRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw (Canvas canvas){
        super.draw(canvas);

        if (mRectangles == null)
            initRectangles();

        for (int i=0; i < mColors.length; i++) {
            mRectangles[i].draw(canvas);
        }
    }

    /**
     * jb-edu: initialize the rectangles shapes, positions and colors.
     */
    private void initRectangles() {

        mRectangles = new ShapeDrawable[mColors.length];

        int rightEdge = mViewWidth - mRectWidth;
        int leftEdge = 0;
        int topEdge;
        int bottomEdge;

        // Used for determining which side of the screen to draw the rectangles on.
        mPutOnLeftSide = true;

        // Create a new rectangle for each of the colors provided in the mColors[],
        // defining their size, position and color details. Alternate between putting
        // each rectangle on the left and right side of the screen.
        for (int i=0; i < mColors.length; i++) {

            ShapeDrawable rectangle = new ShapeDrawable(new RectShape());

            if (i == 0)
                topEdge = (i * mRectHeight);
            else
                topEdge = (i * mRectHeight) + (i * RECT_PADDING);

            bottomEdge = topEdge + mRectHeight;

            if (mPutOnLeftSide) {
                rectangle.setBounds(
                        leftEdge, topEdge,
                        mRectWidth, bottomEdge);
                mPutOnLeftSide = false;
            }
            else {
                rectangle.setBounds(
                        rightEdge, topEdge,
                        rightEdge + mRectWidth,
                        bottomEdge);
                mPutOnLeftSide = true;
            }

            rectangle.getPaint().setColor(mColors[i]);
            rectangle.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
            rectangle.getPaint().setStrokeWidth(5);
            rectangle.getPaint().setAntiAlias(true);
            rectangle.getPaint().setStrokeJoin(Paint.Join.ROUND);
            rectangle.getPaint().setStrokeCap(Paint.Cap.ROUND);

            mRectangles[i] = rectangle;
        }
    }

    /**
     * jb-edu: Update each rectangle's position and color based on user input
     * and redraw the custom view.
     */
    public void updateRectangles(int progress, int progressChange) {

        mPutOnLeftSide = true;

        for (int i = 0; i < mColors.length; i++) {
            updatePosition(mRectangles[i], progressChange);
            updateColor(mRectangles[i], i, progress);
        }

        // Ensure that the display is refreshed.
        invalidate();
    }

    /**
     * jb-edu: update the positions of the rectangles based on user input.
     */
    private void updatePosition(ShapeDrawable rectangle, int progressChange) {

        if (mPutOnLeftSide) {
            rectangle.setBounds(
                    rectangle.getBounds().left + (progressChange),
                    rectangle.getBounds().top,
                    rectangle.getBounds().right + (progressChange),
                    rectangle.getBounds().bottom);
            mPutOnLeftSide = false;
        } else {
            rectangle.setBounds(
                    rectangle.getBounds().left - (progressChange),
                    rectangle.getBounds().top,
                    rectangle.getBounds().right - (progressChange),
                    rectangle.getBounds().bottom);
            mPutOnLeftSide = true;
        }
    }

    /**
     * jb-edu: update the colors of the rectangles based on user input.
     */
    private void updateColor(ShapeDrawable rectangle, int colorID, int progress) {

        // Only change the colors if they are not white
        // or gray.
        if (mColors[colorID] != Color.WHITE &&
                mColors[colorID] != Color.GRAY) {

            int redComp = Color.red(mColors[colorID]);
            int greenComp = Color.green(mColors[colorID]);
            int blueComp = Color.blue(mColors[colorID]);

            int colorChgFromProgress = Math.round(progress * COLOR_RANGE / mSeekBarMax);

            if (redComp == Color.red(Color.RED))
                redComp = Color.red(Color.RED) - colorChgFromProgress;
            else
                redComp = colorChgFromProgress;

            if (greenComp == Color.green(Color.GREEN))
                greenComp = Color.green(Color.GREEN) - (colorChgFromProgress / 2);
            else
                greenComp = colorChgFromProgress / 2;

            if (blueComp == Color.blue(Color.BLUE))
                blueComp = Color.blue(Color.BLUE) - (colorChgFromProgress / 3);
            else
                blueComp = colorChgFromProgress / 3;

            rectangle.getPaint().setColor(Color.rgb(redComp, greenComp, blueComp));
        }
    }

    /*
     * jb-edu: Calculate size of rectangles to display based on available screen space.
     * Ensure that space is provided for the seek bar.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        mRectWidth = mViewWidth / 2;
        mRectHeight = (mViewHeight - (mColors.length * RECT_PADDING)) / (mColors.length);

        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    /*
     * jb-edu: the calling class must use this value for the seek bar max in order for the
     * positions to update properly based on the progress and progress change values provided
     * to this class.
     */
    public int getCalculatedSeekBarMax() {
        if (mSeekBarMax == 0)
            mSeekBarMax = mViewWidth - mRectWidth;
        return mSeekBarMax;
    }
}
