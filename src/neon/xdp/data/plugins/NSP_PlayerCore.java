//package neon.xdp_.data.plugins;
//
//import com.fs.starfarer.api.PluginPick;
//import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
//import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
//import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
//
//public class xdp_PlayerCore extends BaseCampaignPlugin {
//
//    @Override
//    public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
//        if ("ds_playercore".equals(commodityId)) {
//            return new PluginPick<AICoreOfficerPlugin>(new xdp_PlayerCorePlugin(), PickPriority.MOD_SET);
//        }
//        return null;
//    }
//}