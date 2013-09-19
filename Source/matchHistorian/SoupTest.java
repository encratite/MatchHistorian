package matchHistorian;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SoupTest {
	public static void runTest() {
		try {
			String region = "euw";
			int summonerId = 19531813;
			
			String url = "http://www.lolking.net/summoner/" + region + "/" + summonerId;
			System.out.println("Downloading data...");
			long downloadStart = System.currentTimeMillis();
			Document document = Jsoup.connect(url).get();
			long downloadDuration = System.currentTimeMillis() - downloadStart;
			System.out.println("Duration: " + downloadDuration + " ms");
			final String dateGameId = "data-game-id";
			Elements gameElements = document.getElementsByAttribute(dateGameId);
			Pattern championIdPattern = Pattern.compile("(\\d+)_\\d+\\.png");
			Pattern durationPattern = Pattern.compile("\\d+");
			SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yy hh:mmaa zzz");
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			int counter = 1;
			for (Element element : gameElements) {
				int gameId = Integer.parseInt(element.attr(dateGameId));
				boolean victory = element.className().equals("match_win");
				Element icon = element.getElementsByClass("summoner_icon_64").first();
				String style = icon.attr("style");
				Matcher championIdMatcher = championIdPattern.matcher(style);
				if(!championIdMatcher.find())
					throw new Exception("Unable to extract champion ID");
				int championId = Integer.parseInt(championIdMatcher.group(1));
				Element gameModeElement = element.getElementsByAttributeValueContaining("style", "font-weight: bold").first();
				String gameMode = gameModeElement.text();
				final String dataHoverSwitch = "data-hoverswitch";
				Element dateElement = element.getElementsByAttribute(dataHoverSwitch).first();
				String dateString = dateElement.attr(dataHoverSwitch);
				Date date = inputDateFormat.parse(dateString);
				String matchDetailCell = "match_details_cell";
				Element durationCell = element.getElementsByClass(matchDetailCell).get(2);
				String durationString = durationCell.child(0).getElementsByTag("strong").first().text();
				Matcher durationMatcher = durationPattern.matcher(durationString);
				if(!durationMatcher.find())
					throw new Exception("Unable to extract duration");
				int duration = Integer.parseInt(durationMatcher.group()) * 60;
				System.out.println(counter + ". Game:");
				System.out.println("ID: " + gameId);
				System.out.println("Victory: " + victory);
				System.out.println("Champion: " + championId);
				System.out.println("Mode: " + gameMode);
				System.out.println("Date: " + outputDateFormat.format(date));
				System.out.println("Duration: " + duration);
				counter++;
			}
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
