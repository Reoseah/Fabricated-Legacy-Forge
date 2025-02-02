package fr.catcore.fabricatedforge.mixininterface;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public interface IBlockEntity {

    boolean canUpdate();

    void onDataPacket(Connection net, BlockEntityUpdateS2CPacket pkt);

    void onChunkUnload();

    boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z);

    boolean shouldRenderInPass(int pass);

    @Environment(EnvType.CLIENT)
    Box getRenderBoundingBox();
}
