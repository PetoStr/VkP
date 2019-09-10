package org.vkp.engine.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vkp.engine.util.FileUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FntParser {

	private FntParser() {

	}

	public static Map<Character, Glyph> parseFntFile(String fntPath) {
		Map<Character, Glyph> glyphs = new HashMap<>();
		InputStream stream;

		try {
			stream = FileUtil.getResourceURL(fntPath).openStream();
		} catch (IOException e) {
			log.error("", e);
			throw new AssertionError();
		}
		Scanner sc = new Scanner(stream);
		while (sc.hasNext()) {
			String line = sc.nextLine();
			if (!line.startsWith("char id")) continue;
			Pattern p = Pattern.compile("char id=(\\d+)"
					+ "(\\s+)x=(-?[0-9]+)"
					+ "(\\s+)y=(-?[0-9]+)"
					+ "(\\s+)width=(-?[0-9]+)"
					+ "(\\s+)height=(-?[0-9]+)"
					+ "(\\s+)xoffset=(-?[0-9]+)"
					+ "(\\s+)yoffset=(-?[0-9]+)"
					+ "(\\s+)xadvance=(-?[0-9]+)");
			Matcher m = p.matcher(line);
			m.find();
			int id = Integer.parseInt(m.group(1));
			int x = Integer.parseInt(m.group(3));
			int y = Integer.parseInt(m.group(5));
			int width = Integer.parseInt(m.group(7));
			int height = Integer.parseInt(m.group(9));
			int xOffset = Integer.parseInt(m.group(11));
			int yOffset = Integer.parseInt(m.group(13));
			int xAdvance = Integer.parseInt(m.group(15));

			Glyph glyph = Glyph.builder()
					.x(x)
					.y(y)
					.width(width)
					.height(height)
					.xOffset(xOffset)
					.yOffset(yOffset)
					.xAdvance(xAdvance).build();
			Character ch = (char) id;
			glyphs.put(ch, glyph);
		}
		sc.close();

		log.info("Parsed {}", fntPath);

		return glyphs;
	}

}
