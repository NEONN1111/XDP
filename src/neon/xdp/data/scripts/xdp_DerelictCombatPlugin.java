package neon.xdp.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import neon.xdp.data.plugins.XDPModPlugin;

import java.util.List;

public class xdp_DerelictCombatPlugin extends BaseEveryFrameCombatPlugin {
    private final float max = 20f;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        // Only apply if player has the paradeigma skill
        if (!XDPModPlugin.hasParadeigmaSkill) {
            return;
        }

        var playerfm = engine.getFleetManager(0);
        if (playerfm == null) return;

        var playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        var pofficer = playerFleet.getFleetData().getOfficersCopy();
        if (pofficer == null || pofficer.isEmpty()) {
            return;
        }

        float bonus = 0f;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getCaptain() != null &&
                    !member.getCaptain().isDefault() &&
                    member.getCaptain().isAICore() &&
                    Misc.isUnremovable(member.getCaptain())) {
                bonus = Math.min(bonus + (member.getCaptain().getStats().getLevel() / 3f), max);
            }
        }

        if (bonus > 0) {
            playerfm.modifyPercentMax("playercores", bonus);
        }

        super.advance(amount, events);
    }
}