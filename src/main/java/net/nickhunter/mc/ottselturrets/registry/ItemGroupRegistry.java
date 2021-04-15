package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.nickhunter.mc.ottselturrets.OttselTurrets;

public class ItemGroupRegistry {

    // Main item group
    public static final ItemGroup MAIN = new ItemGroup(OttselTurrets.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.LASER_TURRET.get());
        }
    };
}
