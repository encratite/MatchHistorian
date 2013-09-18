package matchHistorian;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Test {
	static String httpDownload(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		InputStreamReader stream = new InputStreamReader(connection.getInputStream());
		char[] buffer = new char[4096];
		StringBuilder builder = new StringBuilder();
		while(true) {
			int bytesRead = stream.read(buffer, 0, buffer.length);
			if(bytesRead <= 0)
				break;
			builder.append(buffer, 0, bytesRead);
		}
		String output = builder.toString();
		return output;
	}
	
	public static void httpTest() {
		try {
			System.out.println("Downloading data...");
			long start = System.currentTimeMillis();
			String content = httpDownload("http://www.lolking.net/summoner/euw/19531813");
			long duration = System.currentTimeMillis() - start;
			System.out.println("Length: " + content.length());
			System.out.println("Duration: " + duration + " ms");
			/*
			PrintWriter writer = new PrintWriter("output.html");
			writer.write(content);
			writer.close();
			*/
			Pattern pattern = Pattern.compile(
					"<div class=\\\"match_(win|loss)\\\" data-game-id=\\\"(\\d+)\\\">.+?" +
					"url\\(\\/\\/lkimg\\.zamimg\\.com\\/shared\\/riot\\/images\\/champions\\/(\\d+)_92\\.png\\).+?" +
					"<div style=\\\"font-size: 12px; font-weight: bold;\\\">(.+?)<\\/div>.+?" +
					"data-hoverswitch=\\\"(\\d+\\/\\d+\\/\\d+ \\d+:\\d+(?:AM|PM) .+?)\\\">",
					Pattern.DOTALL
			);
			Matcher matcher = pattern.matcher(content);
			SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa zzz");
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			int counter = 1;
			while(matcher.find()) {
				try {
					String winLoss = matcher.group(1);
					boolean win = winLoss.equals("win");
					String gameIdString = matcher.group(2);
					int gameId = Integer.parseInt(gameIdString);
					String championIdString = matcher.group(3);
					int championId = Integer.parseInt(championIdString);
					String mode = matcher.group(4);
					String dateString = matcher.group(5);
					Date date = inputDateFormat.parse(dateString);
					System.out.println("Game " + counter + ":");
					System.out.println("Win: " + win);
					System.out.println("Game ID: " + gameId);
					System.out.println("Champion ID: " + championId);
					System.out.println("Mode: " + mode);
					// System.out.println("Date string: " + dateString);
					System.out.println("Date: " + outputDateFormat.format(date));
					// System.out.println("Offset: " + matcher.start() + ", " + matcher.end());
				}
				catch(NumberFormatException exception) {
				}
				counter++;
			}
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
