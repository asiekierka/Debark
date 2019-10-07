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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import pl.asie.debark.util.BlockStateIterator;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(modid = DebarkMod.MODID, version = DebarkMod.VERSION,
        dependencies = "after:forge@[14.23.5.2838,)",
        updateJSON = "http://asie.pl/files/minecraft/update/" + DebarkMod.MODID + ".json")
public final class DebarkMod {
    public static final String MODID = "debark";
    public static final String VERSION = "${version}";
    private static Configuration config;

    @SidedProxy(modId = DebarkMod.MODID, clientSide = "pl.asie.debark.DebarkProxyClient", serverSide = "pl.asie.debark.DebarkProxyCommon")
    private static DebarkProxyCommon proxy;

    private static final Map<String, DebarkBlockEntry> entries = new LinkedHashMap<>();
    static final Map<IBlockState, BlockDebarkedLogEntry> blocksMap = new LinkedHashMap<>();

    private static boolean debarkByRecipe, debarkInWorld;

    private void add(String key) {
        String[] keySplit = key.split(",");
        String[] keySplitArgs = new String[keySplit.length - 1];
        System.arraycopy(keySplit, 1, keySplitArgs, 0, keySplitArgs.length);
        entries.put(keySplit[0], new DebarkBlockEntry(keySplit[0], keySplitArgs));
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());

        add("minecraft:log,variant");
        add("minecraft:log2,variant");
        for (int i = 0; i <= 6; i++) {
            add("forestry:logs." + i + ",variant");
            add("forestry:logs.fireproof." + i + ",variant");
        }
        for (int i = 0; i <= 9; i++) {
            add("extratrees:logs." + i + ",variant");
            add("extratrees:logs.fireproof." + i + ",variant");
        }
        for (int i = 0; i <= 4; i++) {
            add("biomesoplenty:log_" + i + ",variant");
        }
        add("aether_legacy:aether_log,aether_logs");
        add("rustic:log,variant");
        add("natura:overworld_logs,type");
        add("natura:overworld_logs2,type");
        add("natura:nether_logs,type");

        String[] edl = config.get("modsupport", "extraDebarkedLogs", new String[0], "Format: blockId,property1,property2,etc").getStringList();
        Stream.of(edl).forEach(this::add);

        debarkByRecipe = config.getBoolean("debarkByRecipe", "interactions", true, "Allow debarking in crafting tables.");
        debarkInWorld = config.getBoolean("debarkInWorld", "interactions", true, "Allow debarking by right-clicking blocks with an axe.");

        MinecraftForge.EVENT_BUS.register(this);
        proxy.preInit();

        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        for (BlockDebarkedLogEntry block : blocksMap.values()) {
            OreDictionary.registerOre("debarkedLogWood", new ItemStack(block.getBlock(), 1, block.getVariant()));
        }
    }

    public static IBlockState debarkedLogFor(IBlockState state) {
        IBlockState stateKey = BlockStateIterator.permuteByNames(state, ImmutableList.of("axis")).filter(blocksMap::containsKey).findFirst().orElse(null);
        if (stateKey != null) {
            BlockDebarkedLogEntry targetBlock = blocksMap.get(stateKey);
            String axisValue = null;
            for (IProperty property : stateKey.getPropertyKeys()) {
                if ("axis".equals(property.getName())) {
                    axisValue = property.getName(stateKey.getValue(property));
                    break;
                }
            }
            if (axisValue != null) {
                try {
                    BlockLog.EnumAxis enumAxis = BlockLog.EnumAxis.valueOf(axisValue.toUpperCase(Locale.ROOT));
                    IBlockState resultState = targetBlock.getBlockState().withProperty(BlockLog.LOG_AXIS, enumAxis);
                    return resultState;
                } catch (IllegalArgumentException e) {
                    // it fine
                }
            }
        }

        return null;
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (debarkInWorld) {
            if (!stack.isEmpty() && stack.getItem().getToolClasses(stack).contains("axe")) {
                // we have an axe
                IBlockState state = event.getWorld().getBlockState(event.getPos());
                IBlockState debarkedState = debarkedLogFor(state);
                if (debarkedState != null) {
                    event.getWorld().setBlockState(event.getPos(), debarkedState, 3);
                    stack.onBlockDestroyed(event.getWorld(), state, event.getPos(), event.getEntityPlayer());
                    event.getEntityPlayer().swingArm(event.getHand());
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blocksMap.clear();
        for (DebarkBlockEntry entry : entries.values()) {
            if (!event.getRegistry().containsKey(entry.getBlock())) {
                continue;
            }

            Block block = event.getRegistry().getValue(entry.getBlock());
            if (block == null || block.getRegistryName() == null) {
                continue;
            }

            IBlockState defaultState = block.getDefaultState();
            List<IBlockState> states = BlockStateIterator.permuteByNames(defaultState, entry.getProperties()).collect(Collectors.toList());
            if (states.size() > 4) {
                // TODO: don't know what to do with this...
                continue;
            }

            BlockDebarkedLog debarkedBlock = new BlockDebarkedLog(states.toArray(new IBlockState[0]));
            for (int i = 0; i < states.size(); i++) {
                blocksMap.put(states.get(i), new BlockDebarkedLogEntry(debarkedBlock, i));
            }
            debarkedBlock.setRegistryName("debark:debarked_log_" + block.getRegistryName().toString().replaceAll("[^A-Za-z0-9]", "_"));
            event.getRegistry().register(debarkedBlock);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        Set<Block> addedBlocks = new HashSet<>();

        blocksMap.values().forEach(blockEntry -> {
            if (!addedBlocks.add(blockEntry.getBlock())) {
                return;
            }

            ItemBlock itemBlock = new ItemBlock(blockEntry.getBlock()) {
                @Override
                public String getItemStackDisplayName(ItemStack stack) {
                    return ((BlockDebarkedLog) block).getLocalizedName(stack.getMetadata());
                }
            };

            itemBlock.setHasSubtypes(true);
            itemBlock.setRegistryName(blockEntry.getBlock().getRegistryName());
            event.getRegistry().register(itemBlock);
        });
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (debarkByRecipe) {
            blocksMap.values().forEach(blockEntry -> {
                BlockDebarkedLog block = blockEntry.getBlock();
                int i = blockEntry.getVariant();
                ItemStack log = block.getParentStack(i);
                ItemStack debarkedLog = new ItemStack(block, 1, i);
                DebarkShapelessRecipe recipe = new DebarkShapelessRecipe(log, debarkedLog);
                recipe.setRegistryName(new ResourceLocation("debark", "debark_log_" + block.getRegistryName().getPath()));
                event.getRegistry().register(recipe);
            });
        }
    }
}
