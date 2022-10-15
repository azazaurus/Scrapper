import converter.*;
import javafx.util.*;
import org.openqa.selenium.*;
import ru.yandex.qatools.ashot.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileRepository {
	private static final Properties properties = PropertiesHelper.getProperties();
	private static final M3uConverter m3uConverter = new M3uConverter(
		new HLSParser(),
		createFfmpegClient(properties));

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

		String m3u8VideoPath = properties.getProperty("storage_folder_path") + moduleName +
			"\\" + fileBaseName + "\\" + "m3u8 video\\";

		try {
			Files.createDirectories(Paths.get(m3u8VideoPath));
			Path filePath = Paths.get(m3u8VideoPath, getVideoName(fileBaseName));
			Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String mp4VideoPath = properties.getProperty("storage_folder_path") + moduleName +
			"\\" + fileBaseName + "\\" + "mp4 video\\" + getMp4Name(fileBaseName);

		convertVideoFromM3u8toMp4(m3u8VideoPath + getVideoName(fileBaseName), mp4VideoPath);
	}

	public static void convertVideoFromM3u8toMp4(String m3u8VideoPath, String outputMp4Path) {
		try {
			m3uConverter.convertToMp4(m3u8VideoPath, outputMp4Path);
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

	private static String getMp4Name(String name) {
		return getFileName(name, "mp4");
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

	private static FfmpegClient createFfmpegClient(Properties properties) {
		try {
			return new FfmpegClient(properties.getProperty("ffmpeg.path"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

