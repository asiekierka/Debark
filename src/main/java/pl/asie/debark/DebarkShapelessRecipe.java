/*
 * Copyright (c) 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Debark.
 *
 * Debark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Debark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Debark.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.debark;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import pl.asie.debark.util.ItemUtils;

public class DebarkShapelessRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final ItemStack log, debarkedLog;
    private final NonNullList<Ingredient> ingredients;

    public DebarkShapelessRecipe(ItemStack log, ItemStack debarkedLog) {
        this.log = log;
        this.debarkedLog = debarkedLog;
        this.ingredients = NonNullList.create();
        this.ingredients.add(Ingredient.fromStacks(this.log));
        // TODO: dynamic axe lookup
        this.ingredients.add(Ingredient.fromItems(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE));
    }

    private boolean isAxe(ItemStack stack) {
        return stack.getItem().getToolClasses(stack).contains("axe");
    }

    private boolean isLog(ItemStack stack) {
        return ItemUtils.canMerge(stack, log);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        int logs = 0;
        int axes = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (isLog(stack)) logs++;
                else if (isAxe(stack)) axes++;
                else return false;
            }
        }
        return logs == 1 && axes == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return debarkedLog.copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return (width * height) >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return debarkedLog.copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remainingItems = NonNullList.create();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (isAxe(stack)) {
                    ItemStack newStack = stack.copy();
                    newStack.setItemDamage(newStack.getItemDamage() + 1);
                    if (newStack.getItemDamage() > newStack.getMaxDamage()) {
                        remainingItems.add(ItemStack.EMPTY);
                    } else {
                        remainingItems.add(newStack);
                    }
                } else {
                    remainingItems.add(ItemStack.EMPTY);
                }
            } else {
                remainingItems.add(ItemStack.EMPTY);
            }
        }
        return remainingItems;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String getGroup() {
        return "debark:log_debarking";
    }
}
