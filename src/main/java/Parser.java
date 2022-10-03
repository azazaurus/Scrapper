import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.openqa.selenium.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Parser {
	Properties properties = PropertiesHelper.getProperties();
	ChromeDriverHelper chromeDriverHelper = new ChromeDriverHelper();

	public void start() {
		chromeDriverHelper.set();
		parseModulesListPage(properties.getProperty("start_page_url"));
	}

	private Document getHtml(String url) {
		Document document;
		try {
			for (Cookie cookie : chromeDriverHelper.parseCookie()) {
				chromeDriverHelper.setCookie(cookie);
			}

			document = Jsoup.parse(chromeDriverHelper.getHtmlStream(url), "UTF-8", "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return document;
	}

	private void parseModulesListPage(String url) {
		Document document = getHtml(url);
		Elements moduleElements = document.select("tr.no-children");
		for (Element element : moduleElements) {
			String moduleUrl = Objects.requireNonNull(element.select("a").first()).attr("abs:href");
			parseLessonsListPage(moduleUrl);
		}
	}

	private void parseLessonsListPage(String url) {
		Document document = getHtml(url);
		Elements moduleElements = document.getElementsByClass("link title");
		String moduleName = document.getElementsByClass("page-header").tagName("h1").val();
		for (Element element : moduleElements) {
			String lessonUrl =
			Objects.requireNonNull(element.attr("abs:href"));
			parseLessonPage(lessonUrl, moduleName);
		}
	}

	private void parseLessonPage(String url, String moduleName) {
		Document document = getHtml(url);
		String lessonTitle = document.getElementsByClass("lesson-title-value").val();

		String videoM3u8TextFileDownloadLink = document.getElementsByClass("vvd-video").first()
			.select("source").first().attr("src");

		String audioDownloadLink = document.getElementsByClass("jp_audio_0").first()
			.select("source").first().attr("src");

		URL m3u8TextFileDownloadUrl = null;
		if (videoM3u8TextFileDownloadLink != null)
			try {
				m3u8TextFileDownloadUrl = new URL(videoM3u8TextFileDownloadLink);
				String fileName = moduleName + ". " + lessonTitle;
				downloadVideoToFile(m3u8TextFileDownloadUrl,fileName);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}

		System.out.println("Download video");
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
