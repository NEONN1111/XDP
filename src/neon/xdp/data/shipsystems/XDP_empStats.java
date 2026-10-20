package neon.xdp.data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import static neon.xdp.data.scripts.util.XDP_txt.txt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XDP_empStats extends BaseShipSystemScript {

    boolean init;

    List<WeaponSlotAPI> emitters = new ArrayList<>();

    //For main arcs targeting all targets in range when system fully active
    float EMP_RANGE = 1500f; //Max size of the emp bubble sprite
    float ARC_ACTIVE_DAMAGE = 100;
    float ARC_ACTIVE_EMP = 300F;

    private static final Color ARC_FRINGE_COLOR = new Color(255, 143, 52, 255);
    private static final Color ARC_GLOW_COLOR = new Color(253, 216, 182, 255);

    //interval between visual arcs calls
    private final IntervalUtil visualCD = new IntervalUtil(0.1f, 0.3f);
    //interval between damaging arcs calls
    private final IntervalUtil hitCD = new IntervalUtil(0.25f, 0.3f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float amount = Global.getCombatEngine().getElapsedInLastFrame();

        ShipAPI ship = (ShipAPI) stats.getEntity();
        //initiate emitter list
        if(!init){
            for (WeaponSlotAPI s : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if (s.isSystemSlot() && s.getSlotSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
                    emitters.add(s);
                }
            }
            init = true;
        }

        EmpArcEntityAPI.EmpArcParams empArcParams = new EmpArcEntityAPI.EmpArcParams();
        //empArcParams.segmentLengthMult = 4f;
        //empArcParams.zigZagReductionFactor = 0.25f;
        //empArcParams.fadeOutDist = 200f;
        //empArcParams.minFadeOutMult = 2f;

        empArcParams.segmentLengthMult = 8f;
        empArcParams.zigZagReductionFactor = 0.15f;

        empArcParams.brightSpotFullFraction = 0.5f;
        empArcParams.brightSpotFadeFraction = 0.5f;
        //empArcParams.nonBrrightSpotMinBrightness = 0.25f;

        // float dist = Misc.getDistance(slotLoc, target.getLocation());
        //  empArcParams.flickerRateMult = 0.6f - dist / 3000f;
        if (empArcParams.flickerRateMult < 0.3f) {
            empArcParams.flickerRateMult = 0.3f;
        }

        //Visual ring
        //Vex: made ring fade in and out based on effectLevel
        float ringSize = 150 + (2 * EMP_RANGE * effectLevel);
        SpriteAPI EMP_Radius = Global.getSettings().getSprite("fx", "xdp_emp_discharge_ring");
        MagicRender.singleframe(EMP_Radius,
                ship.getLocation(),
                new Vector2f(ringSize, ringSize),
                ship.getFacing() - 90f,
                new Color(255, 143, 52, (int) (45*Math.min(1, effectLevel*2))),
                true,
                CombatEngineLayers.ABOVE_SHIPS_LAYER);


        visualCD.advance(amount);

        if (visualCD.intervalElapsed()) {
            castRandom_EMP_Arc(ship, empArcParams, effectLevel);
        }

        if (state != State.ACTIVE) {
            return;
        }

        hitCD.advance(amount);

        if (!hitCD.intervalElapsed()) {
            return;
        }
        List<ShipAPI> shipsInRange = getTargetsInRange(ship, EMP_RANGE);
        for (ShipAPI targetShip : shipsInRange) {
            if (targetShip == null) {
                return;
            }

            //RNG number of acrs per ship adjusted per hullsize so they don't flood vision as much
            int arcRNGnumber = 1;
            if(targetShip.getHullSize().equals(ShipAPI.HullSize.FIGHTER)){
                arcRNGnumber = 1;
            } else if (targetShip.getHullSize().equals(ShipAPI.HullSize.FRIGATE)) {
                arcRNGnumber = new Random().nextInt(1, 2);
            } else if (targetShip.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
                arcRNGnumber = new Random().nextInt(1, 3);
            } else if (targetShip.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
                arcRNGnumber = new Random().nextInt(2, 3);
            } else if (targetShip.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
                arcRNGnumber = new Random().nextInt(2, 3);
            }

            float thicknessmult = 1f;
            if(targetShip.isHulk()){
                thicknessmult = 0.3f;
            }

            for (int i = 0; i < arcRNGnumber; i++) {
                float pierceChance = targetShip.getHardFluxLevel() - 0.1f;
                pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = (float) Math.random() < pierceChance;

                if (piercedShield) {
                    Global.getCombatEngine().spawnEmpArcPierceShields(
                            ship,
                            emitters.get(new Random().nextInt(emitters.size())).computePosition(ship),
                            null,
                            targetShip,
                            DamageType.ENERGY,
                            ARC_ACTIVE_DAMAGE, //damage
                            ARC_ACTIVE_EMP, // emp damage
                            999999f, // max range
                            "xdp_lightning_ball_cast",
                            40f * thicknessmult, // thickness
                            //new Color(100,165,255,255),
                            ARC_FRINGE_COLOR,
                            ARC_GLOW_COLOR,
                            empArcParams
                    );
                } else {
                    Global.getCombatEngine().spawnEmpArc(
                            ship,
                            emitters.get(new Random().nextInt(emitters.size())).computePosition(ship),
                            null,
                            targetShip,
                            DamageType.ENERGY,
                            ARC_ACTIVE_DAMAGE, //damage
                            ARC_ACTIVE_EMP, // emp damage
                            999999f, // max range
                            "xdp_lightning_ball_cast",
                            40f * thicknessmult, // thickness
                            ARC_FRINGE_COLOR,
                            ARC_GLOW_COLOR,
                            empArcParams
                    );
                }
            }

        }
    }

    //Custom method as CombatUtils.getShipsWithinRange bit inconsistent
    private List<ShipAPI> getTargetsInRange(ShipAPI originShip, float range){
        List<ShipAPI> AllShips = Global.getCombatEngine().getShips();
        List<ShipAPI> Targets = new ArrayList<>();

        for (ShipAPI s : AllShips){
            if(MathUtils.getDistance(originShip.getLocation(), s.getLocation()) < range - s.getCollisionRadius()/2f
                    && s != originShip
                    && s.getOwner() != originShip.getOwner()){
                Targets.add(s);
            }
        }

        return Targets;
    }


    //Creates cosmetic arcs from ship weapon bound to range (supposedly a bit under it)
    //Number of arcs dictated by `numberOfArcs` here time between them by `visualCD` before the call
    void castRandom_EMP_Arc(ShipAPI ship, EmpArcEntityAPI.EmpArcParams empArcParams, float effectLevel) {
        int numberOfArcs = new Random().nextInt(2, 5);

        EmpArcEntityAPI.EmpArcParams visualArcParams = empArcParams;

        visualArcParams.glowAlphaMult = 0.6f;
        visualArcParams.maxZigZagMult = 1.9f;
        //visualArcParams.brightSpotFullFraction = 0;
        //visualArcParams.brightSpotFadeFraction = 0;

        for (int i = 0; i < numberOfArcs; i++) {
            //float distance = (EMP_RANGE-150)*effectlevel;
            float distance = new Random().nextFloat((EMP_RANGE - 800) * effectLevel, (EMP_RANGE - 100) * effectLevel); //pick random distance
            Vector2f target = MathUtils.getRandomPointOnCircumference(ship.getLocation(), distance); //pick random point at that distance

            int randomEmitter = new Random().nextInt(0, emitters.size());

            //Arcs also fade in and out based on effectLevel
            Color DECO_FRINGE_COLOR = new Color(ARC_FRINGE_COLOR.getRed(), ARC_FRINGE_COLOR.getGreen(), ARC_FRINGE_COLOR.getBlue(), (int) (150*Math.min(1, effectLevel*2)));
            Color DECO_GLOW_COLOR = new Color(ARC_GLOW_COLOR.getRed(), ARC_GLOW_COLOR.getGreen(), ARC_GLOW_COLOR.getBlue(), (int) (150*Math.min(1, effectLevel*2)));;

            //Has bit of delay compared to range because they have timed life
            Global.getCombatEngine().spawnEmpArcVisual(
                    emitters.get(randomEmitter).computePosition(ship),
                    ship.getMutableStats().getEntity(),
                    target,
                    null,
                    10f, // thickness
                    //new Color(100,165,255,255),
                    DECO_FRINGE_COLOR,
                    DECO_GLOW_COLOR,
                    visualArcParams
            );
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("ACTIVE", false);
        }
        return null;
    }

    //Vex: Only really worth to use if you actually want to go all the way and do stuff using it everywhere.
    //Using strings.json is great for making localisation way simpler, beside that it kinda extra work
    //For example check, PAGSM and how strings.json looks there
    //private final String READY = txt("ready");
    //private final String OUTOFRANGE = txt("range");
    //private final String TARGET = txt("target");

    private String READY = "Ready";
    private String OUTOFRANGE = "No targets in range";
    private String TARGET = " targets in range";

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        List<ShipAPI> targets = getTargetsInRange(ship, EMP_RANGE);
        if (!targets.isEmpty()) {
            return targets.size() + TARGET;
        }

        if (targets.isEmpty()) {
            return OUTOFRANGE;
        }
        return READY;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        //Commented out for testing todo uncomment before release
        //List<ShipAPI> targets = getTargetsInRange(ship, EMP_RANGE);
        //return !targets.isEmpty();
        return true;
    }
}