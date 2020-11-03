package com.golabiusz.bullethell;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;

class BulletHellGame extends SurfaceView implements Runnable
{
    private final boolean DEBUGGING = false;
    private final int DEFAULT_SHIELD_HP = 10;
    private final int MILLIS_IN_SECOND = 1000;

    private Thread gameThread = null;
    private volatile boolean isPaused;
    private boolean isPlaying = false;

    private Canvas canvas;
    private Paint paint;

    private long fps;

    private int screenWidth;
    private int screenHeight;
    private int fontSize;
    private int fontMargin;

    private SoundPool sp;
    private int beepSoundID = -1;
    private int teleportSoundID = -1;

    private Bullet[] bullets = new Bullet[10000];
    private int numBullets = 0;

    private Bob bob;
    private int shieldHP = DEFAULT_SHIELD_HP;

    private long startGameTime;
    private long bestGameTime;
    private long totalGameTime;

    private Random randomX = new Random();
    private Random randomY = new Random();

    public BulletHellGame(Context context, int screenWidth, int screenHeight)
    {
        super(context);

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        fontSize = (int) (screenWidth * 0.05f);
        fontMargin = (int) (screenWidth * 0.02f);

        paint = new Paint();

        this.loadSounds(context);

        for (int i = 0; i < bullets.length; ++i) {
            bullets[i] = new Bullet(screenWidth);
        }

        bob = new Bob(context, screenWidth, screenHeight);

        startGame();
    }

    public void resume()
    {
        isPaused = false;

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause()
    {
        isPaused = true;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "stopping thread");
        }
    }

    @Override
    public void run()
    {
        while (!isPaused) {
            long frameStartTime = System.currentTimeMillis();

            if (isPlaying) {
                updateObjectsPosition();
                detectCollisions();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            if (timeThisFrame > 0) {
                fps = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!isPlaying) {
                    startGameTime = System.currentTimeMillis();
                    isPlaying = true;
                }

                if (bob.teleport(calculateNewX(motionEvent), calculateNewY(motionEvent))) {
                    sp.play(teleportSoundID, 1, 1, 0, 0, 1);
                }

                break;
            case MotionEvent.ACTION_UP:
                bob.setTeleportAvailable();
                spawnBullet();

                break;
        }

        return true;
    }

    private float calculateNewX(MotionEvent motionEvent)
    {
        if (motionEvent.getX() < screenWidth / 2) {
            return Math.max(motionEvent.getX(), bob.getRect().width() / 2);
        } else {
            return Math.min(motionEvent.getX(), screenWidth - bob.getRect().width() / 2);
        }
    }

    private float calculateNewY(MotionEvent motionEvent)
    {
        if (motionEvent.getY() < screenHeight / 2) {
            return Math.max(motionEvent.getY(), bob.getRect().height() / 2);
        } else {
            return Math.min(motionEvent.getY(), screenHeight - bob.getRect().height() / 2);
        }
    }

    private void loadSounds(Context context)
    {
        sp = new SoundPoolBuilder().build();

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("beep.ogg");
            beepSoundID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("teleport.ogg");
            teleportSoundID = sp.load(descriptor, 0);
        } catch (IOException e) {
            Log.e("error", "failed to load sound files");
        }
    }

    private void startGame()
    {
        shieldHP = DEFAULT_SHIELD_HP;
        numBullets = 0;

        if (totalGameTime > bestGameTime) {
            bestGameTime = totalGameTime;
        }
    }

    private void spawnBullet()
    {
        ++numBullets;

        int spawnX;
        int spawnY;
        int velocityX;
        int velocityY;

        if (bob.getRect().centerX() < screenWidth / 2) {
            spawnX = randomX.nextInt(screenWidth / 2) + screenWidth / 2;
            velocityX = 1;
        } else {
            spawnX = randomX.nextInt(screenWidth / 2);
            velocityX = -1;
        }

        if (bob.getRect().centerY() < screenHeight / 2) {
            spawnY = randomY.nextInt(screenHeight / 2) + screenHeight / 2;
            velocityY = 1;
        } else {
            spawnY = randomY.nextInt(screenHeight / 2);
            velocityY = -1;
        }

        bullets[numBullets - 1].spawn(spawnX, spawnY, velocityX, velocityY);
    }

    private void updateObjectsPosition()
    {
        for (int i = 0; i < numBullets; ++i) {
            bullets[i].update(fps);
        }
    }

    private void detectCollisions()
    {
        detectWallCollisions();
        detectBobCollisions();
    }

    private void detectWallCollisions()
    {
        for (int i = 0; i < numBullets; ++i) {
            if (bullets[i].getRect().bottom >= screenHeight) {
                bullets[i].reverseYVelocity();
            } else if (bullets[i].getRect().top <= 0) {
                bullets[i].reverseYVelocity();
            }
            if (bullets[i].getRect().left <= 0) {
                bullets[i].reverseXVelocity();
            } else if (bullets[i].getRect().right >= screenWidth) {
                bullets[i].reverseXVelocity();
            }
        }
    }

    private void detectBobCollisions()
    {
        for (int i = 0; i < numBullets; ++i) {
            if (RectF.intersects(bullets[i].getRect(), bob.getRect())) {
                sp.play(beepSoundID, 1, 1, 0, 0, 1);

                bullets[i].reverseXVelocity();
                bullets[i].reverseYVelocity();

                --shieldHP;

                if (0 == shieldHP) {
                    isPlaying = false;
                    totalGameTime = System.currentTimeMillis() - startGameTime;

                    startGame();
                }
            }

        }
    }

    private void draw()
    {
        if (getHolder().getSurface().isValid()) {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.argb(255, 243, 111, 36));
            paint.setColor(Color.argb(255, 255, 255, 255));

            drawBullets();
            drawBob();
            drawHUD();

            if (DEBUGGING) {
                printDebuggingText();
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawBob()
    {
        canvas.drawBitmap(bob.getBitmap(), bob.getRect().left, bob.getRect().top, paint);
    }

    private void drawBullets()
    {
        for (int i = 0; i < numBullets; ++i) {
            canvas.drawRect(bullets[i].getRect(), paint);
        }
    }

    private void drawHUD()
    {
        paint.setTextSize(fontSize);
        canvas.drawText(
            "Bullets: " + numBullets + "  Shield: " + shieldHP + "  Best Time: " + bestGameTime / MILLIS_IN_SECOND,
            fontMargin,
            fontSize,
            paint
        );

        if (isPlaying) {
            canvas.drawText(
                "Seconds Survived: " + ((System.currentTimeMillis() - startGameTime) / MILLIS_IN_SECOND),
                fontMargin,
                fontMargin * 30,
                paint
            );
        }
    }

    private void printDebuggingText()
    {
        int debugSize = 35;
        int debugStart = 150;

        paint.setTextSize(debugSize);

        canvas.drawText("FPS: " + fps, 10, debugStart + debugSize, paint);
        canvas.drawText("Bob left: " + bob.getRect().left, 10, debugStart + debugSize * 2, paint);
        canvas.drawText("Bob top: " + bob.getRect().top, 10, debugStart + debugSize * 3, paint);
        canvas.drawText("Bob right: " + bob.getRect().right, 10, debugStart + debugSize * 4, paint);
        canvas.drawText("Bob bottom: " + bob.getRect().bottom, 10, debugStart + debugSize * 5, paint);
        canvas.drawText("Bob centerX: " + bob.getRect().centerX(), 10, debugStart + debugSize * 6, paint);
        canvas.drawText("Bob centerY: " + bob.getRect().centerY(), 10, debugStart + debugSize * 7, paint);
    }
}
