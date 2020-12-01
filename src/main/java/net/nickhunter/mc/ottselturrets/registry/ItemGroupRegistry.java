package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupRegistry {

    //Main item group
    public static final ItemGroup MAIN = new ItemGroup("ottselturrets") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ItemRegistry.LASER_TURRET.get());
        }
    };
}
