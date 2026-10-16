package neon.xdp.data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MoteControlScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import static neon.xdp.data.scripts.util.XDP_txt.txt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class XDP_empStats extends BaseShipSystemScript {

    private final float RANGE=1500, DMG=500, EMP=1200;
    private final String ACTIVE = txt("emp");
    private static final Color GLOW_COLOR = new Color(255,143,52,255);

    private static final float EMP_MAX_SIZE = 2000f; //Max size of the emp bubble sprite

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

       ShipAPI ship = (ShipAPI) stats.getEntity();


       EmpArcEntityAPI.EmpArcParams params = new EmpArcEntityAPI.EmpArcParams();
//			params.segmentLengthMult = 4f;
//			params.zigZagReductionFactor = 0.25f;
//			params.fadeOutDist = 200f;
//			params.minFadeOutMult = 2f;

        params.segmentLengthMult = 8f;
        params.zigZagReductionFactor = 0.15f;

        params.brightSpotFullFraction = 0.5f;
        params.brightSpotFadeFraction = 0.5f;
        //params.nonBrrightSpotMinBrightness = 0.25f;

      // float dist = Misc.getDistance(slotLoc, target.getLocation());
      //  params.flickerRateMult = 0.6f - dist / 3000f;
        if (params.flickerRateMult < 0.3f) {
            params.flickerRateMult = 0.3f;
        }


        float ringSize = 50 + (EMP_MAX_SIZE * effectLevel);
        SpriteAPI EMP_Radius = Global.getSettings().getSprite("fx", "shield_ring");
        MagicRender.singleframe(EMP_Radius,
                ship.getLocation(),
                new Vector2f(ringSize, ringSize),
                ship.getFacing() -90f,
                GLOW_COLOR,
                true,
                CombatEngineLayers.ABOVE_SHIPS_LAYER);

        if(effectLevel==1 && !stats.getArmorDamageTakenMult().getMultMods().containsKey(id)){

            stats.getArmorDamageTakenMult().modifyMult(id, 1.5f);

            ShipAPI source = (ShipAPI)stats.getEntity();
            List<ShipAPI> shipsinRange = CombatUtils.getShipsWithinRange(ship.getLocation(), 1500f);
            for(ShipAPI targetShip : shipsinRange) {
                ShipAPI target = targetShip;


            if(target==null){
                return;
            }

            List <WeaponSlotAPI> emitters = new ArrayList<>();
            for(WeaponSlotAPI s : source.getHullSpec().getAllWeaponSlotsCopy()){
                if(s.isSystemSlot()){
                    emitters.add(s);
                }
            }
                float emp = 0;
                float dam = 0;
            for (int i=0; i<emitters.size(); i++) {
                if (Math.random() < target.getHardFluxLevel()) {
                    Global.getCombatEngine().spawnEmpArcPierceShields(
                            source,
                            emitters.get(i).computePosition(source),
                            null,
                            target,
                            DamageType.ENERGY,
                            dam,
                            emp, // emp
                            100000f, // max range
                            "xdp_lightning_ball_cast",
                            40f, // thickness
                            //new Color(100,165,255,255),
                            GLOW_COLOR,
                            new Color(255, 143, 52, 200),
                            params


                    );
                } else {
                    Global.getCombatEngine().spawnEmpArc(
                            source,
                            emitters.get(i).computePosition(source),
                            null,
                            target,
                            DamageType.ENERGY,
                            dam,
                            emp, // emp
                            100000f, // max range
                            "xdp_lightning_ball_cast",
                            40f, // thickness
                            //new Color(100,165,255,255),
                            GLOW_COLOR,
                            new Color(255, 143, 52, 200),
                            params
                    );
                }
            }

            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(ACTIVE, false);
        }
        return null;
    }

    private final String READY = txt("ready");
    private final String OUTOFRANGE = txt("range");
    private final String TARGET = txt("target");
    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return READY;
        }

        if ((target == null || target == ship) && ship.getShipTarget() != null) {
            return OUTOFRANGE;
        }
        return TARGET;
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        if(
                target!=null
                &&
                (!target.isDrone()||!target.isFighter())
                &&
                MathUtils.isWithinRange(ship, target, RANGE)
                &&
                Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), target.getLocation())))<22.5
                ){
            return target;
        } else {
            return null;
        }
    }


     public void advance(MutableShipStatsAPI stats, float amount,  float effectLevel) {
         ShipAPI ship = (ShipAPI) stats.getEntity();


 }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return true;
    }
}