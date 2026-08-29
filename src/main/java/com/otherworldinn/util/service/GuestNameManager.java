package com.otherworldinn.util.service;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

/** 旅客随机姓名管理器，姓名由名与姓组成，经翻译键支持本地化。 */
public class GuestNameManager {

    public static final int FIRST_NAME_COUNT = 40;
    public static final int LAST_NAME_COUNT = 40;

    public static Component getRandomName(RandomSource random) {
        int firstIndex = random.nextInt(FIRST_NAME_COUNT) + 1;
        int lastIndex = random.nextInt(LAST_NAME_COUNT) + 1;

        String firstNameKey = "guest.name.first." + firstIndex;
        String lastNameKey = "guest.name.last." + lastIndex;

        return Component.translatable(
                "guest.name.format",
                Component.translatable(firstNameKey),
                Component.translatable(lastNameKey));
    }
}
