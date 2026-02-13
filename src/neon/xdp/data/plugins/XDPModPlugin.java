package neon.xdp.data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import neon.xdp.data.plugins.secgen.XDP_InvictaSystemGen;
import neon.xdp.data.scripts.*;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.apache.log4j.Logger;

import java.util.ArrayList;


public class XDPModPlugin extends BaseModPlugin {
    public static boolean hasMagicLib = false;
    public static final String XDP_LUMINANCE_MISSILE_ID = "xdp_luminancemissile";
    public Logger log = Logger.getLogger(this.getClass());
    public static boolean HAS_GRAPHICSLIB = false;

    @Override
    public void onGameLoad(boolean newGame) {
        // NSP initialization

        if (!Global.getSector().getListenerManager().hasListenerOfClass(DerelictOddityTracker.class)) {
            Global.getSector().getListenerManager().addListener(new DerelictOddityTracker(), true);
        }
        try {
            SectorAPI sector = Global.getSector();
            InvictaCampaignPluginImpl plugin = new InvictaCampaignPluginImpl();
            sector.registerPlugin(plugin);
        } catch (Throwable t) {
            log.error("Failed to register InvictaCampaignPluginImpl", t);
        }

        // FactionAPI remmy = Global.getSector().getFaction(Factions.DERELICT);
        // remmy.setShowInIntelTab(true);
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        // NSP content
        CustomFleetsXDP_Templar.spawnTemplar();
        CustomFleetsXDP_Gate.spawnNemetor();
        new XDP_People().xdp_createPeople();
    }

    @Override
    public void onNewGameAfterProcGen() {
        // NSP content
        SectorAPI sector = Global.getSector();
        // ThemeGenerator NSPThemeGenerator;
        ArrayList<String> systemBL = new ArrayList<>();
        ArrayList<String> tagBL = new ArrayList<>();
        tagBL.add(Tags.THEME_HIDDEN);
        tagBL.add(Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        tagBL.add(Tags.SYSTEM_ABYSSAL);
        tagBL.add(Tags.STAR_HIDDEN_ON_MAP);
        tagBL.add("theme_d"); // if people are still running DME, i guess?

        new XDP_gen().generate(Global.getSector());
        new XDP_InvictaSystemGen().generate(Global.getSector());
        //  new nsp_dump_system().generate(Global.getSector());
    }

    @Override
    public void onApplicationLoad() throws Exception {
        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasGraphicsLib) {
            HAS_GRAPHICSLIB = true;
            ShaderLib.init();
            // LightData.readLightDataCSV((String) "data/config/example_lights_data.csv");
            TextureData.readTextureDataCSV((String) "data/config/nsp_texture_data.csv");
            log.info("NSP shaders active");
        }
        hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");

        log.info("Welcome to NSP! I'm in your hulls...");
    }

    @Override
    public void onNewGame() {
        // SectorThemeGenerator.generators.add(1, new NSPThemeGenerator());
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case XDP_LUMINANCE_MISSILE_ID:
                return new PluginPick<MissileAIPlugin>(new xdp_luminancebay(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
        return null;
    }
}