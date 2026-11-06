package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Small buttons in the top right corner of a game window, beside its title. Buttons added by different patches
 * line up, the [i] button always furthest right. The title narrows to make room, and if it needs another line the
 * rest of the window moves down.
 */
final class WindowHeader {

	static final int PRIORITY_INFO = 0;
	static final int PRIORITY_ACTION = 1;
	static final int SMALL_HEIGHT = 12;
	static final int EDIT_WIDTH = 24;
	private static final int ROW = 16;
	private static final int GAP = 2;

	private static final Map<Window, State> states = Collections.synchronizedMap(new WeakHashMap<>());

	private static final class Entry {
		final Component button;
		final float width;
		final float height;
		final int priority;

		Entry(Component button, float width, float height, int priority) {
			this.button = button;
			this.width = width;
			this.height = height;
			this.priority = priority;
		}
	}

	private static final class State {
		Component title;
		float lastBottom;
		final List<Entry> entries = new ArrayList<>();
	}

	private WindowHeader() {
	}

	static RedButton smallButton(String label, Icons icon, Runnable action) {
		RedButton button = new RedButton(label, 6) {
			@Override
			public void onClick() {
				action.run();
			}
		};
		if (icon != null) button.icon(Icons.get(icon));
		return button;
	}

	static void addEdit(Window window, Runnable action) throws ReflectiveOperationException {
		add(window, smallButton("Edit", null, action), EDIT_WIDTH, SMALL_HEIGHT, PRIORITY_ACTION);
	}

	@SuppressWarnings("unchecked")
	static void add(Window window, Component button, float width, float height, int priority) throws ReflectiveOperationException {
		Field membersField = Group.class.getDeclaredField("members");
		membersField.setAccessible(true);
		Field widthField = Window.class.getDeclaredField("width");
		Field heightField = Window.class.getDeclaredField("height");
		widthField.setAccessible(true);
		heightField.setAccessible(true);
		int windowWidth = widthField.getInt(window);
		int windowHeight = heightField.getInt(window);

		State state = states.get(window);
		if (state == null) {
			state = new State();
			state.title = findTitle((List<Gizmo>) membersField.get(window), windowWidth);
			state.lastBottom = state.title == null ? 0 : state.title.bottom();
			states.put(window, state);
		}

		int index = 0;
		while (index < state.entries.size() && state.entries.get(index).priority <= priority) index++;
		state.entries.add(index, new Entry(button, width, height, priority));
		window.add(button);

		float x = windowWidth;
		for (Entry entry : state.entries) {
			x -= entry.width;
			entry.button.setRect(x, (ROW - entry.height) / 2f, entry.width, entry.height);
			x -= GAP;
		}
		if (state.title == null) return;

		Component title = state.title;
		title.setRect(title.left(), title.top(), Math.max(10, x - title.left()), 0);
		float shift = title.bottom() - state.lastBottom;
		if (shift <= 0) return;
		for (Gizmo member : new ArrayList<>((List<Gizmo>) membersField.get(window))) {
			if (member == title || !(member instanceof Component) || isHeaderButton(state, member)) continue;
			Component component = (Component) member;
			if (component.top() < state.lastBottom - 1) continue;
			component.setPos(component.left(), component.top() + shift);
		}
		state.lastBottom = title.bottom();
		window.resize(windowWidth, (int) (windowHeight + shift));
	}

	private static boolean isHeaderButton(State state, Gizmo member) {
		for (Entry entry : state.entries) {
			if (entry.button == member) return true;
		}
		return false;
	}

	private static Component findTitle(List<Gizmo> members, int windowWidth) {
		for (Gizmo member : members) {
			if (!(member instanceof Component) || member instanceof RenderedTextBlock) continue;
			Component component = (Component) member;
			if (component.top() > 1 || component.width() < windowWidth / 2f) continue;
			return component;
		}
		return null;
	}
}
