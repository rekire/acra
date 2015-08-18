/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package org.acra.util;

import android.content.Context;

import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public final class HttpRequest {



    private String login;
    private String password;
    private int connectionTimeOut = 3000;
    private int socketTimeOut = 3000;
    private int maxNrRetries = 3;
    private Map<String,String> headers;
    
    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public void setSocketTimeOut(int socketTimeOut) {
        this.socketTimeOut = socketTimeOut;
    }

    public void setHeaders(Map<String,String> headers) {
       this.headers = headers;
    }

    
    /**
     * The default number of retries is 3.
     * 
     * @param maxNrRetries
     *            Max number of times to retry Request on failure due to
     *            SocketTimeOutException.
     */
    public void setMaxNrRetries(int maxNrRetries) {
        this.maxNrRetries = maxNrRetries;
    }

    /**
     * Posts to a URL.
     * 
     *
     * @param context   Android context for which to create the SocketFactory.
     * @param url       URL to which to post.
     * @param content   Map of parameters to post to a URL.
     * @throws IOException if the data cannot be posted.
     */
    public void send(Context context, URL url, Method method, String content, Type type) throws IOException {


    }


    /**
     * Converts a Map of parameters into a URL encoded Sting.
     * 
     * @param parameters
     *            Map of parameters to convert.
     * @return URL encoded String representing the parameters.
     * @throws UnsupportedEncodingException
     *             if one of the parameters couldn't be converted to UTF-8.
     */
    public static String getParamsAsFormString(Map<?, ?> parameters) throws UnsupportedEncodingException {

        final StringBuilder dataBfr = new StringBuilder();
        for (final Object key : parameters.keySet()) {
            if (dataBfr.length() != 0) {
                dataBfr.append('&');
            }
            final Object preliminaryValue = parameters.get(key);
            final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
            dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"));
            dataBfr.append('=');
            dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }

        return dataBfr.toString();
    }
}