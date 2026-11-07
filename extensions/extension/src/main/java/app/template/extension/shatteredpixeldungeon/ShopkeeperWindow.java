package app.template.extension.shatteredpixeldungeon;

public class ShopkeeperWindow extends EditorWindow {

	private static final String INFO = "Shopkeeper"
		+ "\n\nBanish Enemies: when an enemy walks into the shop, the shopkeeper shouts \"Hey! Get out of here!\" and it is thrown out next to the shop door. It won't try to come back in for 20 turns. If you can't see the shop, you only hear an odd booming sound in the distance."
		+ "\n\nFearless: enemies fighting in the shop don't scare him off. He just sighs \"C'mon... Really?\" He also stays when you carry the Amulet back up. Your own attacks are covered by Passive and Lenient."
		+ "\n\nImmune: gas, fire and cold don't drive him away. While you're in the shop, he remarks on the smell, the heat or the chill."
		+ "\n\nPassive: attacking him does nothing but earn a \"Was that supposed to hurt?\" A thrown weapon gets \"Ooh. Free item!\" and goes up for sale in the shop. Catch you stealing and he only says \"I saw that, you know. Rude.\""
		+ "\n\nLenient: attacking him gets a warning, then a second warning, and on the third attack he throws you out of the shop. Thrown weapons go up for sale. If he catches you stealing, you pay for the item or leave the shop."
		+ "\n\nTo get back in after being thrown out, apologize with gold (the shop's price of a potion of healing) or with a potion of healing. The potion option shows once you have identified potions of healing. Passive and Lenient can't both be on."
		+ "\n\nWith Passive or Lenient on, attacking him from outside the shop, like with a projecting weapon, only gets \"What the hell was that?\""
		+ "\n\n{COIN_SML}Sold Items: how many items you sold to the shopkeeper you can buy back, up to 24. The game keeps 3. Tap i beside an item in the shop to examine it.";

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
