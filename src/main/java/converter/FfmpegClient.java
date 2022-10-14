package converter;

import net.bramp.ffmpeg.*;
import net.bramp.ffmpeg.builder.*;

import java.io.*;

public class FfmpegClient {
	private final FFmpeg ffmpeg;

	public FfmpegClient(String pathToFfmpeg) throws IOException {
		ffmpeg = new FFmpeg(pathToFfmpeg);
	}

	public void convert(String inputFilePath, String outputFilePath) throws IOException {
		var ffmpegBuilder = new FFmpegBuilder()
			.setInput(inputFilePath)
			.overrideOutputFiles(true)
			.addOutput(outputFilePath)
			.setAudioCodec("copy")
			.setVideoCodec("copy")
			.done();

		ffmpeg.run(ffmpegBuilder);
	}
}
