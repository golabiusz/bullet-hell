package com.golabiusz.bullethell;

import android.graphics.RectF;

public class Bullet
{
    private RectF mRect;

    private float mXVelocity;
    private float mYVelocity;

    private float mWidth;
    private float mHeight;

    Bullet(int screenWidth)
    {
        mRect = new RectF();

        mWidth = screenWidth / 100;
        mHeight = screenWidth / 100;

        mYVelocity = screenWidth / 5;
        mXVelocity = screenWidth / 5;
    }

    public RectF getRect()
    {
        return mRect;
    }

    public void update(long fps)
    {
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);

        mRect.right = mRect.left + mWidth;
        mRect.bottom = mRect.top - mHeight;
    }

    public void reverseYVelocity()
    {
        mYVelocity = -mYVelocity;
    }

    public void reverseXVelocity()
    {
        mXVelocity = -mXVelocity;
    }

    void spawn(int positionX, int positionY, int vX, int vY)
    {
        mRect.left = positionX;
        mRect.top = positionY;
        mRect.right = positionX  + mWidth;
        mRect.bottom = positionY + mHeight;

        mXVelocity = mXVelocity * vX;
        mYVelocity = mYVelocity * vY;
    }
}
