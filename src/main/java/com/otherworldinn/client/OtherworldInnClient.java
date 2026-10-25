package com.otherworldinn.client;

import com.otherworldinn.OtherworldInn;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端专用模组入口。
 *
 * <p>只做"模组列表 → 配置"按钮的注册。放在客户端专用 {@code @Mod(dist = CLIENT)} 里，
 * 是因为 {@link IConfigScreenFactory} 的方法签名引用 {@code Screen}，
 * 主类只要出现它的类常量，专用服务端就会在解析主类时崩溃。
 */
@Mod(value = OtherworldInn.MODID, dist = Dist.CLIENT)
public class OtherworldInnClient {
    public OtherworldInnClient(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory)
                        (modContainer, parent) ->
                                new com.otherworldinn.client.gui.screen.GuiAnimationConfigScreen(
                                        parent));
    }
}
