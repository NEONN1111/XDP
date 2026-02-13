package neon.xdp.data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import neon.xdp.data.shipsystems.xdp_PlasmaJetsSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;

public class XDP_PlasmaJetsHullmod extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        if (ship != null) {
            MagicSubsystemsManager.addSubsystemToShip(ship, new xdp_PlasmaJetsSubsystem(ship));
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Can only be prebuilt into specific hulls.";
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}