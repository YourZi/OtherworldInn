package com.otherworldinn.client;

import com.otherworldinn.client.renderer.RoomOutlineRenderer;

/**
 * 客户端钩子：在客户端执行特定逻辑，避免服务端加载客户端类。
 */
public class ClientHooks {

    public static void activateRoomOutline(int ticks) {
        RoomOutlineRenderer.activateTimedRoomOutline(ticks);
    }
}
