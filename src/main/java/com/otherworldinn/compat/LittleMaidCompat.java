package com.otherworldinn.compat;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.BaubleManager;
import com.otherworldinn.compat.task.FrontDeskTask;
import com.otherworldinn.compat.task.RoomCleanTask;

/**
 * 女仆模组扩展入口；@LittleMaidExtension 保证未安装女仆模组时本类不会被加载
 */
@LittleMaidExtension
public class LittleMaidCompat implements ILittleMaid {
    // 女仆模组会在合适时机调用此构造函数，可在此注册女仆专属事件
    public LittleMaidCompat() {
    }

    /**
     * 绑定女仆饰品：将自定义饰品与物品绑定
     */
    @Override
    public void bindMaidBauble(BaubleManager manager) {
    }

    /**
     * 注册女仆工作任务
     */
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new RoomCleanTask());
        manager.add(new FrontDeskTask());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
    }
}
