package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndBag extends Window {
	public static Window INSTANCE;

	public WndBag(Bag bag) { throw new RuntimeException("Stub"); }
	public WndBag(Bag bag, ItemSelector selector) { throw new RuntimeException("Stub"); }

	public abstract static class ItemSelector {
	}
}
