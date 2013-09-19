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
			PrintWriter writer = new PrintWriter("Output/summoner.html");
			writer.write(content);
			writer.close();
			*/
			Pattern gamePattern = Pattern.compile(
					"<div class=\\\"match_(win|loss)\\\" data-game-id=\\\"(\\d+)\\\">.+?" +
					"url\\(\\/\\/lkimg\\.zamimg\\.com\\/shared\\/riot\\/images\\/champions\\/(\\d+)_92\\.png\\).+?" +
					"<div style=\\\"font-size: 12px; font-weight: bold;\\\">(.+?)<\\/div>.+?" +
					"data-hoverswitch=\\\"(\\d+\\/\\d+\\/\\d+ \\d+:\\d+(?:AM|PM) .+?)\\\">.+?" +
					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Kills<\\/span><br \\/>.+?" +
					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Deaths<\\/span><br \\/>.+?" +
					"<strong>(\\d+)</strong> <span style=\\\"color: #BBBBBB; font-size: 10px; line-height: 6px;\\\">Assists<\\/span>.+?" +
					"<strong>(\\d+)\\.(\\d)k</strong><div class=\\\"match_details_cell_label\\\">Gold</div>.+?" +
					"<strong>(\\d+)</strong><div class=\\\"match_details_cell_label\\\">Minions<\\/div>.+?" +
					"url\\(\\/\\/lkimg\\.zamimg\\.com\\/images\\/spells\\/(\\d+)\\.png\\).+?" +
					"url\\(\\/\\/lkimg\\.zamimg\\.com\\/images\\/spells\\/(\\d+)\\.png\\).+?" +
					"<div style=\\\"width: 114px;\\\">(.+?)<button class=\\\"match_show_full_details_btn\\\">",
					Pattern.DOTALL
			);
			Pattern itemPattern = Pattern.compile(
					"<div style=\\\"display: table-cell; padding: 2px; width: 34px; height: 34px;\\\">\\s+" +
					"(<div class=\\\"icon_32\\\" style=\\\"background: url\\(\\/\\/lkimg\\.zamimg\\.com\\/shared\\/riot\\/images\\/items\\/\\d+_32\\.png\\);\\\"><a href=\\\"\\/items\\/(\\d+)\\\"><\\/a><\\/div>)?" +
					"\\s+<\\/div>"
				);
			Matcher gameMatcher = gamePattern.matcher(content);
			SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa zzz");
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			int counter = 1;
			while(gameMatcher.find()) {
				try {
					int group = 1;
					String winLoss = gameMatcher.group(group++);
					boolean win = winLoss.equals("win");
					String gameIdString = gameMatcher.group(group++);
					int gameId = Integer.parseInt(gameIdString);
					String championIdString = gameMatcher.group(group++);
					int championId = Integer.parseInt(championIdString);
					String mode = gameMatcher.group(group++);
					String dateString = gameMatcher.group(group++);
					Date date = inputDateFormat.parse(dateString);
					String killsString = gameMatcher.group(group++);
					int kills = Integer.parseInt(killsString);
					String deathsString = gameMatcher.group(group++);
					int deaths = Integer.parseInt(deathsString);
					String assistsString = gameMatcher.group(group++);
					int assists = Integer.parseInt(assistsString);
					String goldIntegerString = gameMatcher.group(group++);
					int goldInteger = Integer.parseInt(goldIntegerString);
					String goldFractionString = gameMatcher.group(group++);
					int goldFraction = Integer.parseInt(goldFractionString);
					int gold = goldInteger * 1000 + goldFraction * 100;
					String minionsString = gameMatcher.group(group++);
					int minions = Integer.parseInt(minionsString);
					String summoner1String = gameMatcher.group(group++);
					int summoner1 = Integer.parseInt(summoner1String);
					String summoner2String = gameMatcher.group(group++);
					int summoner2 = Integer.parseInt(summoner2String);
					String itemsString = gameMatcher.group(group++);
					Matcher itemMatcher = itemPattern.matcher(itemsString);
					int[] items = new int[6];
					for(int i = 0; i < items.length; i++) {
						if(!itemMatcher.find())
							throw new Exception("Invalid item count");
						String itemString = itemMatcher.group(2);
						int item;
						if(itemString == null)
							item = 0;
						else
							item = Integer.parseInt(itemString);
						items[i] = item;
					}
					System.out.println("Game " + counter + ":");
					System.out.println("Win: " + win);
					System.out.println("Game ID: " + gameId);
					System.out.println("Champion ID: " + championId);
					System.out.println("Mode: " + mode);
					System.out.println("Date: " + outputDateFormat.format(date));
					System.out.println("K/D/A: " + kills + "/" + deaths + "/" + assists);
					System.out.println("Gold: " + gold);
					System.out.println("Minions: " + minions);
					System.out.println("Summoners: " + summoner1 + ", " + summoner2);
					System.out.print("Items:");
					for(int item : items) {
						System.out.print(" " + item);
					}
					System.out.println("");
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
