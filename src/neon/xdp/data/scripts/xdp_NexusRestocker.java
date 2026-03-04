package neon.xdp.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import neon.xdp.data.plugins.XDPModPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class xdp_NexusRestocker implements EconomyTickListener {

    @Override
    public void reportEconomyTick(int iterIndex) {
        // Empty implementation
    }

    @Override
    public void reportEconomyMonthEnd() {
        // Only proceed if player has the skill
        if (!XDPModPlugin.hasParadeigmaSkill) return;

        List<SectorEntityToken> nexii = Global.getSector().getCustomEntitiesWithTag(Entities.DERELICT_MOTHERSHIP);

        // Filter out the entity with id "derelict_mothership"
        List<SectorEntityToken> filteredNexii = new ArrayList<>();
        for (SectorEntityToken entity : nexii) {
            if (!"derelict_mothership".equals(entity.getId())) {
                filteredNexii.add(entity);
            }
        }

        // Restock each filtered nexus
        for (SectorEntityToken entity : filteredNexii) {
            if (entity.getCargo() != null) {
                entity.getCargo().clear();
                CargoAPI cargo = addNexusCargo(entity);
                if (cargo != null) {
                    entity.getCargo().addAll(cargo);
                }
            }
        }
    }

    // add cargo to all nexus, undamaged ones get more stuff
    private CargoAPI addNexusCargo(SectorEntityToken nexus) {
        float mult = 1f;

        // Check if nexus is damaged - you can implement your own logic here
        // For example, check memory flags or health
        if (nexus.getMemoryWithoutUpdate() != null &&
                nexus.getMemoryWithoutUpdate().getBoolean("$damaged")) {
            mult = 0.5f;
        }

        CargoAPI cargo = Global.getFactory().createCargo(false);

        cargo.addCommodity(Commodities.GAMMA_CORE,
                Math.round(8 + Math.random() * 7) * (int)mult);
        cargo.addCommodity(Commodities.BETA_CORE,
                Math.round(5 + Math.random() * 4) * (int)mult);
        cargo.addCommodity(Commodities.ALPHA_CORE,
                Math.round(4 + Math.random() * 3) * (int)mult);
        cargo.addCommodity(Commodities.FUEL,
                Math.round(9000 + Math.random() * 6000) * (int)mult);
        cargo.addCommodity(Commodities.SUPPLIES,
                Math.round(3000 + Math.random() * 2000) * (int)mult);

        Random random = new Random();

        if (random.nextFloat() > 0.97f) {
            cargo.addCommodity(Commodities.OMEGA_CORE, 1f);
        }

        return cargo;
    }
}