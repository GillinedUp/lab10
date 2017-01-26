import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yurii on 26.01.17.
 */
public class ChatBot {

    private String apiID = "";
    private String cityID = "3094802";

    public String getAnswer(String question) {
        switch (question) {
            case "hour":
            case "time":
                return new SimpleDateFormat("HH:mm:ss").format(new Date());
            case "day":
            case "day of the week":
                return new SimpleDateFormat("EE", Locale.ENGLISH)
                        .format(Calendar.getInstance().getTime());
            case "weather":
            case "current weather":
                return getWeather();
            default:
                return "Can't understand the question";
        }
    }

    private String getWeather() {
        String url = "http://api.openweathermap.org/data/2.5/weather?id=" + cityID + "&APPID=" + apiID;
        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url)
                .openStream(), Charset.forName("UTF-8")))) {
            for(String line; (line = bufferedReader.readLine()) != null; ) {
                builder.append(line);
            }
            JSONObject jsonWeather = new JSONObject(builder.toString());
            String result = jsonWeather.getJSONArray("weather").getJSONObject(0).getString("main");
            String temperature = jsonWeather.getJSONObject("main").getDouble("temp") + "";
            double temp = Double.parseDouble(temperature) - 273f;
            return (result + ", " + Double.toString(Math.round(temp*100)/100) + " °C"); // to get precise values
        }
        catch (Exception e) {
            return "Failed to get weather data";
        }
    }
}
