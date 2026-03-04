package neon.xdp.data.plugins;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin;

public class xdp_CampaignPlugin extends BaseCampaignPlugin {

    private static final String PLAYER_CORE_ID = "xdp_playercore";
    private static final String DELICIOUS_CORE_ID = "xdp_core_delicious";

    @Override
    public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
        if (PLAYER_CORE_ID.equals(commodityId) || DELICIOUS_CORE_ID.equals(commodityId)) {
            return new PluginPick<AICoreOfficerPlugin>(
                    new xdp_CorePlugin(),
                    CampaignPlugin.PickPriority.MOD_SET
            );
        }
        return null;
    }

    @Override
    public String getId() {
        return "xdp_campaign_plugin";
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}