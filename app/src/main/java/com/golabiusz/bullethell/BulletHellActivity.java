package com.golabiusz.bullethell;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class BulletHellActivity extends Activity
{
    private BulletHellGame bulletHellGame;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        bulletHellGame = new BulletHellGame(this, size.x, size.y);
        setContentView(bulletHellGame);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        bulletHellGame.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        bulletHellGame.pause();
    }
}
