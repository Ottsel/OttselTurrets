package net.nickhunter.mc.ottselturrets.registry;

import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.BallistaTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserTurretBlock;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            OttselTurrets.MOD_ID);

    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // BlockItems
    public static final RegistryObject<Item> LASER_TURRET = ITEMS.register(LaserTurretBlock.RESOURCE_NAME,
            () -> new TurretBlockItem(BlockRegistry.LASER_TURRET.get()));
    public static final RegistryObject<Item> BALLISTA_TURRET = ITEMS.register(BallistaTurretBlock.RESOURCE_NAME,
            () -> new TurretBlockItem(BlockRegistry.BALLISTA_TURRET.get()));

}
