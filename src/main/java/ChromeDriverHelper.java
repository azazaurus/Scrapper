import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class ChromeDriverHelper {
	Properties properties = PropertiesHelper.getProperties();
	WebDriver driver;

	public void set() {
		String chromeDriverPath = "D:\\Programs\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--window-size=1920,1200", "--silent");
		driver = new ChromeDriver(options);
	}

	public void setCookie(String key, String value) {
		driver.get("https://rockvox.ru");
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		driver.manage().addCookie(new Cookie(key, value, "rockvox.ru", "https://rockvox.ru", null));
	}

	public InputStream getHtmlStream(String url) {
		driver.get(url);
		return new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8));
	}

	public List<Cookie> parseCookie() {
		String cookies = properties.getProperty("cookie");
		cookies = cookies.replaceAll(" ", "");
		String[] cookiesArray = cookies.split(";");

		ArrayList<Cookie> parsedCookies = new ArrayList<>();
		String[] keyAndValue;
		for (String cookie : cookiesArray) {
			keyAndValue = cookie.split("=");
			parsedCookies.add(new Cookie(keyAndValue[0], keyAndValue[1]));
		}

		return parsedCookies;
	}
	// Get the login page



	JavascriptExecutor js = (JavascriptExecutor)driver;
}
