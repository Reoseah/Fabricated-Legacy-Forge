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
package cpw.mods.fml.client;

import com.google.common.base.Strings;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import fr.catcore.fabricatedforge.forged.FabricModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Language;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GuiModList extends Screen {
    private Screen mainMenu;
    private GuiSlotModList modList;
    private int selected = -1;
    private ModContainer selectedMod;
    private int listWidth;
    private ArrayList<ModContainer> mods;

    public GuiModList(Screen mainMenu) {
        this.mainMenu=mainMenu;
        this.mods=new ArrayList<ModContainer>();
        FMLClientHandler.instance().addSpecialModEntries(mods);
        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod.getMetadata()!=null && !Strings.isNullOrEmpty(mod.getMetadata().parent)) {
                String parentMod = mod.getMetadata().parent;
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(parentMod);
                if (parentContainer != null)
                {
                    mod.getMetadata().parentMod = parentContainer;
                    parentContainer.getMetadata().childMods.add(mod);
                    continue;
                }
            }
            mods.add(mod);
        }

        for (net.fabricmc.loader.api.ModContainer container : FabricLoader.getInstance().getAllMods()) {
            mods.add(new FabricModContainer(container));
        }
    }

    public void init() {
        for (ModContainer mod : mods) {
            listWidth=Math.max(listWidth,getFontRenderer().getStringWidth(mod.getName()) + 10);
            listWidth=Math.max(listWidth,getFontRenderer().getStringWidth(mod.getVersion()) + 10);
        }
        listWidth=Math.min(listWidth, 150);
        Language translations = Language.getInstance();
        this.buttons.add(new OptionButtonWidget(6, this.width / 2 - 75, this.height - 38, translations.translate("gui.done")));
        this.modList=new GuiSlotModList(this, mods, listWidth);
        this.modList.registerScrollButtons(this.buttons, 7, 8);
    }

    protected void buttonClicked(ButtonWidget button) {
        if (button.active) {
            switch (button.id) {
                case 6:
                    this.field_1229.openScreen(this.mainMenu);
                    return;
            }
        }

        super.buttonClicked(button);
    }

    public int drawLine(String line, int offset, int shifty) {
        this.textRenderer.method_964(line, offset, shifty, 14151146);
        return shifty + 10;
    }

    public void render(int p_571_1_, int p_571_2_, float p_571_3_) {
        this.modList.drawScreen(p_571_1_, p_571_2_, p_571_3_);
        this.drawCenteredString(this.textRenderer, "Mod List", this.width / 2, 16, 16777215);
        int offset = this.listWidth + 20;
        if (this.selectedMod != null) {
            GL11.glEnable(3042);
            if (!this.selectedMod.getMetadata().autogenerated) {
                int shifty = 35;
                if (!this.selectedMod.getMetadata().logoFile.isEmpty()) {
                    int texture = this.field_1229.textureManager.getTextureFromPath(this.selectedMod.getMetadata().logoFile);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.field_1229.textureManager.bindTexture(texture);
                    Dimension dim = TextureFXManager.instance().getTextureDimensions(texture);
                    int top = 32;
                    Tessellator tess = Tessellator.INSTANCE;
                    tess.begin();
                    tess.vertex((double)offset, (double)(top + dim.height), (double)this.zOffset, 0.0, 1.0);
                    tess.vertex((double)(offset + dim.width), (double)(top + dim.height), (double)this.zOffset, 1.0, 1.0);
                    tess.vertex((double)(offset + dim.width), (double)top, (double)this.zOffset, 1.0, 0.0);
                    tess.vertex((double)offset, (double)top, (double)this.zOffset, 0.0, 0.0);
                    tess.end();
                    shifty += 65;
                }

                this.textRenderer.method_956(this.selectedMod.getMetadata().name, offset, shifty, 16777215);
                shifty += 12;
                shifty = this.drawLine(String.format("Version: %s (%s)", this.selectedMod.getDisplayVersion(), this.selectedMod.getVersion()), offset, shifty);
                shifty = this.drawLine(String.format("Mod State: %s", Loader.instance().getModState(this.selectedMod)), offset, shifty);
                if (!this.selectedMod.getMetadata().credits.isEmpty()) {
                    shifty = this.drawLine(String.format("Credits: %s", this.selectedMod.getMetadata().credits), offset, shifty);
                }

                shifty = this.drawLine(String.format("Authors: %s", this.selectedMod.getMetadata().getAuthorList()), offset, shifty);
                shifty = this.drawLine(String.format("URL: %s", this.selectedMod.getMetadata().url), offset, shifty);
                shifty = this.drawLine(this.selectedMod.getMetadata().childMods.isEmpty() ? "No child mods for this mod" : String.format("Child mods: %s", this.selectedMod.getMetadata().getChildModList()), offset, shifty);
                TextRenderer var10000 = this.getFontRenderer();
                int var10003 = shifty + 10;
                int var10004 = this.width - offset - 20;
                var10000.drawTrimmed(this.selectedMod.getMetadata().description, offset, var10003, var10004, 14540253);
            } else {
                offset = (this.listWidth + this.width) / 2;
                this.drawCenteredString(this.textRenderer, this.selectedMod.getName(), offset, 35, 16777215);
                this.drawCenteredString(this.textRenderer, String.format("Version: %s", this.selectedMod.getVersion()), offset, 45, 16777215);
                this.drawCenteredString(this.textRenderer, String.format("Mod State: %s", Loader.instance().getModState(this.selectedMod)), offset, 55, 16777215);
                this.drawCenteredString(this.textRenderer, "No mod information found", offset, 65, 14540253);
                this.drawCenteredString(this.textRenderer, "Ask your mod author to provide a mod mcmod.info file", offset, 75, 14540253);
            }

            GL11.glDisable(3042);
        }

        super.render(p_571_1_, p_571_2_, p_571_3_);
    }

    Minecraft getMinecraftInstance() {
        return this.field_1229;
    }

    TextRenderer getFontRenderer() {
        return this.textRenderer;
    }

    public void selectModIndex(int var1) {
        this.selected = var1;
        if (var1 >= 0 && var1 <= this.mods.size()) {
            this.selectedMod = (ModContainer)this.mods.get(this.selected);
        } else {
            this.selectedMod = null;
        }

    }

    public boolean modIndexSelected(int var1) {
        return var1 == this.selected;
    }
}
