"""Merge and export: layers pictures into one custom icon and saves it with the patches' other custom icons.

Each layer is a picture fitted into a square of the icon, drawn over the layers before it. A layer's picture is a PNG file, a part of
one (file.png@x,y,width,height), or a cell of one of the game's sprite sheets (items:column,row, icons:column,row) read from the game's
APK. Transparent edges are trimmed before fitting, and pixels are scaled without blurring.

Example, the Equipment tab icon: a leather armor in the lower left with a sword over it in the upper right.
	python3 tools/merge_icons.py equipment --apk spd.apk --layer items:1,11 0 4 12 --layer items:0,7 4 0 12
"""

import argparse
import io
import os
import zipfile

from PIL import Image

ICONS = os.path.join(os.path.dirname(__file__), "..", "patches", "src", "main", "resources", "cloverlord", "icons")
INDEX = "index.txt"
SHEETS = {"items": ("assets/sprites/items.png", 16), "icons": ("assets/interfaces/icons.png", 16)}


def source(spec, apk):
	if ":" in spec and spec.split(":", 1)[0] in SHEETS:
		sheet, cell = spec.split(":", 1)
		path, size = SHEETS[sheet]
		if apk is None:
			raise SystemExit(f"{spec} needs the game's APK: add --apk")
		with zipfile.ZipFile(apk) as archive:
			picture = Image.open(io.BytesIO(archive.read(path))).convert("RGBA")
		column, row = (int(part) for part in cell.split(","))
		return picture.crop((column * size, row * size, (column + 1) * size, (row + 1) * size))
	path, _, area = spec.partition("@")
	picture = Image.open(path).convert("RGBA")
	if not area:
		return picture
	x, y, width, height = (int(part) for part in area.split(","))
	return picture.crop((x, y, x + width, y + height))


def fit(picture, size):
	box = picture.getbbox()
	if box:
		picture = picture.crop(box)
	scale = min(size / picture.width, size / picture.height)
	width = max(1, round(picture.width * scale))
	height = max(1, round(picture.height * scale))
	return picture.resize((width, height), Image.NEAREST)


def main():
	parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
	parser.add_argument("name", help="the icon's name: letters, digits and underscores")
	parser.add_argument("--size", type=int, default=16, help="the icon's width and height in pixels (default 16, an item's size)")
	parser.add_argument("--apk", help="the game's APK, for layers from its sprite sheets")
	parser.add_argument("--layer", nargs=4, action="append", required=True, metavar=("PICTURE", "X", "Y", "SIZE"),
		help="a picture fitted into the SIZE square at X, Y; repeat for each layer, bottom first")
	options = parser.parse_args()

	icon = Image.new("RGBA", (options.size, options.size), (0, 0, 0, 0))
	for spec, x, y, size in options.layer:
		x, y, size = int(x), int(y), int(size)
		picture = fit(source(spec, options.apk), size)
		layer = Image.new("RGBA", icon.size, (0, 0, 0, 0))
		layer.paste(picture, (x + (size - picture.width) // 2, y + (size - picture.height) // 2))
		icon = Image.alpha_composite(icon, layer)

	os.makedirs(ICONS, exist_ok=True)
	icon.save(os.path.join(ICONS, options.name + ".png"))
	index = os.path.join(ICONS, INDEX)
	names = open(index).read().split() if os.path.exists(index) else []
	if options.name not in names:
		names.append(options.name)
	with open(index, "w") as out:
		out.write("\n".join(names) + "\n")
	print(f"Saved {options.name}.png")


if __name__ == "__main__":
	main()
