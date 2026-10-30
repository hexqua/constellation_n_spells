package jp.aquafactory.constellationandspells.magic;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

public final class SchoolPowerSharing {
    // integrated server でも論理 side ごとに保持し、切断・設定変更が反対側へ漏れないようにする。
    private static volatile SchoolPowerSharingState serverState = SchoolPowerSharingState.INACTIVE;
    private static volatile SchoolPowerSharingState clientState = SchoolPowerSharingState.INACTIVE;

    private SchoolPowerSharing() {
    }

    public static SchoolPowerSharingState stateFor(LivingEntity entity) {
        return entity.level().isClientSide ? clientState : serverState;
    }

    public static SchoolPowerSharingState serverState() {
        return serverState;
    }

    public static SchoolPowerSharingState clientState() {
        return clientState;
    }

    public static void updateServer(SchoolPowerSharingRules rules) {
        serverState = resolve(rules);
    }

    public static void updateClient(SchoolPowerSharingRules rules) {
        clientState = resolve(rules);
    }

    public static void resetClient() {
        clientState = SchoolPowerSharingState.INACTIVE;
    }

    public static void resetServer() {
        serverState = SchoolPowerSharingState.INACTIVE;
    }

    private static SchoolPowerSharingState resolve(SchoolPowerSharingRules rules) {
        var schools = new HashMap<ResourceLocation, SchoolType>();
        SchoolRegistry.REGISTRY.keySet().forEach(id -> schools.put(id, SchoolRegistry.REGISTRY.get(id)));
        return new SchoolPowerSharingState(rules, rules.resolveTargets(schools));
    }
}
