package app.template.extension.shatteredpixeldungeon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Image;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.BreakIterator;

/**
 * Tag icons made from an emoji. The game's pixel font can't draw emoji, so each one is drawn by Android into a small picture the size of the
 * game's icons, which then scales up crisp like the game's own sprites.
 */
final class EmojiIcon {

	static final String PREFIX = "EMOJI:";
	private static final String TAG = "EmojiIcon";
	private static final String KEY_PREFIX = "cloverlord_emoji:";
	private static final int SIZE = 16;
	private static final float TEXT_SHARE = 0.85f;
	private static final int BYTES_PER_PIXEL = 4;

	private EmojiIcon() {
	}

	/**
	 * The first emoji, or other single character, in [text], or null when there is none.
	 */
	static String first(String text) {
		if (text == null) return null;
		String trimmed = text.trim();
		if (trimmed.isEmpty()) return null;
		BreakIterator characters = BreakIterator.getCharacterInstance();
		characters.setText(trimmed);
		int end = characters.next();
		return end == BreakIterator.DONE ? trimmed : trimmed.substring(0, end);
	}

	/**
	 * The icon for [emoji], drawn the first time it is needed and kept with the game's other pictures after that.
	 */
	static Image image(String emoji) {
		String key = KEY_PREFIX + emoji;
		if (!TextureCache.contains(key) && !draw(key, emoji)) return null;
		return new Image(key);
	}

	private static boolean draw(String key, String emoji) {
		try {
			int[] pixels = render(emoji);
			SmartTexture texture = TextureCache.create(key, SIZE, SIZE);
			texture.filter(Texture.NEAREST, Texture.NEAREST);
			ByteBuffer buffer = pixelBuffer(Reflect.callStatic(TextureCache.class, "getBitmap", new Class<?>[] { Object.class }, key));
			buffer.rewind();
			for (int pixel : pixels) {
				buffer.put((byte) (pixel >> 16));
				buffer.put((byte) (pixel >> 8));
				buffer.put((byte) pixel);
				buffer.put((byte) (pixel >>> 24));
			}
			buffer.rewind();
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Could not draw the emoji", e);
			TextureCache.remove(key);
			return false;
		}
	}

	/**
	 * [emoji] drawn centered in a SIZE by SIZE picture, as unpremultiplied ARGB pixels row by row.
	 */
	private static int[] render(String emoji) {
		Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(SIZE * TEXT_SHARE);
		paint.setTextAlign(Paint.Align.CENTER);
		Paint.FontMetrics metrics = paint.getFontMetrics();
		canvas.drawText(emoji, SIZE / 2f, SIZE / 2f - (metrics.ascent + metrics.descent) / 2f, paint);
		int[] pixels = new int[SIZE * SIZE];
		bitmap.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE);
		bitmap.recycle();
		return pixels;
	}

	/**
	 * The pixel memory of the game's picture object, found by what it returns since the class is renamed in the game.
	 */
	private static ByteBuffer pixelBuffer(Object pixmap) throws ReflectiveOperationException {
		for (Method method : pixmap.getClass().getMethods()) {
			if (method.getParameterTypes().length == 0 && method.getReturnType() == ByteBuffer.class) {
				ByteBuffer buffer = (ByteBuffer) method.invoke(pixmap);
				if (buffer != null && buffer.capacity() >= SIZE * SIZE * BYTES_PER_PIXEL) return buffer;
			}
		}
		throw new NoSuchMethodException("No pixel buffer on " + pixmap.getClass().getName());
	}
}
