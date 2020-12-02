package net.nickhunter.mc.ottselturrets.registry;

import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileRegistry {

    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, OttselTurrets.MOD_ID);

    public static void init(){ TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus()); }

    public static final RegistryObject<TileEntityType<TurretTileEntity>> TURRET = TILE_ENTITIES.register("turret", () -> TileEntityType.Builder.create(
            TurretTileEntity::new,
            new Block[]{
                    BlockRegistry.LASER_TURRET.get()}
            ).build(null)
    );
}
