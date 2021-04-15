package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.BallistaTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserNodeBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserTurretBlock;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import net.nickhunter.mc.ottselturrets.items.LaserWeaponItem;

public class ItemRegistry {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        OttselTurrets.MOD_ID);

        public static void init() {
                ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        // BlockItems
        public static final RegistryObject<Item> LASER_TURRET = ITEMS.register(LaserTurretBlock.RESOURCE_NAME,
                        () -> new AnimatedBlockItem(BlockRegistry.LASER_TURRET.get()));
        public static final RegistryObject<Item> BALLISTA_TURRET = ITEMS.register(BallistaTurretBlock.RESOURCE_NAME,
                        () -> new AnimatedBlockItem(BlockRegistry.BALLISTA_TURRET.get()));
        public static final RegistryObject<Item> LASER_NODE = ITEMS.register(LaserNodeBlock.RESOURCE_NAME,
                        () -> new AnimatedBlockItem(BlockRegistry.LASER_NODE.get()));

        // Items
        public static final RegistryObject<Item> LASER_WEAPON = ITEMS.register(LaserWeaponItem.RESOURCE_NAME,
                        () -> new LaserWeaponItem(new Item.Properties().tab(ItemGroupRegistry.MAIN)));
        public static final RegistryObject<Item> LASER_DIODE = ITEMS.register("laser_diode",
                        () -> new Item(new Item.Properties().tab(ItemGroupRegistry.MAIN)));
        public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
                        () -> new Item(new Item.Properties().tab(ItemGroupRegistry.MAIN)));
        public static final RegistryObject<Item> MECHANICAL_LEGS = ITEMS.register("mechanical_legs",
                        () -> new Item(new Item.Properties().tab(ItemGroupRegistry.MAIN)));
        public static final RegistryObject<Item> TURRET_CORE = ITEMS.register("turret_core",
                        () -> new Item(new Item.Properties().tab(ItemGroupRegistry.MAIN)));
}
