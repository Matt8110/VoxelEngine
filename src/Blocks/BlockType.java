package Blocks;

import java.util.ArrayList;
import java.util.List;

public enum BlockType {
		
		GRASS(0, 2, 1, 1, 1),
		DIRT(2, 2, 2, 2, 2),
		STONE(3, 3, 3, 3, 3),
		PLANKS(4, 4, 4, 4, 4),
		FURNACE(3, 3, 5, 3, 3);
		
		private static List<BlockType> values = new ArrayList<BlockType>();
		
		public int textureSides, textureTop, textureBottom, textureFront, textureBack;
		
		private BlockType(int textureTop, int textureBottom, int textureFront, int textureBack, int textureSides) {
			this.textureSides = textureSides;
			this.textureTop = textureTop;
			this.textureBottom = textureBottom;
			this.textureBack = textureBack;
			this.textureFront = textureFront;
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
