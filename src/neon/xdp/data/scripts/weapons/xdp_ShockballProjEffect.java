package neon.xdp.data.scripts.weapons;

import com.fs.starfarer.api.graphics.SpriteAPI;
import neon.xdp.data.plugins.XDP_EnergyMissileEffectsPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.Iterator;

public class xdp_ShockballProjEffect extends XDP_EnergyMissileEffectsPlugin {
    public static enum EMPArcHitType {
        SOURCE,
        DEST,
        DEST_NO_TARGET,
    }

    protected IntervalUtil arcInterval = new IntervalUtil(0.17f, 0.23f);
    protected float delay = 1f;

    public static float ARC_RATE_MULT = 2f;

    public static int MAX_ARC_RANGE = 150;

    private SpriteAPI EMP_Radius = Global.getSettings().getSprite("fx", "shield_ring");

    public static String PROJ_SFX = "xdp_shockball_loop";

    public xdp_ShockballProjEffect(WeaponAPI weapon) {
        super(weapon);
        arcInterval = new IntervalUtil(0.17f, 0.23f);
        delay = 0.5f;

        //Set FX colors
        MAIN_COLOR = new Color(255, 240, 219,255); //SET CORE COLOR HERE
        FRINGE_COLOR = new Color(255,143,52,255); //SET FRINGE COLOR HERE
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        super.advance(amount);

        if (isWeaponCharging(weapon)) {

            float ringSize = (MAX_ARC_RANGE * 2.0f) * weapon.getChargeLevel();

            MagicRender.singleframe(EMP_Radius,
                    weapon.getLocation(),
                    new Vector2f(ringSize, ringSize),
                    weapon.getCurrAngle() - 90f,
                    Misc.scaleAlpha(FRINGE_COLOR, weapon.getChargeLevel()),
                    true,
                    CombatEngineLayers.ABOVE_SHIPS_LAYER);
        }

        if (proj != null && !isProjectileExpired(proj) && !proj.isFading()) {

            float ringSize = MAX_ARC_RANGE * 2.0f;

            MagicRender.singleframe(EMP_Radius,
                    proj.getLocation(),
                    new Vector2f(ringSize, ringSize),
                    proj.getFacing() - 90f,
                    FRINGE_COLOR,
                    true,
                    CombatEngineLayers.ABOVE_SHIPS_LAYER);

            delay -= amount;
            if (delay <= 0) {
                arcInterval.advance(amount * ARC_RATE_MULT);
                if (arcInterval.intervalElapsed()) {
                    spawnArc();
                    MissileAPI missile = (MissileAPI) proj;
                    CombatEngineAPI engine = Global.getCombatEngine();
                    if (findTarget(proj, weapon, engine) instanceof ShipAPI) {
                        missile.getEngineStats().getMaxSpeed().modifyMult("ShockballSlowdownOnTarget", 0.5f);
                    }
                    else {
                        missile.getEngineStats().getMaxSpeed().unmodifyMult("ShockballSlowdownOnTarget");
                    }
                }
            }
        }

        if (proj != null) {
            Global.getSoundPlayer().playLoop(PROJ_SFX, proj, 1f, 1f * proj.getBrightness(),
                    proj.getLocation(), proj.getVelocity());
        }
    }

    public void spawnArc() {
        CombatEngineAPI engine = Global.getCombatEngine();

        float emp = proj.getEmpAmount();
        float dam = proj.getDamageAmount();

        CombatEntityAPI target = findTarget(proj, weapon, engine);
        float thickness = 20f;
        float coreWidthMult = 0.67f;
        boolean randomArc = Math.random() < 0.33f;
        if (target != null) {
            EmpArcEntityAPI arc = engine.spawnEmpArc(proj.getSource(), proj.getLocation(), proj,
                    target,
                    DamageType.ENERGY,
                    dam,
                    emp, // emp
                    100000f, // max range
                    "realitydisruptor_emp_impact",
                    thickness, // thickness
                    FRINGE_COLOR,
                    MAIN_COLOR
            );
            arc.setCoreWidthOverride(thickness * coreWidthMult);

            if (target instanceof ShipAPI) {
                ShipAPI s = (ShipAPI) target;
            }

        }

        else if(randomArc) {
            Vector2f from = new Vector2f(proj.getLocation());
            Vector2f to = pickNoTargetDest(proj, weapon, engine);
            EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, proj, to, null, thickness, FRINGE_COLOR, MAIN_COLOR);
            arc.setCoreWidthOverride(thickness * coreWidthMult);
            Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, to, new Vector2f());

        }
    }

    public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float range = 200f;
        Vector2f from = projectile.getLocation();
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
        dir.scale(range);
        Vector2f.add(from, dir, dir);
        dir = Misc.getPointWithinRadius(dir, range * 0.25f);
        return dir;
    }

    public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float range = MAX_ARC_RANGE;
        Vector2f from = projectile.getLocation();

        Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
                range * 2f, range * 2f);
        int owner = weapon.getShip().getOwner();
        CombatEntityAPI best = null;
        float minScore = Float.MAX_VALUE;
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof MissileAPI) &&
                    //!(o instanceof CombatAsteroidAPI) &&
                    !(o instanceof ShipAPI)) continue;
            CombatEntityAPI other = (CombatEntityAPI) o;
            if (other.getOwner() == owner) continue;

            if (other instanceof ShipAPI) {
                ShipAPI otherShip = (ShipAPI) other;
                if (otherShip.isHulk()) continue;
                if (otherShip.isPhased()) continue;
                if (!otherShip.isTargetable()) continue;
            }
            if (other.getCollisionClass() == CollisionClass.NONE) continue;

            float radius = Misc.getTargetingRadius(from, other, false);
            float dist = Misc.getDistance(from, other.getLocation()) - radius - 50f;
            if (dist > range) continue;

            float score = dist;

            if (score < minScore) {
                minScore = score;
                best = other;
            }
        }
        return best;
    }
}
