package Blocks;

import java.util.ArrayList;
import java.util.List;

public enum BlockType {
		
		GRASS(0, 2, 1, 1, 1, false),
		DIRT(2, 2, 2, 2, 2, false),
		STONE(3, 3, 3, 3, 3, false),
		PLANKS(4, 4, 4, 4, 4, false),
		SEMIDARK_PLANKS(10, 10, 10, 10, 10, false),
		DARK_PLANKS(9, 9, 9, 9, 9, false),
		//FURNACE(3, 3, 5, 3, 3, true),
		COAL(6, 6, 6, 6, 6, false),
		GOLD(7, 7, 7, 7, 7, false),
		DIAMOND(8, 8, 8, 8, 8, false);
		
		private static List<BlockType> values = new ArrayList<BlockType>();
		
		public int textureSides, textureTop, textureBottom, textureFront, textureBack;
		public boolean canUse;
		
		private BlockType(int textureTop, int textureBottom, int textureFront, int textureBack, int textureSides, boolean canUse) {
			this.textureSides = textureSides;
			this.textureTop = textureTop;
			this.textureBottom = textureBottom;
			this.textureBack = textureBack;
			this.textureFront = textureFront;
			this.canUse = canUse;
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
