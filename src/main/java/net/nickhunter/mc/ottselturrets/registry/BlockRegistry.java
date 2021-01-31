package net.nickhunter.mc.ottselturrets.registry;

import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.BallistaTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.TurretBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            OttselTurrets.MOD_ID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // Blocks
    public static final RegistryObject<TurretBlock> LASER_TURRET = BLOCKS.register(LaserTurretBlock.RESOURCE_NAME,
            () -> new LaserTurretBlock());
    public static final RegistryObject<TurretBlock> BALLISTA_TURRET = BLOCKS.register(BallistaTurretBlock.RESOURCE_NAME,
            () -> new BallistaTurretBlock());

}
