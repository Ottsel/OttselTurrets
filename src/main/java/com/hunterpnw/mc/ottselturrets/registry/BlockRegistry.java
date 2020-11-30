package com.hunterpnw.mc.ottselturrets.registry;

import com.hunterpnw.mc.ottselturrets.OttselTurrets;
import com.hunterpnw.mc.ottselturrets.TurretType;
import com.hunterpnw.mc.ottselturrets.blocks.TurretBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, OttselTurrets.MOD_ID);

    public static void init(){
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //Blocks
    public static final RegistryObject<Block> LASER_TURRET = BLOCKS.register("laser_turret", () -> new TurretBlock(TurretType.LASER));

}
