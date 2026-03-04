package neon.xdp.data.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import neon.xdp.data.plugins.XDPModPlugin;

import java.awt.Color;
import java.util.Random;

/**
 * Plugin for handling Explorarium AI cores
 * Combined version that handles both player core and delicious core
 */
public class xdp_CorePlugin extends BaseAICoreOfficerPluginImpl implements AICoreOfficerPlugin {

    private static final String PLAYER_CORE_ID = "xdp_playercore";
    private static final String DELICIOUS_CORE_ID = "xdp_core_delicious";

    @Override
    public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {

        if (!XDPModPlugin.hasParadeigmaSkill) {
            return null;
        }

        switch (aiCoreId) {
            case DELICIOUS_CORE_ID:
                return makeTheGuy(factionId, random);
            case PLAYER_CORE_ID:
                return Global.getSector().getPlayerPerson();
            default:
                return null;
        }
    }

    private PersonAPI makeTheGuy(String factionId, Random random) {

        AICoreOfficerPlugin gammaPlugin = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE);
        PersonAPI person = gammaPlugin.createPerson(DELICIOUS_CORE_ID, factionId, random != null ? random : Misc.random);


        person.getStats().setLevel(1);
        person.getMemoryWithoutUpdate().set("$xdp_delicious", true);
        person.getMemoryWithoutUpdate().set("$chatterChar", "freeborn");
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2f);

        return person;
    }

    @Override
    public void createPersonalitySection(PersonAPI person, TooltipMakerAPI tooltip) {
        if (tooltip == null || person == null) return;

        float opad = 10f;
        Color text = person.getFaction().getBaseUIColor();
        Color bg = person.getFaction().getDarkUIColor();


        if (person == Global.getSector().getPlayerPerson()) {
            tooltip.addPara("A direct neural interface linking the pilot to the ship's systems.", 5f)
                    .setAlignment(Alignment.MID);
            tooltip.addPara("This represents your consciousness being directly integrated with the vessel, " +
                    "allowing for instinctive control and response.", 5f);
            tooltip.addPara("Functions identically to a standard AI core but draws from your own skills and experience.", 5f);
            return;
        }


        tooltip.addPara("A somewhat primitive artificial intelligence built into the ship.", 5f)
                .setAlignment(Alignment.MID);
        tooltip.addPara("While not quite as competent in combat as an AI core to start, they require no specialized maintenance and have a great learning capacity.", 5f);
        tooltip.addPara("May level up after combat and gain a random skill, up to level 6.", 5f)
                .setHighlight("level 6.");

        tooltip.addSectionHeading("Personality: " + Misc.getPersonalityName(person),
                text, bg, Alignment.MID, 20f);

        String personalityId = person.getPersonalityAPI().getId();
        switch (personalityId) {
            case Personalities.RECKLESS:
                tooltip.addPara("In combat, this AI is single-minded and determined. " +
                        "In a human captain, their traits might be considered reckless. " +
                        "In a machine, they're terrifying.", opad);
                break;

            case Personalities.AGGRESSIVE:
                tooltip.addPara("In combat, this AI will prefer to engage at a range that allows the use of " +
                        "all of their ship's weapons and will employ any fighters under their command aggressively.", opad);
                break;

            case Personalities.STEADY:
                tooltip.addPara("In combat, this AI will favor a balanced approach with " +
                        "tactics matching the current situation.", opad);
                break;

            case Personalities.CAUTIOUS:
                tooltip.addPara("In combat, this AI will prefer to stay out of enemy range, " +
                        "only occasionally moving in if out-ranged by the enemy.", opad);
                break;

            case Personalities.TIMID:
                tooltip.addPara("In combat, this AI will attempt to avoid direct engagements if at all " +
                        "possible, even if commanding a combat vessel.", opad);
                break;

            default:
                tooltip.addPara("In combat, this AI adapts to the situation with standard tactical protocols.", opad);
                break;
        }
    }
}