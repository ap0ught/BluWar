/*Copyright (C) 2013  NaikSoftware

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software*/
package ua.naiksoftware.bluwar;

//import filelog.Log;
import java.io.IOException;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ua.naiksoftware.bluwar.maps.Map;

/*
 * Main game class that extends GameCanvas for J2ME.
 * Handles game rendering, input, and the main game loop.
 */
public class Game extends GameCanvas implements Runnable {

    /*
     * FPS calculation and display variables.
     */
    private static final String tag = "Game";
    private static long FPS, FPS_Count, FPS_Start;

    /*
     * Размеры карты в пикселях.
     * Map dimensions in pixels.
     */
    private final int w, h;
    /*
     * Размеры экрана в пикселях.
     * Screen dimensions in pixels.
     */
    private final int scrW, scrH;
    /*
     * Центр экрана.
     * Screen center.
     */
    private int cX, cY;
    /*
     * Координаты фокусировки на карте.
     * Focus coordinates on the map.
     */
    private int mapX, mapY;
    /*
     * Обертка для удобства действий с картой.
     * Wrapper for convenient map operations.
     */
    private final MapHolder mapHolder;
    private byte moveType;
    private static final byte MOVE_CURSOR = 1;
    /* 
     * Изображения, которые все время будут в памяти.
     * Images that will always be kept in memory.
     */
    private Image cursor;
    private final Graphics graphics;
    /* 
     * Координаты на экране, курсора.
     * Cursor coordinates on screen.
     */
    private int curX, curY;

    /* 
     * Управление.
     * Controls.
     */
    private Thread threadRepaint;
    private int keyState, lastKeyState;
    private boolean running;

    public Game(String mapHeader, WaitScreen waitScreen) {
        super(true);
        waitScreen.setProgress(1, "Loading map...");
        setFullScreenMode(true);
        scrW = getWidth();
        scrH = getHeight();
        cX = scrW / 2;
        cY = scrH / 2;
        //Log.d(tag, "scrW=" + scrW);
        //Log.d(tag, "scrH=" + scrH);
        Map map = new Map(mapHeader);//Send BlueWarHeader file.
        w = map.getBlockSize() * map.getWbl();
        h = map.getBlockSize() * map.getHbl();
        //Log.d(tag, "W=" + w);
        //Log.d(tag, "H=" + h);
        /*
         * Если ширина карты меньше ширины экрана,
         * то присваиваем ложное значение центра экрана чтоб переменная не влияла на расчеты.
         * 
         * If map width is less than screen width,
         * assign a false center value so the variable doesn't affect calculations.
         */
        if (w < scrW) {
            cX = w + 1;
        }
        /*
         * Если высота карты меньше высоты экрана,
         * то присваиваем ложное значение центра экрана чтоб переменная не влияла на расчеты.
         * 
         * If map height is less than screen height,
         * assign a false center value so the variable doesn't affect calculations.
         */
        if (h < scrH) {
            cX = w + 1;
            cY = h + 1;
        }
        /*
         * Временно, потом вычислять коорд. персонажа.
         * Temporary, later calculate character coordinates.
         */
        mapX = w / 2;
        mapY = h / 2;
        waitScreen.setProgress(3, "Initializing map...");
        mapHolder = new MapHolder(map, scrW, scrH, mapX - getXOnScreen(mapX), mapY - getYOnScreen(mapY));
        moveType = MOVE_CURSOR;
        waitScreen.setProgress(4, "Loading graphics...");
        preLoadImages();
        graphics = getGraphics();
    }

    public void showNotify() {
        super.showNotify();
        /* 
         * Отрисуем сцену первый раз, потом обновляем при необходимости в треде
         * Draw the scene for the first time, then update as needed in the thread
         */
        updateGraphics();
        /* 
         * Используется в треде отрисовки.
         * Used in the rendering thread.
         */
        running = true;
        threadRepaint = new Thread(this);
        threadRepaint.start();
    }

    /* 
     * Цикл.
     * Main loop.
     */
    public void run() {
        while (running) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            lastKeyState = keyState;
            keyState = getKeyStates();
            //Log.d(tag, "calc");
            if ((keyState & LEFT_PRESSED) != 0) {
                mapX = mapX <= 0 ? 0 : mapX - 1;
            } else if ((keyState & UP_PRESSED) != 0) {
                mapY = mapY <= 0 ? 0 : mapY - 1;
            } else if ((keyState & RIGHT_PRESSED) != 0) {
                mapX = mapX >= w ? w : mapX + 1;
            } else if ((keyState & DOWN_PRESSED) != 0) {
                mapY = mapY >= h ? h : mapY + 1;
            }
            updateGraphics();
            try {
                if (lastKeyState != keyState) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException ex) {
            }
        }
    }

    private void updateGraphics() {
        curX = getXOnScreen(mapX);
        curY = getYOnScreen(mapY);
        mapHolder.draw(graphics, mapX - curX, mapY - curY);
        graphics.drawImage(cursor, curX - 7, curY - 7, Graphics.LEFT | Graphics.TOP);
        graphics.setColor(0xFF0000);
        graphics.drawString(getFPS(), 5, 5, 20);
        flushGraphics();
    }

    private int getXOnScreen(int mapX) {
        if (mapX < cX) {
            return mapX;
        } else if ((w - mapX) < cX) {
            return scrW - w + mapX;
        } else {
            return cX;
        }
    }

    private int getYOnScreen(int mapY) {
        if (mapY < cY) {
            return mapY;
        } else if ((h - mapY) < cY) {
            return scrH - h + mapY;
        } else {
            return cY;
        }
    }

    private void preLoadImages() {
        try {
            cursor = Image.createImage("/cursor.png");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getFPS() {
        FPS_Count++;
        if (FPS_Start == 0) {
            FPS_Start = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - FPS_Start >= 1000) {
            FPS = FPS_Count;
            FPS_Count = 0;
            FPS_Start = System.currentTimeMillis();
        }
        return Long.toString(FPS);
    }

    public void hideNotify() {
        running = false;
        mapHolder.stopMapDisplay();
    }
}
