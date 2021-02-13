package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.BallistaTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserTurretBlock;
import net.nickhunter.mc.ottselturrets.blocks.LaserNodeBlock;
import net.nickhunter.mc.ottselturrets.blocks.tile.BallistaTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserNodeTileEntity;

public class TileRegistry {

        private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister
                        .create(ForgeRegistries.TILE_ENTITIES, OttselTurrets.MOD_ID);

        public static void init() {
                TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        public static final RegistryObject<TileEntityType<LaserTurretTileEntity>> LASER_TURRET = TILE_ENTITIES.register(
                        LaserTurretBlock.RESOURCE_NAME, () -> TileEntityType.Builder.create(LaserTurretTileEntity::new,
                                        new Block[] { BlockRegistry.LASER_TURRET.get() }).build(null));
        public static final RegistryObject<TileEntityType<BallistaTurretTileEntity>> BALLISTA_TURRET = TILE_ENTITIES
                        .register(BallistaTurretBlock.RESOURCE_NAME,
                                        () -> TileEntityType.Builder.create(BallistaTurretTileEntity::new,
                                                        new Block[] { BlockRegistry.BALLISTA_TURRET.get() })
                                                        .build(null));
        public static final RegistryObject<TileEntityType<LaserNodeTileEntity>> LASER_NODE = TILE_ENTITIES
                        .register(LaserNodeBlock.RESOURCE_NAME,
                                        () -> TileEntityType.Builder
                                                        .create(LaserNodeTileEntity::new,
                                                                        new Block[] { BlockRegistry.LASER_NODE.get() })
                                                        .build(null));
}
