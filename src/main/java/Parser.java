import javafx.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Parser {
	private ArrayList<PageContent> pageContents;
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
		List<Pair<String, String>> modulesUrlAndName = chromeDriverHelper.getModuleNameAndlink();

		for (Pair<String, String> moduleNameAndUrl : modulesUrlAndName) {
			parseLessonsListPage(moduleNameAndUrl.getKey(), moduleNameAndUrl.getValue());
		}
	}

	private void parseLessonsListPage(String moduleUrl, String moduleName) {
		chromeDriverHelper.goTo(moduleUrl);
		List<Pair<String, String>> lessonsUrlAndName = chromeDriverHelper.getLessonNameAndUrl();

		for (Pair<String, String> lessonUrlAndName : lessonsUrlAndName) {
			parseLessonPage(lessonUrlAndName.getKey(), lessonUrlAndName.getValue(), moduleName);
		}
	}

	private void parseLessonPage(String lessonUrl, String lessonName, String moduleName) {
		chromeDriverHelper.goTo(lessonUrl);
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
}

