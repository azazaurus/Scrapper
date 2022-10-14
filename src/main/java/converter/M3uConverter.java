package converter;

import org.apache.commons.io.*;

import java.io.*;
import java.net.*;

public class M3uConverter {
	private final HLSParser hlsParser;
	private final FfmpegClient ffmpegClient;

	public M3uConverter(HLSParser hlsParser, FfmpegClient ffmpegClient) {
		this.hlsParser = hlsParser;
		this.ffmpegClient = ffmpegClient;
	}

	public void convertToMp4(String m3uFilePath, String mp4FilePath) throws IOException {
		var tsFilePath = getTsFilePath(mp4FilePath);
		convertFromM3uToTs(m3uFilePath, tsFilePath);

		convertFromTsToMp4(tsFilePath, mp4FilePath);
	}

	private void convertFromM3uToTs(String m3uFilePath, String tsFilePath) throws IOException {
		var tsFilesUrls = hlsParser.parseFile(new File(m3uFilePath));

		//noinspection ResultOfMethodCallIgnored
		new File(tsFilePath).getParentFile().mkdirs();
		try (var tsFile = new FileOutputStream(tsFilePath, false)) {
			tsFilesUrls.stream()
				.map(M3uConverter::getStream)
				.forEachOrdered(stream -> {
					try {
						stream.transferTo(tsFile);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
	}

	private void convertFromTsToMp4(String tsFilePath, String mp4FilePath) throws IOException {
		ffmpegClient.convert(tsFilePath, mp4FilePath);
	}

	private static InputStream getStream(String url) {
		try {
			return new URL(url).openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getTsFilePath(String mp4FilePath) {
		return FilenameUtils.removeExtension(mp4FilePath) + ".ts";
	}
}
