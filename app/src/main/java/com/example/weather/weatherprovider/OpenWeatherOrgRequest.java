package com.example.weather.weatherprovider;

import com.example.weather.CityID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OpenWeatherOrgRequest {
    private static final String WEATHER_URL_BEFORE_CURRENT_WEATHER_REQUEST = "https://api.openweathermap.org/data/2.5/weather?id=";
    private static final String WEATHER_URL_BEFORE_FIND_REQUEST = "https://api.openweathermap.org/data/2.5/find?q=";
    private static final String WEATHER_URL_AFTER_CITY = "&units=metric&appid=";
    private static final String WEATHER_API_KEY = "bb0dbb13a7f84df1ca260fe5fcab1320";

    private static boolean httpsCertificateDisabled = false;

    /** Были проблемы с сертификаторм.
     * https://stackoverflow.com/questions/35548162/how-to-bypass-ssl-certificate-validation-in-android-app
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection} This has been created to
     * aid testing on a local box, not for use on production.
     */
    private static void disableSSLCertificateChecking() {
        if ( httpsCertificateDisabled ) return;

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { @Override public boolean verify(String hostname, SSLSession session) { return true; } });
            httpsCertificateDisabled = true;
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String getLines(final BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder(1580);
        String line = null;
        do {
            line = reader.readLine();
            if ( line != null) {
                builder.append(line);
            }
        } while (line != null);
        return builder.toString();
    }

    private static HttpsURLConnection getConnection(final URL url) throws IOException {
        HttpsURLConnection urlConnection;
        disableSSLCertificateChecking();
        urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(1000);
        return urlConnection;
    }

    public static String getWeatherUpdateResponseFor(CityID cityID) throws IOException {
        HttpsURLConnection urlConnection = null;
        try {
            final URL url = new URL(WEATHER_URL_BEFORE_CURRENT_WEATHER_REQUEST
                    + cityID.getId() + WEATHER_URL_AFTER_CITY
                    + WEATHER_API_KEY );
            urlConnection = getConnection(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            return getLines(in);
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    public static String getWeatherSearchResponseFor(String keywords) throws IOException {
        HttpsURLConnection urlConnection = null;
        try {
            String reqKey = keywords;
            if (keywords.length() < 3) {
                reqKey = String.format(Locale.getDefault(), "%-3.3s", keywords);
            }
            final URL url = new URL(WEATHER_URL_BEFORE_FIND_REQUEST
                    + reqKey + WEATHER_URL_AFTER_CITY
                    + WEATHER_API_KEY);
            urlConnection = getConnection(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            return getLines(in);
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }
}
