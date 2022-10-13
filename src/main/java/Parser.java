import javafx.util.*;
import org.openqa.selenium.*;
import ru.yandex.qatools.ashot.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Parser {
	private final Properties properties = PropertiesHelper.getProperties();
	private final ChromeDriverHelper chromeDriverHelper = new ChromeDriverHelper();

	public void start() {
		chromeDriverHelper.init();
		chromeDriverHelper.setCookies(chromeDriverHelper.parseCookies());
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		parseModulesListPage(properties.getProperty("start_page_url"));
	}

	private void parseModulesListPage(String url) {
		chromeDriverHelper.goTo(url);
		
		List<Pair<String, String>> modulesUrlAndName = chromeDriverHelper.getModuleNameAndLink();
		for (Pair<String, String> moduleNameAndUrl : modulesUrlAndName) {
			parseLessonsListPage(moduleNameAndUrl.getKey(), moduleNameAndUrl.getValue());
		}
	}

	private void parseLessonsListPage(String moduleUrl, String moduleName) {
		chromeDriverHelper.goTo(moduleUrl);
		
		List<Pair<String, String>> lessonsUrlAndName = chromeDriverHelper.getLessonNameAndUrl();

		int lessonsCount = lessonsUrlAndName.size();
		Pair<String, String> lessonUrlAndName;
		for (int i = 0; i < lessonsCount; i++) {
			lessonUrlAndName = new Pair<>(
				lessonsUrlAndName.get(i).getKey(),
				(i + 1) + ". " + lessonsUrlAndName.get(i).getValue());

			parseLessonPage(lessonUrlAndName.getKey(), lessonUrlAndName.getValue(), moduleName);
		}
	}

	private void parseLessonPage(String lessonUrl, String lessonName, String moduleName) {
		chromeDriverHelper.goTo(lessonUrl);
		tryFindAndDownloadTextContent(lessonName, moduleName);
		tryFindAndDownloadVideo(lessonName, moduleName);
		tryFindAndDownloadAudio(lessonName, moduleName);
	}

	private void tryFindAndDownloadTextContent(String lessonName, String moduleName) {
		Optional<WebElement> contentElement = chromeDriverHelper.getWebElement(
			properties.getProperty("xpath_to_text_content"));

		if (contentElement.isEmpty()) {
			return;
		}

		Screenshot screenshot = chromeDriverHelper.getScreenshot();

		FileRepository.saveScreenshotToFile(
			screenshot,
			contentElement.get(),
			lessonName,
			moduleName
		);
	}

	private void tryFindAndDownloadVideo(String lessonName, String moduleName) {
		Optional<String> videoM3u8TextFileDownloadLink = chromeDriverHelper.getM3u8TextFileDownloadLink();

		URL m3u8TextFileDownloadUrl = null;
		if (videoM3u8TextFileDownloadLink.isPresent())
			try {
				m3u8TextFileDownloadUrl = new URL(videoM3u8TextFileDownloadLink.get());
				InputStream videoStream = Downloader.downloadVideo(m3u8TextFileDownloadUrl);
				FileRepository.saveVideoToFile(videoStream, lessonName, moduleName);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
	}

	private void tryFindAndDownloadAudio(String lessonName, String moduleName) {
		Optional<String> audioDownloadLink = chromeDriverHelper.getAudioDownloadLink();

		URL audioDownloadUrl = null;
		if (audioDownloadLink.isPresent())
			try {
				audioDownloadUrl = new URL(audioDownloadLink.get());
				InputStream videoStream = Downloader.downloadAudio(audioDownloadUrl);
				FileRepository.saveAudioToFile(videoStream, lessonName, moduleName);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
	}
}

