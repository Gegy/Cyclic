package com.lothrazar.cyclicmagic.registry;

import java.util.ArrayList;
import com.lothrazar.cyclicmagic.ModMain;
import com.lothrazar.cyclicmagic.block.BlockBucketStorage;
import com.lothrazar.cyclicmagic.block.BlockDimensionOre;
import com.lothrazar.cyclicmagic.block.BlockDimensionOre.SpawnType;
import com.lothrazar.cyclicmagic.block.BlockScaffolding;
import com.lothrazar.cyclicmagic.block.BlockUncrafting;
import com.lothrazar.cyclicmagic.item.itemblock.ItemBlockBucket;
import com.lothrazar.cyclicmagic.item.itemblock.ItemBlockScaffolding;
import com.lothrazar.cyclicmagic.util.Const;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockRegistry {

	public static ArrayList<Block>		blocks							= new ArrayList<Block>();

	public static BlockScaffolding		block_fragile;
	static BlockUncrafting uncrafting_block;
	public static BlockBucketStorage	block_storelava;
	public static BlockBucketStorage	block_storewater;
	public static BlockBucketStorage	block_storemilk;
	public static BlockBucketStorage	block_storeempty;

	public static BlockDimensionOre			nether_gold_ore;
	public static BlockDimensionOre			nether_coal_ore;
	public static BlockDimensionOre			nether_lapis_ore;
	public static BlockDimensionOre			nether_emerald_ore; 
	public static BlockDimensionOre			end_redstone_ore;
	public static BlockDimensionOre			end_coal_ore;
	public static BlockDimensionOre			end_lapis_ore;
	public static BlockDimensionOre			end_emerald_ore;

	private static boolean						enabledBucketBlocks; 
	private static boolean						enableBlockFragile;


	private static boolean spawnersUnbreakable;
//lots of helpers/overrides with defaults
	private static void registerBlock(Block b, String name) {
		registerBlock(b,name,false);
	}
	private static void registerBlock(Block b, String name, boolean isHidden) {

		registerBlock(b,new ItemBlock(b),name,isHidden);
	}
	private static void registerBlock(Block b, ItemBlock ib, String name) {

		registerBlock(b,ib,name,false);
	}
	private static void registerBlock(Block b, ItemBlock ib, String name,boolean isHidden) {
		b.setRegistryName(name);
		b.setUnlocalizedName(name); 

		GameRegistry.register(b);

		ib.setRegistryName(b.getRegistryName());
		GameRegistry.register(ib);

		if (isHidden == false) {
			b.setCreativeTab(ModMain.TAB);
		}

		blocks.add(b);
	}

	public static void register() {
		
		if(spawnersUnbreakable){
			Blocks.mob_spawner.setBlockUnbreakable();
		}
		//??maybe? nah.
		//Blocks.obsidian.setHardness(Blocks.obsidian.getHarvestLevel(Blocks.obsidian.getDefaultState()) / 2);
		
		if(BlockUncrafting.enableBlockUncrafting){
			registerBlock(uncrafting_block, "uncrafting_block");
		}

		if (enableBlockFragile) { 
			registerBlock(block_fragile,new ItemBlockScaffolding(block_fragile), BlockScaffolding.name);
		}

		if (WorldGenRegistry.netherOreEnabled) {

			nether_gold_ore = new BlockDimensionOre(Items.gold_nugget, 0, 4);
			nether_gold_ore.setSpawnType(SpawnType.SILVERFISH, 1);
			registerBlock(nether_gold_ore, "nether_gold_ore");

			nether_coal_ore = new BlockDimensionOre(Items.coal);
			nether_coal_ore.setSpawnType(SpawnType.SILVERFISH, 1);
			registerBlock(nether_coal_ore, "nether_coal_ore");

			nether_lapis_ore = new BlockDimensionOre(Items.dye, EnumDyeColor.BLUE.getDyeDamage(), 3);
			nether_lapis_ore.setSpawnType(SpawnType.SILVERFISH, 2);
			registerBlock(nether_lapis_ore, "nether_lapis_ore");

			nether_emerald_ore = new BlockDimensionOre(Items.emerald);
			nether_emerald_ore.setSpawnType(SpawnType.SILVERFISH, 5);
			registerBlock(nether_emerald_ore, "nether_emerald_ore");
		}

		if (WorldGenRegistry.endOreEnabled) {

			end_redstone_ore = new BlockDimensionOre(Items.redstone);
			end_redstone_ore.setSpawnType(SpawnType.ENDERMITE, 3);
			registerBlock(end_redstone_ore, "end_redstone_ore");

			end_coal_ore = new BlockDimensionOre(Items.coal);
			end_coal_ore.setSpawnType(SpawnType.ENDERMITE, 1);
			registerBlock(end_coal_ore, "end_coal_ore");

			end_lapis_ore = new BlockDimensionOre(Items.dye, EnumDyeColor.BLUE.getDyeDamage(), 3);
			end_lapis_ore.setSpawnType(SpawnType.ENDERMITE, 5);
			registerBlock(end_lapis_ore, "end_lapis_ore");

			end_emerald_ore = new BlockDimensionOre(Items.emerald);
			end_emerald_ore.setSpawnType(SpawnType.ENDERMITE, 8);
			registerBlock(end_emerald_ore, "end_emerald_ore");
		}

		if (enabledBucketBlocks) {
			block_storewater = new BlockBucketStorage(Items.water_bucket);
			registerBlock(block_storewater, new ItemBlockBucket(block_storewater), "block_storewater", true);

			block_storemilk = new BlockBucketStorage(Items.milk_bucket);
			registerBlock(block_storemilk, new ItemBlockBucket(block_storemilk), "block_storemilk", true);

			block_storelava = new BlockBucketStorage(Items.lava_bucket);
			registerBlock(block_storelava, new ItemBlockBucket(block_storelava), "block_storelava", true);

			block_storeempty = new BlockBucketStorage(null);
			registerBlock(block_storeempty, new ItemBlockBucket(block_storeempty), "block_storeempty", false);

			// not irecipe so just like this is fine i guess
			block_storeempty.addRecipe();
		}
	}

	public static void construct(){

		uncrafting_block = new BlockUncrafting();
		block_fragile = new BlockScaffolding();
	}
	
	public static void syncConfig(Configuration config) {

		String category = Const.ConfigCategory.blockChanges;

		spawnersUnbreakable = config.getBoolean("Spawners Unbreakable", category, true, "Make mob spawners unbreakable");
 
		category = Const.ConfigCategory.blocks;
 
		config.setCategoryComment(category, "Disable or customize blocks added to the game");

		enableBlockFragile = config.getBoolean("Scaffolding", category, true, "Enable the scaffolding block that breaks by itself");

		enabledBucketBlocks = config.getBoolean("Bucket Blocks", category, true, "Enable Bucket Storage Blocks");
 
		uncrafting_block.syncConfig(config);
	}
}
