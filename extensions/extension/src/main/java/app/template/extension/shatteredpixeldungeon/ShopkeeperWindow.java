package app.template.extension.shatteredpixeldungeon;

public class ShopkeeperWindow extends EditorWindow {

	private static final String INFO = "Shopkeeper"
		+ "\n\nBanish Enemies: when an enemy walks into the shop, the shopkeeper shouts \"Hey! Get out of here!\" and it is thrown out next to the shop door. It won't try to come back in for 20 turns."
		+ "\n\nFearless: fighting in the shop doesn't scare the shopkeeper off. He just sighs \"C'mon... Really?\""
		+ "\n\nImmune: gas, fire and cold don't drive him away either. He only remarks on the smell, the heat or the chill."
		+ "\n\nPassive: attacking the shopkeeper yourself does nothing but earn a \"Was that supposed to hurt?\""
		+ "\n\nLenient: attacking him gets a warning, then a second warning, and on the third attack he throws you out of the shop. To get back in, you'll have to apologize with gold, the shop's price of a potion of healing, or with a potion of healing. The potion option shows once you have identified potions of healing. Passive and Lenient can't both be on."
		+ "\n\n{COIN_SML}Sold Items: how many items you sold to the shopkeeper you can buy back. The game keeps 3.";

	public ShopkeeperWindow() {
		super();
		addTitle("Shopkeeper", INFO);
		addCheckBox("Banish Enemies", ShopkeeperRules.banish(), () -> ShopkeeperRules.banish(!ShopkeeperRules.banish()));
		addCheckBox("Fearless", ShopkeeperRules.fearless(), () -> ShopkeeperRules.fearless(!ShopkeeperRules.fearless()));
		addCheckBox("Immune", ShopkeeperRules.immune(), () -> ShopkeeperRules.immune(!ShopkeeperRules.immune()));
		addCheckBox("Passive", ShopkeeperRules.passive(), () -> ShopkeeperRules.passive(!ShopkeeperRules.passive()));
		addCheckBox("Lenient", ShopkeeperRules.lenient(), () -> ShopkeeperRules.lenient(!ShopkeeperRules.lenient()));
		int sold = ShopkeeperRules.soldItems();
		addStepper("Sold Items", String.valueOf(sold), sold, 1, 0, ShopkeeperRules.MAX_SOLD_ITEMS, ShopkeeperRules::soldItems, true, () -> ShopkeeperRules.soldItems(ShopkeeperRules.DEFAULT_SOLD_ITEMS));
		pos += 2 * GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new ShopkeeperWindow());
	}
}
