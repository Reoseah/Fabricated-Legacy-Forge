package fr.catcore.fabricatedforge.mixin.forgefml.server.network;

import cpw.mods.fml.common.FMLLog;
import fr.catcore.fabricatedforge.mixininterface.IPacketListener;
import net.minecraft.network.IntegratedConnection;
import net.minecraft.server.ServerPacketListener;
import net.minecraft.server.network.PacketListenerManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mixin(PacketListenerManager.class)
public class PacketListenerManagerMixin {

    @Shadow @Final private List<ServerPacketListener> packetListeners;

    @Shadow public static Logger LOGGER;

    /**
     * @author Minecraft Forge
     * @reason none
     */
    @Overwrite
    public void handlePackets() {
        for(int var1 = 0; var1 < this.packetListeners.size(); ++var1) {
            ServerPacketListener var2 = (ServerPacketListener)this.packetListeners.get(var1);

            try {
                var2.tick();
            } catch (Exception var5) {
                if (var2.connection instanceof IntegratedConnection) {
                    CrashReport var4 = CrashReport.create(var5, "Ticking memory connection");
                    throw new CrashException(var4);
                }

                FMLLog.log(Level.SEVERE, var5, "A critical server error occured handling a packet, kicking %s", new Object[]{((IPacketListener)var2).getPlayer().id});
                LOGGER.log(Level.WARNING, "Failed to handle packet for " + var2.player.getTranslationKey() + "/" + var2.player.getIp() + ": " + var5, var5);
                var2.disconnect("Internal server error");
            }

            if (var2.field_2895) {
                this.packetListeners.remove(var1--);
            }

            var2.connection.wakeThreads();
        }
    }
}
