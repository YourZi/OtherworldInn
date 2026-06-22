package com.otherworldinn.client;

import com.otherworldinn.client.renderer.RoomOutlineRenderer;

/**
 * 客户端钩子
 *
 * <p>用于在客户端执行特定的逻辑，避免在服务端加载客户端类。
 */
public class ClientHooks {

    public static void activateRoomOutline(int ticks) {
        RoomOutlineRenderer.activateTimedRoomOutline(ticks);
    }
}
