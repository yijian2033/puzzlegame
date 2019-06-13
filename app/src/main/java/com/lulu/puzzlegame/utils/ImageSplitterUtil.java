package com.lulu.puzzlegame.utils;

import android.graphics.Bitmap;

import com.lulu.puzzlegame.mode.ImagePiece;

import java.util.ArrayList;
import java.util.List;

/**
 * create by zyj
 * on 2019/6/12
 **/
public class ImageSplitterUtil {

    /**
     * @param bitmap 原图片
     * @param piece  切成piece*piece块
     * @return List<ImagePiece>
     */
    public static List<ImagePiece> spliteImage(Bitmap bitmap, int piece) {

        List<ImagePiece> imagePieces = new ArrayList<>();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pieceWidth = Math.min(width, height) / piece;

        //几行几列
        for (int i = 0; i < piece; i++) {
            for (int j = 0; j < piece; j++) {
                ImagePiece imagePiece = new ImagePiece();
                //j+i*piece可以按自然数取下去
                imagePiece.setIndex(j + i * piece);
                //获取每个的坐标点
                int x = j * pieceWidth;
                int y = i * pieceWidth;
                imagePiece.setBitmap(Bitmap.createBitmap(bitmap, x, y, pieceWidth, pieceWidth));
                imagePieces.add(imagePiece);
            }
        }
        return imagePieces;
    }

}
