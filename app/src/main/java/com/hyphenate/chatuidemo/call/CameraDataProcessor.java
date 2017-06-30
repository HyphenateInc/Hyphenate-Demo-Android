package com.hyphenate.chatuidemo.call;

import android.hardware.Camera;
import com.hyphenate.chat.EMCallManager;

/**
 * Created by lzan13 on 2016/8/9.
 *
 * call camera data callback
 */
public class CameraDataProcessor implements EMCallManager.EMCameraDataProcessor {

    byte yDelta = 0;

    synchronized void setYDelta(byte yDelta) {
        this.yDelta = yDelta;
    }

    // data size is width*height*2
    // the first width*height is Y, second part is UV
    // the storage layout detailed please refer 2.x demo CameraHelper.onPreviewFrame
    @Override public synchronized void onProcessData(byte[] data, Camera camera, int width,
            int height, int rotation) {
        int wh = width * height;
        for (int i = 0; i < wh; i++) {
            int d = (data[i] & 0xFF) + yDelta;
            d = d < 16 ? 16 : d;
            d = d > 235 ? 235 : d;
            data[i] = (byte) d;
        }
    }
}
