package neon.xdp.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom production delegate for Explorarium Motherships
 * Allows players to produce Explorarium ships, weapons, and fighters
 */
public class xdp_NexusCustomProduction extends BaseCustomProductionPickerDelegateImpl {

    private InteractionDialogAPI dialog;

    public xdp_NexusCustomProduction(InteractionDialogAPI dialog) {
        this.dialog = dialog;
    }

    @Override
    public Set<String> getAvailableShipHulls() {
        Set<String> availableShips = new HashSet<>();

        // Get all ship hulls from the faction's known ships
        FactionAPI derelict = Global.getSector().getFaction(Factions.DERELICT);

        // Add all ships known to the Derelict faction
        for (String hullId : derelict.getKnownShips()) {
            if (!hullId.contains("station") && !hullId.contains("platform")) {
                availableShips.add(hullId);
            }
        }

        // Also add ships with AUTOMATED_RECOVERABLE tag that might not be in the faction's known list yet
        for (ShipHullSpecAPI hullSpec : Global.getSettings().getAllShipHullSpecs()) {
            String hullId = hullSpec.getHullId();
            if (hullSpec.hasTag(Tags.AUTOMATED_RECOVERABLE)) {
                if (!hullId.contains("station") && !hullId.contains("platform")) {
                    availableShips.add(hullId);
                }
            }
        }

        return availableShips;
    }

    @Override
    public Set<String> getAvailableWeapons() {
        Set<String> availableWeapons = new HashSet<>();

        // Get all weapons known to the DERELICT faction
        FactionAPI derelict = Global.getSector().getFaction(Factions.DERELICT);
        availableWeapons.addAll(derelict.getKnownWeapons());

        return availableWeapons;
    }

    @Override
    public Set<String> getAvailableFighters() {
        Set<String> availableFighters = new HashSet<>();

        // Get all fighters known to the DERELICT faction
        FactionAPI derelict = Global.getSector().getFaction(Factions.DERELICT);
        availableFighters.addAll(derelict.getKnownFighters());

        return availableFighters;
    }

    @Override
    public float getCostMult() {
        return 1f;
    }

    @Override
    public float getMaximumValue() {
        return Global.getSector().getPlayerFleet().getCargo().getCredits().get();
    }

    @Override
    public boolean withQuantityLimits() {
        return false;
    }

    @Override
    public void notifyProductionSelected(FactionProductionAPI production) {
        if (production == null) return;

        try {
            // Get the current production items using reflection
            Method getCurrentMethod = production.getClass().getMethod("getCurrent");
            Object currentObj = getCurrentMethod.invoke(production);

            CampaignFleetAPI playerFleet = null;
            if (currentObj instanceof List) {
                List<?> current = (List<?>) currentObj;
                playerFleet = Global.getSector().getPlayerFleet();
                CargoAPI playerCargo = playerFleet.getCargo();

                for (Object item : current) {
                    // Use reflection to get the methods we need
                    Class<?> itemClass = item.getClass();

                    Method getTypeMethod = itemClass.getMethod("getType");
                    Method getSpecIdMethod = itemClass.getMethod("getSpecId");
                    Method getQuantityMethod = itemClass.getMethod("getQuantity");

                    Object typeObj = getTypeMethod.invoke(item);
                    String type = typeObj != null ? typeObj.toString() : "";
                    String specId = (String) getSpecIdMethod.invoke(item);
                    int quantity = (Integer) getQuantityMethod.invoke(item);

                    // Check the type
                    if ("SHIP".equals(type)) {
                        for (int i = 0; i < quantity; i++) {
                            // Create the ship (add "_Hull" suffix as in original)
                            FleetMemberAPI member = Global.getFactory().createFleetMember(
                                    FleetMemberType.SHIP,
                                    specId + "_Hull"
                            );

                            // Set CR to maximum
                            member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

                            // Add AUTOMATED hullmod if not present
                            ShipVariantAPI variant = member.getVariant();
                            if (!variant.hasHullMod(HullMods.AUTOMATED)) {
                                variant.addPermaMod(HullMods.AUTOMATED, true);

                                // Handle modules
                                for (String slotId : variant.getStationModules().keySet()) {
                                    ShipVariantAPI moduleVariant = variant.getModuleVariant(slotId);
                                    ShipVariantAPI newModuleVariant = getRefitVariant(moduleVariant).clone();
                                    if (!newModuleVariant.hasHullMod(HullMods.AUTOMATED)) {
                                        newModuleVariant.addPermaMod(HullMods.AUTOMATED, true);
                                    }
                                    if (newModuleVariant != moduleVariant) {
                                        variant.setModuleVariant(slotId, newModuleVariant);
                                    }
                                }
                            }

                            // Fix the variant
                            fixVariant(member);

                            // Give the ship a random name from the Explorarium naming list
                            member.setShipName(Global.getSector().getFaction(Factions.DERELICT).pickRandomShipName());

                            // Add to fleet
                            playerFleet.getFleetData().addFleetMember(member);
                            AddRemoveCommodity.addFleetMemberGainText(member, dialog.getTextPanel());
                        }
                    }
                    // FIGHTER type
                    else if ("FIGHTER".equals(type)) {
                        playerCargo.addFighters(specId, quantity);
                        AddRemoveCommodity.addFighterGainText(specId, quantity, dialog.getTextPanel());
                    }
                    // WEAPON type
                    else if ("WEAPON".equals(type)) {
                        playerCargo.addWeapons(specId, quantity);
                        AddRemoveCommodity.addWeaponGainText(specId, quantity, dialog.getTextPanel());
                    }
                }
            }

            // Handle the cost
            Method getTotalCurrentCostMethod = production.getClass().getMethod("getTotalCurrentCost");
            int totalCost = (Integer) getTotalCurrentCostMethod.invoke(production);

            AddRemoveCommodity.addCreditsLossText(totalCost, dialog.getTextPanel());
            playerFleet.getCargo().getCredits().subtract(totalCost);

        } catch (Exception e) {
            // Log the error but don't crash
            System.err.println("Error processing production: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getWeaponColumnNameOverride() {
        return "Explorarium Arsenal";
    }

    @Override
    public String getNoMatchingBlueprintsLabelOverride() {
        return "No blueprints available. Deconstruct automated ships to add their blueprints to the Explorarium database.";
    }

    @Override
    public String getMaximumOrderValueLabelOverride() {
        return "Maximum Production Capacity";
    }

    @Override
    public String getCurrentOrderValueLabelOverride() {
        return "Current Production Order";
    }

    @Override
    public String getCustomOrderLabelOverride() {
        return "Explorarium Production";
    }

    @Override
    public String getNoProductionOrdersLabelOverride() {
        return "No production orders selected.";
    }

    @Override
    public String getItemGoesOverMaxValueStringOverride() {
        return "Insufficient production capacity.";
    }

    @Override
    public boolean isUseCreditSign() {
        return false;
    }

    @Override
    public int getCostOverride(Object item) {
        if (item instanceof WeaponSpecAPI) {
            WeaponSpecAPI weapon = (WeaponSpecAPI) item;
            if (weapon.hasTag(Tags.OMEGA)) {
                return Math.round(weapon.getBaseValue() * 3f);
            }
        }
        return -1;
    }

    /**
     * Gets a refit variant of the given ship variant
     */
    public static ShipVariantAPI getRefitVariant(ShipVariantAPI variant) {
        if (variant.isStockVariant() || variant.getSource() != VariantSource.REFIT) {
            ShipVariantAPI newVariant = variant.clone();
            newVariant.setOriginalVariant(null); // Prevents ship from reverting on game load
            newVariant.setSource(VariantSource.REFIT);
            return newVariant;
        }
        return variant;
    }

    /**
     * Fixes a fleet member's variant to ensure it's a refit variant
     */
    public static void fixVariant(FleetMemberAPI member) {
        ShipVariantAPI newVariant = getRefitVariant(member.getVariant());
        if (newVariant != member.getVariant()) {
            member.setVariant(newVariant, false, false);
        }
        fixModuleVariants(newVariant);
    }

    /**
     * Recursively fixes all module variants
     */
    public static void fixModuleVariants(ShipVariantAPI variant) {
        for (String slotId : variant.getStationModules().keySet()) {
            ShipVariantAPI moduleVariant = variant.getModuleVariant(slotId);
            ShipVariantAPI newModuleVariant = getRefitVariant(moduleVariant);
            if (newModuleVariant != moduleVariant) {
                variant.setModuleVariant(slotId, newModuleVariant);
            }
            fixModuleVariants(newModuleVariant);
        }
    }
}