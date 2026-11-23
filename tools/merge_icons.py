"""Merge and export: layers pictures into one custom icon and saves it with the patches' other custom icons.

Each layer is a picture fitted into a box of the icon, drawn over the layers before it: --layer PICTURE X Y SIZE [ANGLE], where SIZE is
one number for a square or WIDTHxHEIGHT, and ANGLE, when given, turns the picture that many degrees counterclockwise first. A layer's
picture is a PNG file, a part of one (file.png@x,y,width,height), or a cell of one of the game's sprite sheets (items:column,row,
icons:column,row) read from the game's APK. Transparent edges are trimmed before fitting, and pixels are scaled and turned without
blurring.

Example, the Equipment tab icon: the round shield on the left and the shortsword, turned upright, on the right.
	python3 tools/merge_icons.py equipment --apk spd.apk --layer items:3,7 0 2 12 --layer items:0,6 10 0 6x16 45
"""

import argparse
import io
import os
import zipfile

from PIL import Image

ICONS = os.path.join(os.path.dirname(__file__), "..", "patches", "src", "main", "resources", "cloverlord", "icons")
INDEX = "index.txt"
TURN_DETAIL = 8
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


def trim(picture):
	box = picture.getbbox()
	return picture.crop(box) if box else picture


def turn(picture, angle):
	big = picture.resize((picture.width * TURN_DETAIL, picture.height * TURN_DETAIL), Image.NEAREST)
	return trim(big.rotate(angle, Image.NEAREST, expand=True))


def fit(picture, box_width, box_height):
	picture = trim(picture)
	scale = min(box_width / picture.width, box_height / picture.height)
	width = max(1, round(picture.width * scale))
	height = max(1, round(picture.height * scale))
	return picture.resize((width, height), Image.NEAREST)


def main():
	parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
	parser.add_argument("name", help="the icon's name: letters, digits and underscores")
	parser.add_argument("--size", type=int, default=16, help="the icon's width and height in pixels (default 16, an item's size)")
	parser.add_argument("--apk", help="the game's APK, for layers from its sprite sheets")
	parser.add_argument("--layer", nargs="+", action="append", required=True, metavar="PICTURE X Y SIZE [ANGLE]",
		help="a picture fitted into the SIZE box at X, Y, turned ANGLE degrees first; repeat for each layer, bottom first")
	options = parser.parse_args()

	icon = Image.new("RGBA", (options.size, options.size), (0, 0, 0, 0))
	for values in options.layer:
		if len(values) not in (4, 5):
			parser.error("each --layer needs PICTURE X Y SIZE and an optional ANGLE")
		spec, x, y, size = values[:4]
		x, y = int(x), int(y)
		box_width, _, box_height = size.partition("x")
		box_width = int(box_width)
		box_height = int(box_height) if box_height else box_width
		picture = source(spec, options.apk)
		if len(values) == 5:
			picture = turn(picture, float(values[4]))
		picture = fit(picture, box_width, box_height)
		layer = Image.new("RGBA", icon.size, (0, 0, 0, 0))
		layer.paste(picture, (x + (box_width - picture.width) // 2, y + (box_height - picture.height) // 2))
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
