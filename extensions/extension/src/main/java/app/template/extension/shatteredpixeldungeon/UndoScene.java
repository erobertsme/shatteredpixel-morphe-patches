package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.Visual;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * What an undo step looked like, copied from the game when the step is taken: the dungeon tiles around the hero, the creatures and items in view, a
 * simple map of the floor and the game log lines from that action. It is drawn again with the game's own tiles and sprites.
 */
final class UndoScene {

	static final int AREA = 9;
	private static final int TILE = 16;
	private static final String FEATURES_TEX = "environment/terrain_features.png";
	private static final int MAX_LINES = 4;
	private static final float SPRITE_RAISE = 6f / 16f;
	private static final int REMEMBERED_FOG = 0x99000000;
	private static final int UNKNOWN_FOG = 0xFF000000;

	private static final byte FOG_UNKNOWN = 0;
	private static final byte FOG_REMEMBERED = 1;
	private static final byte FOG_VISIBLE = 2;

	static final byte MAP_UNKNOWN = 0;
	static final byte MAP_WALL = 1;
	static final byte MAP_FLOOR = 2;
	static final byte MAP_WATER = 3;
	static final byte MAP_CHASM = 4;
	static final byte MAP_DOOR = 5;
	static final byte MAP_STAIRS = 6;
	private static final int[] MAP_COLORS = { 0, 0xFF4A443D, 0xFF8C826F, 0xFF3F6E9A, 0xFF16141A, 0xFF9A6A3A, 0xFFE0C050 };
	private static final int HERO_COLOR = 0xFFFFFFFF;
	private static final int ENEMY_COLOR = 0xFFE04040;
	private static final int ALLY_COLOR = 0xFF50D050;
	private static final int NEUTRAL_COLOR = 0xFFE0E040;
	private static final int ITEM_COLOR = 0xFFFFA020;
	private static final int OUTLINE_COLOR = 0xCCFFFFFF;

	private static final class Figure {
		final String spriteClass;
		final RectF frame;
		final boolean flipped;
		final int cell;

		Figure(String spriteClass, RectF frame, boolean flipped, int cell) {
			this.spriteClass = spriteClass;
			this.frame = frame;
			this.flipped = flipped;
			this.cell = cell;
		}
	}

	private static final class Marker {
		final int pos;
		final int color;

		Marker(int pos, int color) {
			this.pos = pos;
			this.color = color;
		}
	}

	private String tilesTex;
	private String waterTex;
	private int[] tiles;
	private int[] walls;
	private int[] features;
	private boolean[] water;
	private byte[] fog;
	private String heroClass;
	private int heroTier;
	private final List<Figure> figures = new ArrayList<>();
	private final List<Figure> heaps = new ArrayList<>();
	private int mapWidth;
	private int mapHeight;
	private byte[] map;
	private int heroPos;
	private int areaLeft;
	private int areaTop;
	private final List<Marker> markers = new ArrayList<>();
	final List<String> lines = new ArrayList<>();

	private static final List<String> pendingLines = new ArrayList<>();
	private static final Signal.Listener<String> logListener = text -> {
		if (text == null || text.equals(GLog.NEW_LINE) || text.trim().isEmpty()) return false;
		synchronized (pendingLines) {
			pendingLines.add(text);
			while (pendingLines.size() > MAX_LINES) pendingLines.remove(0);
		}
		return false;
	};

	private UndoScene() {
	}

	/**
	 * Starts listening to the game log. Called each time the game scene is made, after the game's own log has replaced the listeners.
	 */
	static void listen() {
		GLog.update.remove(logListener);
		GLog.update.add(logListener);
	}

	/**
	 * Forgets log lines that came from loading a step rather than from an action.
	 */
	static void discardLines() {
		synchronized (pendingLines) {
			pendingLines.clear();
		}
	}

	/**
	 * Copies what the game looks like now. Returns null if it can't be read.
	 */
	static UndoScene capture() {
		UndoScene scene = new UndoScene();
		synchronized (pendingLines) {
			scene.lines.addAll(pendingLines);
			pendingLines.clear();
		}
		try {
			Hero hero = Dungeon.hero;
			Level level = Dungeon.level;
			if (hero == null || level == null) return scene;
			scene.captureArea(hero, level);
			scene.captureMap(hero, level);
		} catch (Throwable e) {
			Log.e("UndoScene", "Could not copy the scene", e);
			scene.tiles = null;
			scene.map = null;
		}
		return scene;
	}

	private void captureArea(Hero hero, Level level) throws ReflectiveOperationException {
		int width = level.width();
		int height = level.height();
		areaLeft = hero.pos % width - AREA / 2;
		areaTop = hero.pos / width - AREA / 2;
		int[] levelTiles = tilemapData("tiles");
		int[] levelWalls = tilemapData("walls");
		int[] levelFeatures = tilemapData("terrainFeatures");
		tilesTex = level.tilesTex();
		waterTex = level.waterTex();
		tiles = new int[AREA * AREA];
		walls = new int[AREA * AREA];
		features = new int[AREA * AREA];
		water = new boolean[AREA * AREA];
		fog = new byte[AREA * AREA];
		for (int y = 0; y < AREA; y++) {
			for (int x = 0; x < AREA; x++) {
				int i = y * AREA + x;
				int cx = areaLeft + x;
				int cy = areaTop + y;
				tiles[i] = -1;
				walls[i] = -1;
				features[i] = -1;
				if (cx < 0 || cy < 0 || cx >= width || cy >= height) continue;
				int cell = cy * width + cx;
				if (levelTiles != null && cell < levelTiles.length) tiles[i] = levelTiles[cell];
				if (levelWalls != null && cell < levelWalls.length) walls[i] = levelWalls[cell];
				if (levelFeatures != null && cell < levelFeatures.length) features[i] = levelFeatures[cell];
				water[i] = level.water[cell];
				fog[i] = level.heroFOV[cell] ? FOG_VISIBLE : level.visited[cell] || level.mapped[cell] ? FOG_REMEMBERED : FOG_UNKNOWN;
			}
		}
		heroClass = hero.heroClass.name();
		heroTier = hero.tier();
		for (Mob mob : level.mobs.toArray(new Mob[0])) {
			int i = areaIndex(mob.pos, width);
			if (i < 0 || fog[i] != FOG_VISIBLE || mob.sprite == null || !mob.sprite.visible || mob.invisible > 0) continue;
			RectF frame = mob.sprite.frame();
			if (frame == null) continue;
			figures.add(new Figure(mob.sprite.getClass().getName(), new RectF(frame.left, frame.top, frame.right, frame.bottom), mob.sprite.flipHorizontal, i));
		}
		for (Heap heap : level.heaps.valueList()) {
			int i = areaIndex(heap.pos, width);
			if (i < 0 || fog[i] == FOG_UNKNOWN || !heap.seen || heap.sprite == null) continue;
			RectF frame = heap.sprite.frame();
			if (frame == null) continue;
			heaps.add(new Figure(null, new RectF(frame.left, frame.top, frame.right, frame.bottom), false, i));
		}
	}

	private int areaIndex(int pos, int width) {
		int x = pos % width - areaLeft;
		int y = pos / width - areaTop;
		if (x < 0 || y < 0 || x >= AREA || y >= AREA) return -1;
		return y * AREA + x;
	}

	private static int[] tilemapData(String name) throws ReflectiveOperationException {
		Field sceneField = GameScene.class.getDeclaredField("scene");
		sceneField.setAccessible(true);
		Object scene = sceneField.get(null);
		if (scene == null) return null;
		Field layerField = GameScene.class.getDeclaredField(name);
		layerField.setAccessible(true);
		Object layer = layerField.get(scene);
		if (layer == null) return null;
		Field dataField = Tilemap.class.getDeclaredField("data");
		dataField.setAccessible(true);
		int[] data = (int[]) dataField.get(layer);
		return data == null ? null : data.clone();
	}

	private void captureMap(Hero hero, Level level) {
		mapWidth = level.width();
		mapHeight = level.height();
		heroPos = hero.pos;
		int length = level.length();
		map = new byte[length];
		for (int i = 0; i < length; i++) {
			if (!level.visited[i] && !level.mapped[i]) continue;
			int terrain = level.map[i];
			if (terrain == Terrain.ENTRANCE || terrain == Terrain.ENTRANCE_SP || terrain == Terrain.EXIT || terrain == Terrain.UNLOCKED_EXIT) {
				map[i] = MAP_STAIRS;
			} else if (terrain == Terrain.DOOR || terrain == Terrain.OPEN_DOOR || terrain == Terrain.LOCKED_DOOR || terrain == Terrain.CRYSTAL_DOOR) {
				map[i] = MAP_DOOR;
			} else if (level.solid[i]) {
				map[i] = MAP_WALL;
			} else if (level.pit[i]) {
				map[i] = MAP_CHASM;
			} else if (level.water[i]) {
				map[i] = MAP_WATER;
			} else {
				map[i] = MAP_FLOOR;
			}
		}
		for (Heap heap : level.heaps.valueList()) {
			if (heap.seen && map[heap.pos] != MAP_UNKNOWN) markers.add(new Marker(heap.pos, ITEM_COLOR));
		}
		for (Mob mob : level.mobs.toArray(new Mob[0])) {
			if (!level.heroFOV[mob.pos] || mob.invisible > 0) continue;
			int color = mob.alignment == Char.Alignment.ALLY ? ALLY_COLOR : mob.alignment == Char.Alignment.ENEMY ? ENEMY_COLOR : NEUTRAL_COLOR;
			markers.add(new Marker(mob.pos, color));
		}
		markers.add(new Marker(hero.pos, HERO_COLOR));
	}

	boolean hasArea() {
		return tiles != null;
	}

	boolean hasMap() {
		return map != null;
	}

	/**
	 * The first log line, without its colour mark.
	 */
	String firstLine() {
		return lines.isEmpty() ? null : plain(lines.get(0));
	}

	static String plain(String line) {
		for (String prefix : new String[] { GLog.POSITIVE, GLog.NEGATIVE, GLog.WARNING, GLog.HIGHLIGHT }) {
			if (line.startsWith(prefix)) return line.substring(prefix.length());
		}
		return line;
	}

	static int lineColor(String line) {
		if (line.startsWith(GLog.POSITIVE)) return 0x44FF44;
		if (line.startsWith(GLog.NEGATIVE)) return 0xFF4444;
		if (line.startsWith(GLog.WARNING)) return 0xFF8800;
		if (line.startsWith(GLog.HIGHLIGHT)) return 0xFFFF44;
		return 0xFFFFFF;
	}

	/**
	 * Draws the area around the hero into [out], [size] wide, with its top left corner at [x], [y].
	 */
	void drawArea(List<Gizmo> out, Camera camera, float x, float y, float size) {
		float scale = size / (AREA * TILE);
		if (waterTex != null) {
			SkinnedBlock waterBlock = new SkinnedBlock(AREA * TILE, AREA * TILE, waterTex);
			place(out, waterBlock, camera, x, y, scale);
		}
		Tilemap ground = new Tilemap(tilesTex, new TextureFilm(tilesTex, TILE, TILE));
		ground.map(tiles.clone(), AREA);
		place(out, ground, camera, x, y, scale);
		Tilemap grass = new Tilemap(FEATURES_TEX, new TextureFilm(FEATURES_TEX, TILE, TILE));
		grass.map(features.clone(), AREA);
		place(out, grass, camera, x, y, scale);
		for (Figure heap : heaps) {
			try {
				Image image = new Image(new ItemSprite(0));
				image.frame(heap.frame);
				float px = (heap.cell % AREA) * TILE + (TILE - image.width()) / 2f;
				float py = (heap.cell / AREA) * TILE + (TILE - image.height()) / 2f;
				place(out, image, camera, x + px * scale, y + py * scale, scale);
			} catch (Throwable e) {
				Log.e("UndoScene", "Could not draw an item", e);
			}
		}
		for (Figure figure : figures) {
			Image image = figureImage(figure);
			if (image != null) placeFigure(out, image, camera, figure.cell, x, y, scale);
		}
		Image hero = heroImage();
		if (hero != null) placeFigure(out, hero, camera, (AREA / 2) * AREA + AREA / 2, x, y, scale);
		Tilemap raised = new Tilemap(tilesTex, new TextureFilm(tilesTex, TILE, TILE));
		raised.map(walls.clone(), AREA);
		place(out, raised, camera, x, y, scale);
		for (int i = 0; i < fog.length; i++) {
			if (fog[i] == FOG_VISIBLE) continue;
			ColorBlock shade = new ColorBlock(TILE, TILE, fog[i] == FOG_UNKNOWN ? UNKNOWN_FOG : REMEMBERED_FOG);
			place(out, shade, camera, x + (i % AREA) * TILE * scale, y + (i / AREA) * TILE * scale, scale);
		}
	}

	private Image figureImage(Figure figure) {
		try {
			Image sprite = (Image) Class.forName(figure.spriteClass).getConstructor().newInstance();
			Image image = new Image(sprite);
			image.frame(figure.frame);
			image.flipHorizontal = figure.flipped;
			return image;
		} catch (Throwable e) {
			Log.e("UndoScene", "Could not draw " + figure.spriteClass, e);
			return null;
		}
	}

	private Image heroImage() {
		try {
			return HeroSprite.avatar(HeroClass.valueOf(heroClass), heroTier);
		} catch (Throwable e) {
			Log.e("UndoScene", "Could not draw the hero", e);
			return null;
		}
	}

	private void placeFigure(List<Gizmo> out, Image image, Camera camera, int cell, float x, float y, float scale) {
		float px = (cell % AREA + 0.5f) * TILE - image.width() / 2f;
		float py = (cell / AREA + 1f) * TILE - image.height() - SPRITE_RAISE * TILE;
		place(out, image, camera, x + px * scale, y + py * scale, scale);
	}

	/**
	 * Draws the explored floor as coloured squares into [out], fitting a [size] square with its top left corner at [x], [y].
	 */
	void drawMap(List<Gizmo> out, Camera camera, float x, float y, float size) {
		float cell = size / Math.max(mapWidth, mapHeight);
		float offsetX = x + (size - cell * mapWidth) / 2f;
		float offsetY = y + (size - cell * mapHeight) / 2f;
		for (int row = 0; row < mapHeight; row++) {
			int start = 0;
			while (start < mapWidth) {
				byte kind = map[row * mapWidth + start];
				int end = start + 1;
				while (end < mapWidth && map[row * mapWidth + end] == kind) end++;
				if (kind != MAP_UNKNOWN) {
					ColorBlock block = new ColorBlock((end - start) * cell, cell, MAP_COLORS[kind]);
					place(out, block, camera, offsetX + start * cell, offsetY + row * cell, 1f);
				}
				start = end;
			}
		}
		if (tiles != null) {
			float left = offsetX + Math.max(0, areaLeft) * cell;
			float top = offsetY + Math.max(0, areaTop) * cell;
			float right = offsetX + Math.min(mapWidth, areaLeft + AREA) * cell;
			float bottom = offsetY + Math.min(mapHeight, areaTop + AREA) * cell;
			float line = Math.max(1f, cell / 3f);
			place(out, new ColorBlock(right - left, line, OUTLINE_COLOR), camera, left, top, 1f);
			place(out, new ColorBlock(right - left, line, OUTLINE_COLOR), camera, left, bottom - line, 1f);
			place(out, new ColorBlock(line, bottom - top, OUTLINE_COLOR), camera, left, top, 1f);
			place(out, new ColorBlock(line, bottom - top, OUTLINE_COLOR), camera, right - line, top, 1f);
		}
		float dot = Math.max(cell, 2f);
		for (Marker marker : markers) {
			float mx = offsetX + (marker.pos % mapWidth) * cell + (cell - dot) / 2f;
			float my = offsetY + (marker.pos / mapWidth) * cell + (cell - dot) / 2f;
			place(out, new ColorBlock(dot, dot, marker.color), camera, mx, my, 1f);
		}
	}

	private static void place(List<Gizmo> out, Visual visual, Camera camera, float x, float y, float scale) {
		visual.camera = camera;
		visual.x = x;
		visual.y = y;
		visual.scale.x *= scale;
		visual.scale.y *= scale;
		out.add(visual);
	}
}
