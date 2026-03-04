package neon.xdp.data.econ.items;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import neon.xdp.data.plugins.XDPModPlugin;

import java.awt.Color;

/**
 * Special item representing a packed ship that can be unpacked by the player
 * Only available when the player has the paradeigma skill
 */
public class xdp_packedShip extends BaseSpecialItemPlugin {

    private String memberID = null;
    private FleetMemberAPI member = null;

    private static final String STORAGE_ENTITY_ID = "xdp_nexusStorage";
    private static final Color PACKED_SHIP_COLOR = new Color(255, 192, 203); // Pink as in original

    @Override
    public void performRightClickAction() {
        // Check if player has the paradeigma skill
        if (!XDPModPlugin.hasParadeigmaSkill) {
            Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                    "You lack the necessary Explorarium credentials to unpack this ship.");
            return;
        }

        if (member == null) return;

        // Add the ship to player fleet
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
        Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                "Retrieved the " + member.getShipName() + ".");
    }

    @Override
    public void init(CargoStackAPI stack) {
        if (stack == null || stack.getSpecialDataIfSpecial() == null) return;

        memberID = stack.getSpecialDataIfSpecial().getData();

        // Find the packed ship in the storage
        SectorEntityToken storage = Global.getSector().getEntityById(STORAGE_ENTITY_ID);
        if (storage != null && storage.getMarket() != null) {
            SubmarketAPI submarket = storage.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE);
            if (submarket != null && submarket.getCargo() != null) {
                for (FleetMemberAPI storedMember : submarket.getCargo().getMothballedShips().getMembersListCopy()) {
                    if (storedMember.getId().equals(memberID)) {
                        member = storedMember;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded,
                              CargoTransferHandlerAPI transferHandler, Object stackSource) {
        if (tooltip == null) return;

        tooltip.addPara("A compacted Explorarium vessel stored in a portable format.", 5f);

        if (member == null) {
            tooltip.addPara("Ship data corrupted.", 5f).setColor(Color.RED);
            return;
        }

        tooltip.addTitle(member.getShipName()).setColor(PACKED_SHIP_COLOR);

        if (expanded) {

            String spriteName = member.getHullSpec().getSpriteName();
            if (spriteName != null && !spriteName.isEmpty()) {
                tooltip.addImage(spriteName, 128f, 5f);
            }


            tooltip.addPara("Class: " + member.getHullSpec().getHullNameWithDashClass(), 5f);
            tooltip.addPara("Size: " + member.getHullSpec().getHullSize().toString(), 5f);
            tooltip.addPara("FP: " + member.getFleetPointCost(), 5f);


            tooltip.addPara("Right-click to unpack this ship and add it to your fleet.", 10f);
            tooltip.addPara("This action will consume the item.", 2f).setColor(Misc.getHighlightColor());
        }
    }

    @Override
    public String getName() {
        if (member != null) {
            return "Packed " + member.getHullSpec().getNameWithDesignationWithDashClass();
        }
        return "Packed Explorarium Vessel";
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    @Override
    public boolean hasRightClickAction() {

        return XDPModPlugin.hasParadeigmaSkill && member != null;
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        if (member == null) return 0;
        return Math.round(member.getBaseValue());
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return true;
    }

    /**
     * Creates a packed ship special item from a fleet member
     * This would be called when a ship is packed/stored
     */
    public static CargoStackAPI createPackedShipItem(FleetMemberAPI member) {
        if (member == null) return null;

        SpecialItemData data = new SpecialItemData("xdp_packed_ship", member.getId());


        return Global.getFactory().createCargoStack(CargoAPI.CargoItemType.SPECIAL, data, (CargoAPI) null);
    }
}