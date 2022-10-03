import java.net.*;
import java.util.*;

public class PageContent {
	String moduleNumber;
	String lessonNumber;
	String lessonTitle;
	String tasks;
	Execution example;
	URL videoDownloadUrl;
	Info additionalInformation;

	PageContent(URL videoDownloadUrl) {
		this.videoDownloadUrl = videoDownloadUrl;
	}
}
