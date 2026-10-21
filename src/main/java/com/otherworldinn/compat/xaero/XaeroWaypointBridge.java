package com.otherworldinn.compat.xaero;

import com.otherworldinn.OtherworldInn;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * 把委托标点写进玩家客户端的 Xaero 小地图（反射调用，客户端专用）。
 *
 * <p>整表语义：先删掉所有名为「委托」的标点，再按给定坐标重建。Xaero 没有公开 API 包，这里调用其内部类：
 * {@code XaeroMinimapSession.getCurrentSession().getWaypointsManager().getCurrentWorld().getCurrentSet()
 * .add(new Waypoint(x, y, z, name, initials, color, purpose, boolean, boolean))}，
 * 签名已在 Xaero's Minimap 25.2.10 / MC 1.21.1 上核对。
 */
public final class XaeroWaypointBridge {

    /** 所有委托标点统一使用这个名称，整体覆盖/清理都以它为准。 */
    private static final String WAYPOINT_NAME = "委托";

    private static final String SESSION_CLASS = "xaero.common.XaeroMinimapSession";
    private static final String WAYPOINT_CLASS = "xaero.common.minimap.waypoints.Waypoint";
    private static final String WAYPOINT_SET_CLASS = "xaero.hud.minimap.waypoint.set.WaypointSet";
    private static final String COLOR_CLASS = "xaero.hud.minimap.waypoint.WaypointColor";
    private static final String PURPOSE_CLASS = "xaero.hud.minimap.waypoint.WaypointPurpose";

    /** Xaero 未安装或类结构不符时置位，之后不再尝试。 */
    private static boolean unavailable;
    private static boolean warned;

    private XaeroWaypointBridge() {}

    public static void replaceAll(long[] packedPositions) {
        if (unavailable) {
            return;
        }
        try {
            Object session = Class.forName(SESSION_CLASS).getMethod("getCurrentSession").invoke(null);
            if (session == null) {
                // 尚未进入世界：等下一次同步
                return;
            }
            Object manager = session.getClass().getMethod("getWaypointsManager").invoke(session);
            Object world = manager.getClass().getMethod("getCurrentWorld").invoke(manager);
            Object waypointSet = world.getClass().getMethod("getCurrentSet").invoke(world);
            if (waypointSet == null) {
                return;
            }

            Class<?> waypointClass = Class.forName(WAYPOINT_CLASS);
            Class<?> waypointSetClass = Class.forName(WAYPOINT_SET_CLASS);
            Method nameGetter = waypointClass.getMethod("getName");
            Method waypointsGetter = waypointSetClass.getMethod("getWaypoints");
            Method remove = waypointSetClass.getMethod("remove", waypointClass);
            Method add = waypointSetClass.getMethod("add", waypointClass);

            List<Object> stale = new ArrayList<>();
            for (Object waypoint : (Iterable<?>) waypointsGetter.invoke(waypointSet)) {
                if (waypoint == null) {
                    continue;
                }
                if (WAYPOINT_NAME.equals(nameGetter.invoke(waypoint))) {
                    stale.add(waypoint);
                }
            }
            for (Object waypoint : stale) {
                remove.invoke(waypointSet, waypoint);
            }

            Class<?> colorClass = Class.forName(COLOR_CLASS);
            Class<?> purposeClass = Class.forName(PURPOSE_CLASS);
            Object color = enumConstant(colorClass, "GOLD");
            Object purpose = enumConstant(purposeClass, "NORMAL");
            Constructor<?> constructor = waypointClass.getConstructor(
                    int.class,
                    int.class,
                    int.class,
                    String.class,
                    String.class,
                    colorClass,
                    purposeClass,
                    boolean.class,
                    boolean.class);
            for (long packed : packedPositions) {
                BlockPos pos = BlockPos.of(packed);
                Object waypoint = constructor.newInstance(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        WAYPOINT_NAME,
                        WAYPOINT_NAME,
                        color,
                        purpose,
                        true,
                        false);
                add.invoke(waypointSet, waypoint);
            }
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            unavailable = true;
            warnOnce("Xaero 小地图未安装或标点类结构不符，委托标点不会显示。", exception);
        } catch (ReflectiveOperationException exception) {
            warnOnce("写入 Xaero 委托标点失败。", exception);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<?> enumClass, String name) {
        return Enum.valueOf((Class<? extends Enum>) enumClass, name);
    }

    private static void warnOnce(String message, Throwable exception) {
        if (warned) {
            return;
        }
        warned = true;
        OtherworldInn.LOGGER.warn(message, exception);
    }
}
