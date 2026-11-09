package app.template.extension.shatteredpixeldungeon;

import android.opengl.GLES20;
import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.utils.PointF;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Small pictures of the dungeon around the hero for undo steps. The picture is read straight after the dungeon is drawn, before any buttons or windows,
 * shrunk to a small square and kept compressed in memory.
 */
final class UndoPreview {

	static final int SIZE = 144;
	private static final float SCREEN_SHARE = 0.8f;
	private static final String TAG = "UndoPreview";
	private static final String TEXTURE_KEY = "cloverlord_undo_preview_";

	interface Receiver {
		void accept(byte[] image);
	}

	private static volatile Receiver pending;
	private static ByteBuffer buffer;
	private static int textureCount;
	private static String textureKey;

	private UndoPreview() {
	}

	/**
	 * Asks for a picture of the next frame in which the hero is standing still.
	 */
	static void request(Receiver receiver) {
		pending = receiver;
	}

	/**
	 * Puts the picture taker into the game scene, right after the dungeon and before the interface.
	 */
	@SuppressWarnings("unchecked")
	static void install(Scene scene) {
		try {
			Field selectorField = GameScene.class.getDeclaredField("cellSelector");
			selectorField.setAccessible(true);
			Object selector = selectorField.get(null);
			Field membersField = Group.class.getDeclaredField("members");
			membersField.setAccessible(true);
			ArrayList<Gizmo> members = (ArrayList<Gizmo>) membersField.get(scene);
			int position = members.indexOf(selector);
			if (position < 0) return;
			Gizmo grabber = new Gizmo() {
				@Override
				public void draw() {
					grab();
				}
			};
			grabber.parent = scene;
			members.add(position + 1, grabber);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add the preview taker", e);
		}
	}

	private static void grab() {
		Receiver receiver = pending;
		Hero hero = Dungeon.hero;
		if (receiver == null || hero == null || hero.sprite == null || hero.sprite.isMoving) return;
		pending = null;
		try {
			byte[] image = read(hero);
			if (image != null) receiver.accept(image);
		} catch (Throwable e) {
			Log.e(TAG, "Could not take a preview", e);
		}
	}

	private static byte[] read(Hero hero) {
		Camera camera = Camera.main;
		if (camera == null || Game.width <= 0 || Game.height <= 0) return null;
		int side = (int) (Math.min(Game.width, Game.height) * SCREEN_SHARE);
		if (side < SIZE) return null;
		PointF center = hero.sprite.center();
		int centerX = (int) ((center.x - camera.scroll.x) * camera.zoom + camera.x);
		int centerY = (int) ((center.y - camera.scroll.y) * camera.zoom + camera.y);
		int left = Math.max(0, Math.min(Game.width - side, centerX - side / 2));
		int top = Math.max(0, Math.min(Game.height - side, centerY - side / 2));
		int bytes = side * side * 4;
		if (buffer == null || buffer.capacity() < bytes) buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
		buffer.clear();
		GLES20.glReadPixels(left, Game.height - top - side, side, side, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
		byte[] rgb = new byte[SIZE * SIZE * 3];
		int out = 0;
		for (int y = 0; y < SIZE; y++) {
			int row = side - 1 - (y * side + side / 2) / SIZE;
			for (int x = 0; x < SIZE; x++) {
				int column = (x * side + side / 2) / SIZE;
				int at = (row * side + column) * 4;
				rgb[out++] = buffer.get(at);
				rgb[out++] = buffer.get(at + 1);
				rgb[out++] = buffer.get(at + 2);
			}
		}
		return deflate(rgb);
	}

	/**
	 * An image of a stored picture, or null if it can't be shown. Only the latest image's texture is kept.
	 */
	static Image image(byte[] data) {
		if (data == null) return null;
		try {
			byte[] rgb = inflate(data, SIZE * SIZE * 3);
			release();
			textureKey = TEXTURE_KEY + (textureCount++);
			SmartTexture texture = TextureCache.create(textureKey, SIZE, SIZE);
			Field bitmapField = texture.getClass().getField("bitmap");
			Object bitmap = bitmapField.get(texture);
			ByteBuffer pixels = pixelBuffer(bitmap);
			pixels.position(0);
			for (int i = 0; i < rgb.length; i += 3) {
				pixels.put(rgb[i]);
				pixels.put(rgb[i + 1]);
				pixels.put(rgb[i + 2]);
				pixels.put((byte) 0xFF);
			}
			pixels.position(0);
			for (Method method : texture.getClass().getMethods()) {
				if (!method.getName().equals("bitmap") || method.getParameterTypes().length != 1) continue;
				method.invoke(texture, bitmap);
				break;
			}
			return new Image(texture);
		} catch (Throwable e) {
			Log.e(TAG, "Could not show a preview", e);
			return null;
		}
	}

	/**
	 * Frees the texture of the last image shown.
	 */
	static void release() {
		if (textureKey == null) return;
		try {
			TextureCache.remove(textureKey);
		} catch (Throwable e) {
			Log.e(TAG, "Could not free a preview", e);
		}
		textureKey = null;
	}

	private static ByteBuffer pixelBuffer(Object bitmap) throws ReflectiveOperationException {
		for (Method method : bitmap.getClass().getMethods()) {
			if (method.getParameterTypes().length != 0 || method.getReturnType() != ByteBuffer.class) continue;
			return (ByteBuffer) method.invoke(bitmap);
		}
		throw new NoSuchMethodException("pixels");
	}

	private static byte[] deflate(byte[] data) {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(data);
		deflater.finish();
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 4);
		byte[] chunk = new byte[8192];
		while (!deflater.finished()) out.write(chunk, 0, deflater.deflate(chunk));
		deflater.end();
		return out.toByteArray();
	}

	private static byte[] inflate(byte[] data, int length) throws DataFormatException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		byte[] out = new byte[length];
		int read = 0;
		while (read < length && !inflater.finished()) {
			int count = inflater.inflate(out, read, length - read);
			if (count == 0 && inflater.needsInput()) break;
			read += count;
		}
		inflater.end();
		return out;
	}
}
