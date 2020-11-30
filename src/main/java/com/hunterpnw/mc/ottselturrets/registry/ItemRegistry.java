package com.hunterpnw.mc.ottselturrets.registry;

import com.hunterpnw.mc.ottselturrets.OttselTurrets;
import com.hunterpnw.mc.ottselturrets.TurretType;
import com.hunterpnw.mc.ottselturrets.items.TurretBlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OttselTurrets.MOD_ID);

    public static void init(){
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //BlockItems
    public static final RegistryObject<Item> LASER_TURRET = ITEMS.register("laser_turret",() -> new TurretBlockItem(BlockRegistry.LASER_TURRET.get(), TurretType.LASER));

}
