import javafx.util.*;
import org.apache.hc.core5.net.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.devtools.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

public class ChromeDriverHelper {
	private static final String initialUrl = "data:,";

	private Properties properties = PropertiesHelper.getProperties();
	private ChromeDriver driver;
	private DevTools devTools;
	String baseUrl;

	ChromeDriverHelper() {
		baseUrl = properties.getProperty("base_url");
	}

	public void init() {
		String chromeDriverPath = "D:\\Programs\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--window-size=1920,1200", "--silent");
		driver = new ChromeDriver(options);
	}

	public void setCookies(Collection<Cookie> cookies) {
		if (driver.getCurrentUrl().equals(initialUrl)) {
			driver.get(properties.getProperty("page_url_to_set_cookie"));
			while (driver.getCurrentUrl().equals(initialUrl))
				try {
							//noinspection BusyWait
							Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			driver.manage().deleteAllCookies();
		}

		for (Cookie cookie : cookies) {
			driver.manage().addCookie(cookie);
		}
	}

	public InputStream getHtmlStream(String url) {
		driver.get(url);
		return new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8));
	}

	public void goTo(String url) {
		driver.get(url);
	}

	public List<Pair<String, String>> getModuleNameAndlink() {
		List<WebElement> modules = driver.findElements(By.xpath(properties.getProperty("xpath_to_modules")));

		ArrayList<Pair<String, String>> modulesNameAndLink = new ArrayList<>();
		for (WebElement element : modules) {
			String link = element.findElement(By.tagName("a")).getAttribute("href");
			String name = element.findElement(By.tagName("span")).getText();
			modulesNameAndLink.add(new Pair<>(link, name));
		}

		return modulesNameAndLink;
	}

	public List<Pair<String, String>> getLessonNameAndUrl() {
		List<WebElement> lessons = driver.findElements(By.xpath(properties.getProperty("xpath_to_lessons")));

		ArrayList<Pair<String, String>> lessonNameAndLink = new ArrayList<>();
		for (WebElement element : lessons) {
			String link = toAbsoluteUrlIfNot(baseUrl, element.getAttribute("href"));
			String name = element.getText();
			lessonNameAndLink.add(new Pair<>(link, name));
		}

		return lessonNameAndLink;
	}

	public Optional<String> getM3u8TextFileDownloadLink() {
		Optional<WebElement> videoPlayerFrame = driver
			.findElements(By.xpath(properties.getProperty("xpath_to_video_player")))
			.stream().findFirst();
		if (videoPlayerFrame.isEmpty())
			return Optional.empty();

		String videoPlayerUrl = videoPlayerFrame.get().getAttribute("src");
		driver.get(videoPlayerUrl);

		JavascriptExecutor javascriptExecutor = driver;
		return Optional.of(
			(String)javascriptExecutor.executeScript(
				properties.getProperty("javascript_to_video_download_link")));
	}

	public List<Cookie> parseCookies() {
		String cookies = properties.getProperty("cookie");
		String[] cookiesArray = cookies.split("; ");
		ArrayList<Cookie> parsedCookies = new ArrayList<>();
		String[] keyAndValue;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 24 * 7);
		Date now = calendar.getTime();
		for (String cookie : cookiesArray) {
			keyAndValue = cookie.split("=");
			parsedCookies.add(new Cookie(keyAndValue[0], keyAndValue[1], null, null, now, true));
		}

		return parsedCookies;
	}

	public String toAbsoluteUrlIfNot(String baseUrl, String url) {
		try {
			if (new URIBuilder(url).isAbsolute())
				return url;

			URIBuilder builder = new URIBuilder(baseUrl);
			String relativeUrl = URI
				.create(builder.getPath() + "/")
				.resolve("./" + url)
				.getPath();
			return builder.setPath(relativeUrl).build().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
