package com.fouadchamoun.dormcontrol2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Main controller for the REST client. The request you wish to make can
 * be called by either calling the request() method and passing the type
 * of request you will be to make, or by accessing the methods directly.
 *
 * Uses the RESTException to report errors, but this is extremely basic,
 * and left alone due to the fact people should rather implement their
 * own error codes and systems. You can pass a JSONObject to the Exception
 * by calling createErrorObject().
 *
 * @author 	Isaac Whitfield
 * @version 09/03/2014
 *
 */
public class AndrestClient {

    // The client to use for requests
    DefaultHttpClient client = new DefaultHttpClient();

    /**
     * Main controller for the client, has the ability to create any of the other methods. Call with
     * your connection type and data, and everything is handled for you.
     *
     * @param 	url			the url you wish to call
     * @param 	method		the method you wish to call wish
     * @return 	String		the String returned from the request
     */
    public String request(String url, String method) throws RESTException {
        if (method.matches("GET")) {
            return get(url);
        } else if (method.matches("POST")) {
            return post(url);
        }
        throw new RESTException("Error! Incorrect method provided: " + method);
    }

    /**
     * Main controller for the client, has the ability to create any of the other methods. Call with
     * your connection type and data, and everything is handled for you.
     *
     * @param 	url			the url you wish to call
     * @param 	method		the method you wish to call wish
     * @param   data        the POST headers added to the request
     * @return 	JSONObject	the JSON object returned from the request
     */
    public JSONObject request(String url, String method, Map<String, String> data) throws RESTException {
        if (method.matches("GET")) {
            return get(url, data);
        } else if (method.matches("POST")) {
            return post(url, data);
        }
        throw new RESTException("Error! Incorrect method provided: " + method);
    }


    /**
     * Calls a GET request on a given url. Doesn't take a data object (yet), so pass all get parameters
     * alongside the url.
     *
     * @param 	url		the url you wish to connect to
     * @return 	JSON	the JSON response from the call
     */
    public String get(String url) throws RESTException {
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200){
                throw new Exception("Error executing GET request! Received error code: " + response.getStatusLine().getStatusCode());
            }
            return readInput(response.getEntity().getContent());
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }

    /**
     * Calls a POST request on a given url. Takes a data object in the form of a HashMap to POST.
     *
     * @param 	url		the url you wish to connect to
     * @return 	String	the String response from the call
     */
    public String post(String url) throws RESTException {
        HttpPost request = new HttpPost(url);
        try {
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200){
                throw new Exception("Error executing POST request! Received error code: " + response.getStatusLine().getStatusCode());
            }

            return readInput(response.getEntity().getContent());
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }


    /**
     * Calls a GET request on a given url. Takes a data object in the form of a HashMap to GET.
     *
     * @param 	url		    the url you wish to connect to
     * @return 	JSONObject	the JSON object response from the call
     */
    public JSONObject get(String url, Map<String, String> data) throws RESTException {
        HttpGet request = new HttpGet(url);
        try {
            Iterator<String> it = data.keySet().iterator();
            String name = it.next();
            String value = data.get(name);
            request.addHeader(name, value);
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200){
                throw new Exception("Error executing POST request! Received error code: " + response.getStatusLine().getStatusCode());
            }

            return new JSONObject(readInput(response.getEntity().getContent()));
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }


    /**
     * Calls a POST request on a given url. Takes a data object in the form of a HashMap to POST.
     *
     * @param 	url		the url you wish to connect to
     * @return 	JSONObject	the String response from the call
     */
    public JSONObject post(String url, Map<String, String> data) throws RESTException {
        HttpPost request = new HttpPost(url);
        try {
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200){
                throw new Exception("Error executing POST request! Received error code: " + response.getStatusLine().getStatusCode());
            }

            return new JSONObject(readInput(response.getEntity().getContent()));
        } catch (Exception e) {
            throw new RESTException(e.getMessage());
        }
    }


    /**
     * Generic handler to retrieve the result of a request. Simply reads the input stream
     * and returns the string;
     *
     * @param 	is		the InputStream we're reading, usually from getEntity().getContent()
     * @return	JSON 	the read-in JSON data as a string
     */
    private String readInput(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String json = "", line;
        while ((line = br.readLine()) != null){
            json += line;
        }
        return json;
    }
}