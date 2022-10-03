import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class ChromeDriverHelper {
	private static final String initialUrl = "data:,";

	Properties properties = PropertiesHelper.getProperties();
	WebDriver driver;

	public void set() {
		String chromeDriverPath = "D:\\Programs\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--window-size=1920,1200", "--silent");
		driver = new ChromeDriver(options);
	}

	public void setCookie(Cookie cookie) {
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

		driver.manage().addCookie(cookie);
	}

	public InputStream getHtmlStream(String url) {
		driver.get(url);
		return new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8));
	}

	public List<Cookie> parseCookie() {
		String cookies = properties.getProperty("cookie");
		String[] cookiesArray = cookies.split("; ");
		ArrayList<Cookie> parsedCookies = new ArrayList<>();
		String[] keyAndValue;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 30);
		Date now = calendar.getTime();
		for (String cookie : cookiesArray) {
			keyAndValue = cookie.split("=");
			parsedCookies.add(new Cookie(keyAndValue[0], keyAndValue[1], null, null, now, true));
		}

		return parsedCookies;
	}
	// Get the login page



	JavascriptExecutor js = (JavascriptExecutor)driver;
}
