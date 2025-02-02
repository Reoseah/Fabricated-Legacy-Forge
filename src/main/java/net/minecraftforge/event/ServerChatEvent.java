/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */
package net.minecraftforge.event;

import net.minecraft.entity.player.ServerPlayerEntity;

@Cancelable
public class ServerChatEvent extends Event {
    public final String message;
    public final String username;
    public final ServerPlayerEntity player;
    public String line;

    public ServerChatEvent(ServerPlayerEntity player, String message, String line) {
        this.message = message;
        this.player = player;
        this.username = player.username;
        this.line = line;
    }
}
