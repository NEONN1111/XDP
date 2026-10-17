package neon.xdp.data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class xdp_ShockballEffect implements OnFireEffectPlugin, OnHitEffectPlugin, EveryFrameWeaponEffectPlugin {
    protected CombatEntityAPI shockballProjEntity;
    protected xdp_ShockballProjEffect chargeGlowPlugin;
    public xdp_ShockballEffect() {
    }

    //protected IntervalUtil interval = new IntervalUtil(0.1f, 0.2f);
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //interval.advance(amount);

        boolean charging = weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0;
        if (charging && shockballProjEntity == null) {
            chargeGlowPlugin = new xdp_ShockballProjEffect(weapon);
            shockballProjEntity = Global.getCombatEngine().addLayeredRenderingPlugin(chargeGlowPlugin);
        } else if (!charging && shockballProjEntity != null) {
            shockballProjEntity = null;
            chargeGlowPlugin = null;
        }
    }


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (chargeGlowPlugin != null) {
            chargeGlowPlugin.attachToProjectile(projectile);
            chargeGlowPlugin = null;
            shockballProjEntity = null;

            MissileAPI missile = (MissileAPI) projectile;
            missile.setMine(true);
            missile.setNoMineFFConcerns(true);
            missile.setMineExplosionRange(xdp_ShockballProjEffect.MAX_ARC_RANGE + 50f);
            missile.setMinePrimed(true);
            missile.setUntilMineExplosion(0f);
        }
    }
}
