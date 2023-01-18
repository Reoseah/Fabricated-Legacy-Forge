/**
 * This software is provided under the terms of the Minecraft Forge Public
 * License v1.0.
 */
package net.minecraftforge.event.entity.living;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraftforge.event.Cancelable;

@Cancelable
public class LivingHurtEvent extends LivingEvent {
    public final DamageSource source;
    public int ammount;

    public LivingHurtEvent(MobEntity entity, DamageSource source, int ammount) {
        super(entity);
        this.source = source;
        this.ammount = ammount;
    }
}
