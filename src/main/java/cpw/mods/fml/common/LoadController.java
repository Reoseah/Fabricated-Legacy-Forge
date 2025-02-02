/*
 * The FML Forge Mod Loader suite.
 * Copyright (C) 2012 cpw
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package cpw.mods.fml.common;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.event.FMLLoadEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadController {
    private Loader loader;
    private EventBus masterChannel;
    private ImmutableMap<String, EventBus> eventChannels;
    private LoaderState state;
    private Multimap<String, LoaderState.ModState> modStates = ArrayListMultimap.create();
    private Multimap<String, Throwable> errors = ArrayListMultimap.create();
    private Map<String, ModContainer> modList;
    private List<ModContainer> activeModList = Lists.newArrayList();
    private ModContainer activeContainer;
    private BiMap<ModContainer, Object> modObjectList;

    public LoadController(Loader loader) {
        this.loader = loader;
        this.masterChannel = new EventBus("FMLMainChannel");
        this.masterChannel.register(this);
        this.state = LoaderState.NOINIT;
    }

    @Subscribe
    public void buildModList(FMLLoadEvent event) {
        this.modList = this.loader.getIndexedModList();
        ImmutableMap.Builder<String, EventBus> eventBus = ImmutableMap.builder();

        for(ModContainer mod : this.loader.getModList()) {
            EventBus bus = new EventBus(mod.getModId());
            boolean isActive = mod.registerBus(bus, this);
            if (isActive) {
                Level level = Logger.getLogger(mod.getModId()).getLevel();
                FMLLog.log(
                        mod.getModId(),
                        Level.FINE,
                        "Mod Logging channel %s configured at %s level.",
                        new Object[]{mod.getModId(), level == null ? "default" : level}
                );
                FMLLog.log(mod.getModId(), Level.INFO, "Activating mod %s", new Object[]{mod.getModId()});
                this.activeModList.add(mod);
                this.modStates.put(mod.getModId(), LoaderState.ModState.UNLOADED);
                eventBus.put(mod.getModId(), bus);
            } else {
                FMLLog.log(mod.getModId(), Level.WARNING, "Mod %s has been disabled through configuration", new Object[]{mod.getModId()});
                this.modStates.put(mod.getModId(), LoaderState.ModState.UNLOADED);
                this.modStates.put(mod.getModId(), LoaderState.ModState.DISABLED);
            }
        }

        this.eventChannels = eventBus.build();
    }

    public void distributeStateMessage(LoaderState state, Object... eventData) {
        if (state.hasEvent()) {
            this.masterChannel.post(state.getEvent(eventData));
        }
    }

    public void transition(LoaderState desiredState) {
        LoaderState oldState = this.state;
        this.state = this.state.transition(!this.errors.isEmpty());
        if (this.state != desiredState) {
            Throwable toThrow = null;
            FMLLog.severe("Fatal errors were detected during the transition from %s to %s. Loading cannot continue", new Object[]{oldState, desiredState});
            StringBuilder sb = new StringBuilder();
            this.printModStates(sb);
            FMLLog.getLogger().severe(sb.toString());
            if (this.errors.size() > 0) {
                FMLLog.severe("The following problems were captured during this phase", new Object[0]);

                for(Map.Entry<String, Throwable> error : this.errors.entries()) {
                    FMLLog.log(Level.SEVERE, (Throwable)error.getValue(), "Caught exception from %s", new Object[]{error.getKey()});
                    if (error.getValue() instanceof IFMLHandledException) {
                        toThrow = (Throwable)error.getValue();
                    } else if (toThrow == null) {
                        toThrow = (Throwable)error.getValue();
                    }
                }

                if (toThrow != null && toThrow instanceof RuntimeException) {
                    throw (RuntimeException)toThrow;
                } else {
                    throw new LoaderException(toThrow);
                }
            } else {
                FMLLog.severe(
                        "The ForgeModLoader state engine has become corrupted. Probably, a state was missed by and invalid modification to a base classForgeModLoader depends on. This is a critical error and not recoverable. Investigate any modifications to base classes outside ofForgeModLoader, especially Optifine, to see if there are fixes available.",
                        new Object[0]
                );
                throw new RuntimeException("The ForgeModLoader state engine is invalid");
            }
        }
    }

    public ModContainer activeContainer() {
        return this.activeContainer;
    }

    @Subscribe
    public void propogateStateMessage(FMLEvent stateEvent) {
        if (stateEvent instanceof FMLPreInitializationEvent) {
            this.modObjectList = this.buildModObjectList();
        }

        for(ModContainer mc : this.activeModList) {
            this.activeContainer = mc;
            String modId = mc.getModId();
            stateEvent.applyModContainer(this.activeContainer());
            FMLLog.log(modId, Level.FINEST, "Sending event %s to mod %s", new Object[]{stateEvent.getEventType(), modId});
            ((EventBus)this.eventChannels.get(modId)).post(stateEvent);
            FMLLog.log(modId, Level.FINEST, "Sent event %s to mod %s", new Object[]{stateEvent.getEventType(), modId});
            this.activeContainer = null;
            if (stateEvent instanceof FMLStateEvent) {
                if (!this.errors.containsKey(modId)) {
                    this.modStates.put(modId, ((FMLStateEvent)stateEvent).getModState());
                } else {
                    this.modStates.put(modId, LoaderState.ModState.ERRORED);
                }
            }
        }
    }

    public ImmutableBiMap<ModContainer, Object> buildModObjectList() {
        com.google.common.collect.ImmutableBiMap.Builder<ModContainer, Object> builder = ImmutableBiMap.builder();

        for(ModContainer mc : this.activeModList) {
            if (!mc.isImmutable() && mc.getMod() != null) {
                builder.put(mc, mc.getMod());
            }

            if (mc.getMod() == null && !mc.isImmutable() && this.state != LoaderState.CONSTRUCTING) {
                FMLLog.severe("There is a severe problem with %s - it appears not to have constructed correctly", new Object[]{mc.getModId()});
                if (this.state != LoaderState.CONSTRUCTING) {
                    this.errorOccurred(mc, new RuntimeException());
                }
            }
        }

        return builder.build();
    }

    public void errorOccurred(ModContainer modContainer, Throwable exception) {
        if (exception instanceof InvocationTargetException) {
            this.errors.put(modContainer.getModId(), ((InvocationTargetException)exception).getCause());
        } else {
            this.errors.put(modContainer.getModId(), exception);
        }
    }

    public void printModStates(StringBuilder ret) {
        for(ModContainer mc : this.loader.getModList()) {
            ret.append("\n\t").append(mc.getModId()).append(" [").append(mc.getName()).append("] (").append(mc.getSource().getName()).append(") ");
            Joiner.on("->").appendTo(ret, this.modStates.get(mc.getModId()));
        }
    }

    public List<ModContainer> getActiveModList() {
        return this.activeModList;
    }

    public LoaderState.ModState getModState(ModContainer selectedMod) {
        return (LoaderState.ModState)Iterables.getLast(this.modStates.get(selectedMod.getModId()), LoaderState.ModState.AVAILABLE);
    }

    public void distributeStateMessage(Class<?> customEvent) {
        try {
            this.masterChannel.post(customEvent.newInstance());
        } catch (Exception var3) {
            FMLLog.log(Level.SEVERE, var3, "An unexpected exception", new Object[0]);
            throw new LoaderException(var3);
        }
    }

    public BiMap<ModContainer, Object> getModObjectList() {
        if (this.modObjectList == null) {
            FMLLog.severe(
                    "Detected an attempt by a mod %s to perform game activity during mod construction. This is a serious programming error.",
                    new Object[]{this.activeContainer}
            );
            return this.buildModObjectList();
        } else {
            return ImmutableBiMap.copyOf(this.modObjectList);
        }
    }

    public boolean isInState(LoaderState state) {
        return this.state == state;
    }

    boolean hasReachedState(LoaderState state) {
        return this.state.ordinal() >= state.ordinal() && this.state != LoaderState.ERRORED;
    }
}
