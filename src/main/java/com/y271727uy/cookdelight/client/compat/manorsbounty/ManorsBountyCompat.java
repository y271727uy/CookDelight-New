package com.y271727uy.cookdelight.client.compat.manorsbounty;

import com.y271727uy.cookdelight.client.logic.KitchenTargetDefinition;
import com.y271727uy.cookdelight.config.CookDelightConfig;

import java.util.List;

public final class ManorsBountyCompat {
    private ManorsBountyCompat() {
    }

    public static List<KitchenTargetDefinition> definitions() {
        return List.of(
                target(List.of("manors_bounty_machine:fryer"), List.of("type:fast_fry", "type:slow_fry"), "gui.cookdelight.manors_fryer", new SlotBasedManorsMachineSnapshotReader(new int[]{1, 2, 3, 4}, new int[0])),
                target(List.of("manors_bounty_machine:saucepan_and_whisk"), List.of("type:saucepan_and_whisk"), "gui.cookdelight.manors_saucepan_and_whisk", new SlotBasedManorsMachineSnapshotReader(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{0})),
                target(List.of("manors_bounty_machine:teapot"), List.of("type:teapot"), "gui.cookdelight.manors_teapot", new SlotBasedManorsMachineSnapshotReader(new int[]{1, 2, 3, 4, 5, 6}, new int[]{0})),
                target(List.of("manors_bounty_machine:fermenter"), List.of("type:dim_fermentation", "type:normal_fermentation", "type:bright_fermentation"), "gui.cookdelight.manors_fermenter", new SlotBasedManorsMachineSnapshotReader(new int[]{2, 3, 4, 5, 6}, new int[]{7})),
                target(List.of("manors_bounty_machine:blender"), List.of("type:blender"), "gui.cookdelight.manors_blender", new SlotBasedManorsMachineSnapshotReader(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{12})),
                target(List.of("manors_bounty_machine:stock_pot"), List.of("type:stock_pot"), "gui.cookdelight.manors_stock_pot", new SlotBasedManorsMachineSnapshotReader(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, new int[]{0})),
                target(List.of(
                        "manors_bounty_machine:ice_cream_machine",
                        "manors_bounty_machine:icecream_machine",
                        "manors_bounty_machine:ice_cream_maker",
                        "manors_bounty_machine:icecream_maker",
                        "manors_bounty_machine:ice_cream"
                ), List.of("type:ice_cream"), "gui.cookdelight.manors_ice_cream_machine", new SlotBasedManorsMachineSnapshotReader(new int[]{2, 3}, new int[]{1})),
                target(List.of(
                        "manors_bounty_machine:oven",
                        "manors_bounty_machine:cooking_oven",
                        "manors_bounty_machine:baking_oven"
                ), List.of("type:oven"), "gui.cookdelight.manors_oven", new SlotBasedManorsMachineSnapshotReader(new int[]{0, 1, 2, 3, 4, 5}, new int[]{6}))
        );
    }

    private static KitchenTargetDefinition target(List<String> identifiers, List<String> recipePatterns, String titleKey, com.y271727uy.cookdelight.client.logic.KitchenMachineSnapshotReader snapshotReader) {
        return new KitchenTargetDefinition(
                identifiers,
                () -> recipePatterns,
                false,
                titleKey,
                false,
                0,
                true,
                snapshotReader,
                CookDelightConfig::enableManorsBountyMachineOverlay
        );
    }
}





