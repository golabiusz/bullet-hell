package com.golabiusz.bullethell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Bob
{
    private RectF rect;
    private float height;
    private float width;
    private boolean isTeleporting = false;

    private Bitmap bitmap;

    public Bob(Context context, float screenWidth, float screenHeight)
    {
        height = screenHeight / 10;
        width = height / 2;

        float left = screenWidth / 2 - width / 2;
        float top = screenHeight / 2 - height / 2;
        rect = new RectF(left, top, left + width, top + height);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bob);
    }

    public void setTeleportAvailable()
    {
        isTeleporting = false;
    }

    public RectF getRect()
    {
        return rect;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public boolean teleport(float newX, float newY)
    {
        boolean success = false;

        if (!isTeleporting) {
            rect.left = newX - width / 2;
            rect.top = newY - height / 2;
            rect.bottom = rect.top + height;
            rect.right = rect.left + width;

            isTeleporting = true;

            success = true;
        }

        return success;
    }
}
