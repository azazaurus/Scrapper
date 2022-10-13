import javafx.util.*;
import org.openqa.selenium.*;
import ru.yandex.qatools.ashot.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileRepository {
	private static final Properties properties = PropertiesHelper.getProperties();

	public static void saveAudioToFile(InputStream in, String fileBaseName, String moduleName) {
		fileBaseName = replaceIllegalSymbols(fileBaseName);
		moduleName = replaceIllegalSymbols(moduleName);

		String audioPath = properties.getProperty("storage_folder_path") + moduleName +
			"\\" + fileBaseName + "\\" + "audio\\";

		try {
			Files.createDirectories(Paths.get(audioPath));
			Path filePath = Paths.get(audioPath, getAudioName(fileBaseName));
			Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveVideoToFile(InputStream in, String fileBaseName, String moduleName) {
		fileBaseName = replaceIllegalSymbols(fileBaseName);
		moduleName = replaceIllegalSymbols(moduleName);

		String videoPath = properties.getProperty("storage_folder_path") + moduleName +
			"\\" + fileBaseName + "\\" + "video\\";

		try {
			Files.createDirectories(Paths.get(videoPath));
			Path filePath = Paths.get(videoPath, getVideoName(fileBaseName));
			Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveScreenshotToFile(Screenshot screenshot, WebElement element, String fileBaseName, String moduleName) {
		fileBaseName = replaceIllegalSymbols(fileBaseName);
		moduleName = replaceIllegalSymbols(moduleName);

		try {
			BufferedImage subImage = getSubImage(screenshot.getImage(), element.getLocation(),
				element.getSize().getWidth(), element.getSize().getHeight());

			String textContentPath = properties.getProperty("storage_folder_path") + moduleName +
			"\\" + fileBaseName + "\\" + "screenshot\\" + getScreenshotName(fileBaseName);

			File screenshotFile = new File(textContentPath);
			screenshotFile.mkdirs();
			screenshotFile.createNewFile();

			ImageIO.write(subImage, "png", screenshotFile);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getScreenshotName(String name) {
		return getFileName(name, "png");
	}

	private static String getAudioName(String name) {
		return getFileName(name, "mp3");
	}

	private static String getVideoName(String name) {
		return getFileName(name, "m3u8");
	}

	private static String getFileName(String name, String fileExtension) {
		return name + "." + fileExtension;
	}

	private static BufferedImage getSubImage(BufferedImage screenshot, Point location, int width, int height) {
		return screenshot.getSubimage(location.getX(), location.getY(),
			width, height);
	}

	public static String replaceIllegalSymbols(String string) {
		List<Pair<String, String>> replacements = List.of(
			new Pair<>("->", "→"));

		String replacedSymbolsString = string;
		for (Pair<String, String> replacement : replacements) {
			replacedSymbolsString = replacedSymbolsString.replaceAll(replacement.getKey(), replacement.getValue());
		}

		int stringLength = replacedSymbolsString.length();
		char[] stringCharArray = replacedSymbolsString.toCharArray();
		for (int i = 0; i < stringLength; i++) {
			if (stringCharArray[i] != ':') {
				continue;
			}

			stringCharArray[i] = '.';

			getLetterToReplaceIndex(stringCharArray, i + 1)
				.ifPresent(index -> stringCharArray[index] = Character.toUpperCase(stringCharArray[index]));
		}

		return String.valueOf(stringCharArray);
	}

	private static Optional<Integer> getLetterToReplaceIndex(char[] stringCharArray, int startIndex) {
		int stringLength = stringCharArray.length;

		for (int j = startIndex; j < stringLength; j++) {
			if (Character.isAlphabetic(stringCharArray[j])) {
				return Optional.of(j);
			}
		}

		return Optional.empty();
	}
}

