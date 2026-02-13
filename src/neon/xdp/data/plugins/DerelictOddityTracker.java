package neon.xdp.data.plugins;


import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;


public class DerelictOddityTracker extends BaseCampaignEventListener implements FleetInflationListener {


    public DerelictOddityTracker() {
        super(true);
    }

    public void reportFleetInflated(CampaignFleetAPI fleet, FleetInflater inflater) {
        // if (!fleet.getFaction().getId().equals(Factions.DERELICT)) return;
        for(FleetMemberAPI ship:fleet.getFleetData().getMembersListCopy()) {
           if (ship.getHullId().contains("xdp_superderelict")) {
               AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
               PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
               ship.setCaptain(person);
           }
            if (ship.getHullId().contains("xdp_derelominator")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_derelict_messenger")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_hermod")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("guardian")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_trinarch")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_luminance")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.BETA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_nemetor")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_recluse")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.BETA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_gleam")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.GAMMA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_parasite")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.GAMMA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
            if (ship.getHullId().contains("xdp_derelictpursuit")) {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
                PersonAPI person = plugin.createPerson(Commodities.GAMMA_CORE, fleet.getFaction().getId(), Misc.random);
                ship.setCaptain(person);
            }
        }
    }
}

