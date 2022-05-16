package cpw.mods.fml.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.FatalErrorScreen;

@Environment(value= EnvType.CLIENT)
public class GuiCustomModLoadingErrorScreen extends FatalErrorScreen {
    private CustomModLoadingErrorDisplayException customException;

    public GuiCustomModLoadingErrorScreen(CustomModLoadingErrorDisplayException customException) {
        this.customException = customException;
    }

    public void init() {
        super.init();
        this.customException.initGui(this, this.textRenderer);
    }

    public void render(int par1, int par2, float par3) {
        this.renderBackground();
        this.customException.drawScreen(this, this.textRenderer, par1, par2, par3);
    }
}
