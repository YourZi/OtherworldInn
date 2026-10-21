package com.otherworldinn.foundation;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 客户端配置：相机投影参数、GUI 动效两项开关（对应 Subtle GUI Tweaks，MIT）与游玩时长显示开关。
 */
public class ClientConfig {
    public static final ClientConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    static {
        final Pair<ClientConfig, ModConfigSpec> specPair =
                new ModConfigSpec.Builder().configure(ClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public final ModConfigSpec.DoubleValue cameraX;
    public final ModConfigSpec.DoubleValue cameraY;
    public final ModConfigSpec.DoubleValue cameraZ;
    public final ModConfigSpec.DoubleValue cameraPitch;
    public final ModConfigSpec.DoubleValue cameraYaw;
    public final ModConfigSpec.DoubleValue orthoSize;

    public final ModConfigSpec.BooleanValue itemAnimations;
    public final ModConfigSpec.BooleanValue interfaceAnimations;

    public final ModConfigSpec.BooleanValue showWorldPlayTime;
    public final ModConfigSpec.BooleanValue showServerPlayTime;

    public ClientConfig(ModConfigSpec.Builder builder) {
        builder.push("camera");
        cameraX = builder.comment("Camera X Position").defineInRange("x", -46.188, -100000.0, 100000.0);
        cameraY = builder.comment("Camera Y Position").defineInRange("y", 116.188, -64.0, 1000.0);
        cameraZ = builder.comment("Camera Z Position").defineInRange("z", -46.188, -100000.0, 100000.0);
        cameraPitch =
                builder.comment("Camera Pitch (90 is looking straight down)")
                        .defineInRange("pitch", 35.264, -90.0, 90.0);
        cameraYaw = builder.comment("Camera Yaw").defineInRange("yaw", -45.0, -360.0, 360.0);
        orthoSize =
                builder.comment("Orthographic View Size (Zoom Level)")
                        .defineInRange("orthoSize", 22.0, 1.0, 200.0);
        builder.pop();

        builder.comment("GUI 动效（移植自 Subtle GUI Tweaks，MIT，署名见 NOTICE-smooth-gui.md）")
                .push("gui_animations");
        itemAnimations =
                builder.comment("物品动效：槽位悬停放大、点击弹出、手持物品倾斜、输出槽弹出、同物品抖动、热键栏空放抖动")
                        .define("item_animations", true);
        interfaceAnimations =
                builder.comment("界面动效：按钮悬停缩放、控件入场错峰、容器面板展开、列表条目级联、界面滑入、槽位高亮淡入淡出、tooltip 弹出")
                        .define("interface_animations", true);
        builder.pop();

        builder.comment("游玩时长显示（复刻 Linggango display_tweaks）").push("playtime");
        showWorldPlayTime =
                builder.comment("存档列表条目右上角显示该存档累计游玩时长（读取 saves/<id>/stats/*.json 的 play_time）")
                        .define("showWorldPlayTime", true);
        showServerPlayTime =
                builder.comment("服务器列表条目显示累计游玩时长（客户端本地统计，存到 config/otherworldinn_server_playtime.properties）")
                        .define("showServerPlayTime", true);
        builder.pop();
    }

    /**
     * 物品动效总开关。配置在客户端启动较晚阶段才加载，而按钮/HUD 在加载界面就会渲染，
     * 此时读 {@code get()} 会抛 {@code IllegalStateException}，故统一捕获并回落到默认值。
     */
    public static boolean itemAnimations() {
        try {
            return INSTANCE.itemAnimations.get();
        } catch (RuntimeException notLoadedYet) {
            return true;
        }
    }

    public static boolean interfaceAnimations() {
        try {
            return INSTANCE.interfaceAnimations.get();
        } catch (RuntimeException notLoadedYet) {
            return true;
        }
    }

    /** 存档列表显示游玩时长（配置未加载时按默认开启）。 */
    public static boolean showWorldPlayTime() {
        try {
            return INSTANCE.showWorldPlayTime.get();
        } catch (RuntimeException notLoadedYet) {
            return true;
        }
    }

    /** 服务器列表显示游玩时长（配置未加载时按默认开启）。 */
    public static boolean showServerPlayTime() {
        try {
            return INSTANCE.showServerPlayTime.get();
        } catch (RuntimeException notLoadedYet) {
            return true;
        }
    }
}
