import javafx.util.*;
import org.openqa.selenium.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Parser {
	ArrayList<PageContent>  pageContents;
	Properties properties = PropertiesHelper.getProperties();
	ChromeDriverHelper chromeDriverHelper = new ChromeDriverHelper();

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
		List<Pair<String, String>> moduleNameAndUrl = chromeDriverHelper.getModuleNameAndUrl();
		parseLessonsListPage(moduleNameAndUrl.get(0).getKey());
//		for (Element element : moduleElements) {
//			String moduleUrl = Objects.requireNonNull(element.select("a").first()).attr("abs:href");
//			parseLessonsListPage(moduleUrl);
//		}
	}

	private void parseLessonsListPage(String url) {
		chromeDriverHelper.goTo(url);
		List<Pair<String, String>> lessonNameAndUrl = chromeDriverHelper.getLessonNameAndUrl();
		parseLessonPage(lessonNameAndUrl.get(0).getKey());
//		for (Element element : lessonNameAndUrl) {
//			String lessonUrl =
//			Objects.requireNonNull(element.attr("abs:href"));
//			parseLessonPage(lessonUrl);
//		}
	}

	private void parseLessonPage(String url) {
		chromeDriverHelper.goTo(url);
		String videoM3u8TextFileDownloadLink = chromeDriverHelper.getM3u8TextFileDownloadLink();

		URL m3u8TextFileDownloadUrl = null;
		if (videoM3u8TextFileDownloadLink != null)
			try {
				m3u8TextFileDownloadUrl = new URL(videoM3u8TextFileDownloadLink);
				String fileName = "Test.m3u8";
//				String fileName = moduleName + ". " + lessonTitle;
				downloadVideoToFile(m3u8TextFileDownloadUrl,fileName);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
//
//		System.out.println("Download video");
	}
	public void downloadVideoToFile(URL m3u8TextFileUrl, String fileName){
		URL videoDownloadUrl = getVideoUrl(m3u8TextFileUrl);
		InputStream in = null;
		try {
			in = videoDownloadUrl.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		saveVideoToFile(in, fileName);
	}

	public URL getVideoUrl(URL m3u8TextFileUrl) {
		URL videoDownloadUrl = null;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(m3u8TextFileUrl.openStream()))) {
			String urlString = in.lines().skip(6).findFirst().orElseThrow();
			videoDownloadUrl =  new URL(urlString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return videoDownloadUrl;
	}

	public void saveVideoToFile(InputStream in, String fileName) {
		try {
			Path filePath = Paths.get(properties.getProperty("storage_folder_path"), fileName);
			Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}
