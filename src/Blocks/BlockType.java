package Blocks;

import java.util.ArrayList;
import java.util.List;

public enum BlockType {
		
		GRASS(0, 2, 1, 1, 1, false, false),
		DIRT(2, 2, 2, 2, 2, false, false),
		STONE(3, 3, 3, 3, 3, false, false),
		PLANKS(4, 4, 4, 4, 4, false, false),
		SEMIDARK_PLANKS(10, 10, 10, 10, 10, false, false),
		DARK_PLANKS(9, 9, 9, 9, 9, false, false),
		//FURNACE(3, 3, 5, 3, 3, true),
		COAL(6, 6, 6, 6, 6, false, false),
		GOLD(7, 7, 7, 7, 7, false, false),
		DIAMOND(8, 8, 8, 8, 8, false, false),
		GOLD_BLOCK(11, 11, 11, 11, 11, false, false),
		ICE(12, 12, 12, 12, 12, false, false),
		DIAMOND_BLOCK(13, 13, 13, 13, 13, false, false),
		GLASS(14, 14, 14, 14, 14, false, true);
		
		
		private static List<BlockType> values = new ArrayList<BlockType>();
		
		public int textureSides, textureTop, textureBottom, textureFront, textureBack;
		public boolean canUse, transparent;
		
		private BlockType(int textureTop, int textureBottom, int textureFront, int textureBack, int textureSides, boolean canUse, boolean transparent) {
			this.textureSides = textureSides;
			this.textureTop = textureTop;
			this.textureBottom = textureBottom;
			this.textureBack = textureBack;
			this.textureFront = textureFront;
			this.canUse = canUse;
			this.transparent = transparent;
		}
		
		static {
			
			for (BlockType type : BlockType.values()) {
				
				values.add(type);
			}
			
		}
		
		public static BlockType getByInt(int id) {
			return values.get(id);
		}
		
		
		
		public static int size() {
			return values.size();
		}
		
		
		
	}
