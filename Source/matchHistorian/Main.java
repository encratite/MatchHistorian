package matchHistorian;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main {
	public static void main(String[] arguments) {
		try {
			String url = "http://www.lolking.net/summoner/euw/1234555";
			Document document = Jsoup.connect(url).get();
			System.out.println(document.html());
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
