import java.io.*;
import java.util.*;

public class PropertiesHelper {

	public static Properties getProperties() {
		Properties properties;
		try (InputStream input = PropertiesHelper.class.getClassLoader().getResourceAsStream("config.properties")) {

			properties = new Properties();
			properties.load(input);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
}
