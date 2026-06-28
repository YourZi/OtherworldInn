package com.otherworldinn.world.storyguest;

import com.otherworldinn.world.photo.StoryGuestPhotoTaskRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class StoryGuestTodoRegistry {
    private static final Map<OptionKey, TodoEntry> ACCEPT_OPTION_TODOS = new HashMap<>();
    private static final Map<OptionKey, TodoEntry> COMPLETION_OPTION_TODOS = new HashMap<>();
    private static final Map<FlagKey, TodoEntry> COMPLETION_FLAG_TODOS = new HashMap<>();

    static {
        registerItemTask(
                "wandering_cartographer",
                "todo.otherworldinn.story.cartographer_supplies",
                "story_cartographer_leave_intro",
                "story_cartographer_offer_supplies");
        registerPhotoTask(
                "wandering_cartographer",
                "todo.otherworldinn.story.cartographer_ocean_photo",
                "story_cartographer_accept_ocean_photo_task",
                StoryGuestPhotoTaskRegistry.CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "wandering_minstrel",
                "todo.otherworldinn.story.minstrel_supplies",
                "story_minstrel_leave_intro",
                "story_minstrel_offer_supplies");
        registerPhotoTask(
                "wandering_minstrel",
                "todo.otherworldinn.story.minstrel_plains_photo",
                "story_minstrel_accept_plains_photo_task",
                StoryGuestPhotoTaskRegistry.MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "wandering_chef",
                "todo.otherworldinn.story.chef_meal",
                "story_chef_leave_intro",
                "story_chef_offer_supplies");
        registerPhotoTask(
                "wandering_chef",
                "todo.otherworldinn.story.chef_farm_photo",
                "story_chef_accept_farm_photo_task",
                StoryGuestPhotoTaskRegistry.CHEF_FARM_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "fallen_noble",
                "todo.otherworldinn.story.noble_attire",
                "story_noble_leave_intro",
                "story_noble_offer_attire");
        registerPhotoTask(
                "fallen_noble",
                "todo.otherworldinn.story.noble_mansion_photo",
                "story_noble_accept_mansion_photo_task",
                StoryGuestPhotoTaskRegistry.NOBLE_MANSION_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "wandering_alchemist",
                "todo.otherworldinn.story.alchemist_supplies",
                "story_alchemist_leave_intro",
                "story_alchemist_offer_supplies");
        registerPhotoTask(
                "wandering_alchemist",
                "todo.otherworldinn.story.alchemist_fortress_photo",
                "story_alchemist_accept_fortress_photo_task",
                StoryGuestPhotoTaskRegistry.ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "archaeologist",
                "todo.otherworldinn.story.archaeologist_tools",
                "story_archaeologist_leave_intro",
                "story_archaeologist_offer_tools");
        registerPhotoTask(
                "archaeologist",
                "todo.otherworldinn.story.archaeologist_temple_photo",
                "story_archaeologist_accept_temple_photo_task",
                StoryGuestPhotoTaskRegistry.ARCHAEOLOGIST_TEMPLE_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "gem_merchant",
                "todo.otherworldinn.story.gem_merchant_wagon_supplies",
                "story_gem_merchant_leave_intro",
                "story_gem_merchant_offer_wagon_supplies");
        registerPhotoTask(
                "gem_merchant",
                "todo.otherworldinn.story.gem_merchant_ore_photo",
                "story_gem_merchant_accept_ore_photo_task",
                StoryGuestPhotoTaskRegistry.GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG);

        registerItemTask(
                "old_knight",
                "todo.otherworldinn.story.old_knight_repairs",
                "story_old_knight_leave_intro",
                "story_old_knight_offer_repairs");
    }

    private StoryGuestTodoRegistry() {}

    @Nullable
    public static String resolveAcceptedTodoText(String storyGuestId, String optionId) {
        TodoEntry entry = ACCEPT_OPTION_TODOS.get(new OptionKey(storyGuestId, optionId));
        return entry == null ? null : entry.resolveText();
    }

    @Nullable
    public static String resolveCompletedTodoTextByOption(String storyGuestId, String optionId) {
        TodoEntry entry = COMPLETION_OPTION_TODOS.get(new OptionKey(storyGuestId, optionId));
        return entry == null ? null : entry.resolveText();
    }

    @Nullable
    public static String resolveCompletedTodoTextByFlag(String storyGuestId, String completionFlag) {
        TodoEntry entry = COMPLETION_FLAG_TODOS.get(new FlagKey(storyGuestId, completionFlag));
        return entry == null ? null : entry.resolveText();
    }

    private static void registerItemTask(
            String storyGuestId, String todoKey, String acceptOptionId, String completionOptionId) {
        TodoEntry entry = new TodoEntry(todoKey);
        ACCEPT_OPTION_TODOS.put(new OptionKey(storyGuestId, acceptOptionId), entry);
        COMPLETION_OPTION_TODOS.put(new OptionKey(storyGuestId, completionOptionId), entry);
    }

    private static void registerPhotoTask(
            String storyGuestId, String todoKey, String acceptOptionId, String completionFlag) {
        TodoEntry entry = new TodoEntry(todoKey);
        ACCEPT_OPTION_TODOS.put(new OptionKey(storyGuestId, acceptOptionId), entry);
        COMPLETION_FLAG_TODOS.put(new FlagKey(storyGuestId, completionFlag), entry);
    }

    private record OptionKey(String storyGuestId, String optionId) {}

    private record FlagKey(String storyGuestId, String flag) {}

    private record TodoEntry(String todoKey) {
        private String resolveText() {
            return Component.translatable(todoKey).getString();
        }
    }
}
