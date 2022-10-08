import java.io.*;
import java.net.*;

public class Downloader {
	public static InputStream downloadVideo(URL m3u8TextFileUrl) {
		URL videoDownloadUrl = getVideoUrl(m3u8TextFileUrl);
		InputStream in = null;
		try {
			in = videoDownloadUrl.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return in;
	}

	private static URL getVideoUrl(URL m3u8TextFileUrl) {
		URL videoDownloadUrl = null;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(m3u8TextFileUrl.openStream()))) {
			String urlString = in.lines().skip(6).findFirst().orElseThrow();
			videoDownloadUrl = new URL(urlString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return videoDownloadUrl;
	}
}
