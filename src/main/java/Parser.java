import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Parser {
	ArrayList<PageContent>  pageContents;
	Properties properties = PropertiesHelper.getProperties();

	public void start() throws IOException {
		parseModulesListPage(properties.getProperty("start-page_url"));
	}

	private Document getHTML(String url) {
		Document document;
		try {
			document = Jsoup.connect(url)
				.cookie(properties.getProperty("cookie_header"), properties.getProperty("cookie")).get();
		} catch (IOException e) {
			throw new RuntimeException("Page connection is failed");
		}

		return document;
	}

	private void parseModulesListPage(String url) {
		Document document = getHTML(url);
		Elements moduleElements = document.getElementsByClass("no-children");
		for (Element element : moduleElements) {
			String moduleUrl = Objects
				.requireNonNull(element.select("a").first()).attr("abs:href");
			parseLessonsListPage(moduleUrl);
		}
	}

	private void parseLessonsListPage(String url) {
		Document document = getHTML(url);
		Elements moduleElements = document.getElementsByClass("link title");
		for (Element element : moduleElements) {
			String lessonUrl =
			Objects.requireNonNull(element.attr("abs:href"));
			parseLessonPage(lessonUrl);
		}
	}

	private void parseLessonPage(String url) {

	}

	public void downloadVideo() {

	}


}
