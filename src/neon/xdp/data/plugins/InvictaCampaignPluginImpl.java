package neon.xdp.data.plugins;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin;

public class InvictaCampaignPluginImpl extends BaseCampaignPlugin {

    @Override
    public String getId() {
        return "xdp_CampaignPlugin";
    }

    @Override
    public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
        if ("xdp_invicta_core".equals(commodityId)) {
            return new PluginPick<AICoreOfficerPlugin>(new XDP_InvictaCore(), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
