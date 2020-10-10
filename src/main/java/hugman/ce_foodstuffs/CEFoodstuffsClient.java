package hugman.ce_foodstuffs;

import hugman.ce_foodstuffs.init.client.CEFColorsMaps;
import hugman.ce_foodstuffs.init.client.CEFScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CEFoodstuffsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CEFColorsMaps.registerColors();
		CEFScreens.init();
	}
}
