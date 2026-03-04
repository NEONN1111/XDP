package neon.xdp.data.scripts.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import neon.xdp.data.plugins.XDPModPlugin;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Rule command for handling remnant defenders at Explorarium Motherships
 * Checks if player has the paradeigma skill before allowing interaction
 */
public class xdp_getRemnantDefenders extends BaseCommandPlugin {

    private static final Color EXPLORARIUM_COLOR = new Color(100, 250, 210, 250);

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        // Check if player has the paradeigma skill - if not, deny access
        if (!XDPModPlugin.hasParadeigmaSkill) {
            if (dialog != null && dialog.getTextPanel() != null) {
                dialog.getTextPanel().addPara("You lack the necessary Explorarium credentials to interface with this system.");
            }
            return false;
        }

        MemoryAPI memory = getEntityMemory(memoryMap);
        if (memory == null) return false;

        if (params == null || params.isEmpty()) return false;

        int type = params.get(0).getInt(memoryMap);

        switch (type) {
            case 1:
                // Check if defender fleet exists and is Derelict faction
                return checkDefenderFleet(memory);

            case 2:
                // Show defender fleet info and option to transmit IFF
                return showDefenderFleetInfo(dialog, memory);

            case 3:
                // Clear defenders after successful interaction
                return clearDefenders(dialog, memory);

            default:
                return false;
        }
    }

    /**
     * Case 1: Check if defender fleet exists and belongs to Derelict faction
     */
    private boolean checkDefenderFleet(MemoryAPI memory) {
        Object defenderObj = memory.get("$defenderFleet");
        if (defenderObj == null) {
            return false;
        }

        // The memory might store the fleet directly or as a string reference
        // This handles both possibilities
        if (defenderObj instanceof com.fs.starfarer.api.campaign.CampaignFleetAPI) {
            com.fs.starfarer.api.campaign.CampaignFleetAPI fleet = (com.fs.starfarer.api.campaign.CampaignFleetAPI) defenderObj;
            return Factions.DERELICT.equals(fleet.getFaction().getId());
        }

        return false;
    }

    /**
     * Case 2: Show defender fleet info and add transmit option
     */
    private boolean showDefenderFleetInfo(InteractionDialogAPI dialog, MemoryAPI memory) {
        if (dialog == null) return false;

        Object defenderObj = memory.get("$defenderFleet");
        if (defenderObj == null) return false;

        if (defenderObj instanceof com.fs.starfarer.api.campaign.CampaignFleetAPI) {
            com.fs.starfarer.api.campaign.CampaignFleetAPI fleet = (com.fs.starfarer.api.campaign.CampaignFleetAPI) defenderObj;

            // Show fleet info in visual panel
            dialog.getVisualPanel().showFleetInfo(
                    "Explorarium Automated Defenses",
                    fleet,
                    null,
                    null
            );

            // Add option to transmit IFF code
            dialog.getOptionPanel().addOption(
                    "Transmit your Explorarium IFF code",
                    "ds_yeetRemmies",
                    EXPLORARIUM_COLOR,
                    null
            );

            return true;
        }

        return false;
    }

    /**
     * Case 3: Clear defenders after successful transmission/defeat
     */
    private boolean clearDefenders(InteractionDialogAPI dialog, MemoryAPI memory) {
        if (dialog != null) {
            dialog.getVisualPanel().fadeVisualOut();
        }

        // Clear defender-related memory flags
        memory.unset("$hasDefenders");
        memory.unset("$defenderFleet");
        memory.set("$defenderFleetDefeated", true, 0);

        // Play success sound
        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND_TECHNOLOGY, 1f, 1f);

        // Add message if dialog exists
        if (dialog != null && dialog.getTextPanel() != null) {
            dialog.getTextPanel().addPara("Explorarium IFF code accepted. Automated defenses stand down.");
        }

        return true;
    }

    /**
     * Helper method to get entity memory safely
     */
    public static MemoryAPI getEntityMemory(Map<String, MemoryAPI> memoryMap) {
        if (memoryMap == null) return null;

        // Try to get from entity memory first, then local memory
        MemoryAPI memory = memoryMap.get("entity");
        if (memory == null) {
            memory = memoryMap.get("local");
        }
        return memory;
    }
}