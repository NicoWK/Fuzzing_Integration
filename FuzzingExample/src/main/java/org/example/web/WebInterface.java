package org.example.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.database.Database;
import org.example.model.User;
import org.example.vulnerabilities.SerializationHelper;
import org.example.serialization.VulnerableObject;
import org.example.vulnerabilities.*;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico Werner Keller
 * @version 1.0
 * @since 15.01.2023
 * <p>
 * The WebInterface class provides the web-based User-Interface
 */
public class WebInterface {

    private Database db;
    private User user;
    private boolean loggedIn;

    public WebInterface(Database db) {
        this.db = db;
        db.connect();
        this.user = new User();


    }

    // Constructor to create new db
    public WebInterface() {
        this(new Database());
    }

    /**
     * The startServer method starts a web server on localhost at port 8081.
     * It creates a new HttpServer instance and sets up several contexts for handling different requests.
     * Each of these contexts is handled by a different instance of a class that implements the
     * HttpHandler interface. The server's executor is set to null. If an IOException is thrown, it is
     * caught and the stack trace is printed.
     */
    public void startServer() throws IOException {
        System.out.println("Starting Webserver...");
        HttpServer server;
        server = HttpServer.create(new InetSocketAddress("localhost", 8081), 0);
        //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        WebInterface webInterface = new WebInterface();
        server.createContext("/", webInterface.new IndexHttpHandler());
        server.createContext("/login", webInterface.new LoginHttpHandler());
        server.createContext("/register", webInterface.new LoginHttpHandler());
        server.createContext("/logout", webInterface.new LoginHttpHandler());
        server.createContext("/changePassword", webInterface.new LoginHttpHandler());
        server.createContext("/command", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/resourceConsumption", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/nullDereference", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/pathTraversal", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/inputValidation", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/deserialization", webInterface.new VulnerabilityHttpHandler());
        server.createContext("/wraparound", webInterface.new VulnerabilityHttpHandler());
        //server.setExecutor(threadPoolExecutor);
        server.setExecutor(null);
        server.start();
        System.out.println(" Server started on port 8081");
    }

    /**
     * The handleResponse method handles the HTTP response for a given HttpExchange.
     * It sets the HTTP status code to 200 and calculates the fixed response body length. Subsequently, it sets the response body to the provided htmlResponse.
     * It writes the htmlResponse to the OutputStream of the HttpExchange, flushes the OutputStream,
     * and then closes it.
     *
     * @param httpExchange The HttpExchange object representing the current request/response
     * @param htmlResponse The response body as a String in HTML format
     * @throws IOException
     */
    protected void handleResponse(HttpExchange httpExchange, String htmlResponse) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        int l = htmlResponse.getBytes(Charset.forName("UTF-8")).length;
        httpExchange.sendResponseHeaders(200, l);
        outputStream.write(htmlResponse.getBytes(Charset.forName("UTF-8")));
        outputStream.flush();
        outputStream.close();
    }

    /**
     * The method paramsToMap converts a query or POST body String into a map of
     * key-value pairs. It uses '&' as the separator between different query parameters and '=' as the
     * separator between a key and its value. If the query string is null, it will return an empty map.
     * If a query parameter does not have a value, it will be stored as an empty string in the map.
     * If there is an UnsupportedEncodingException, it will be thrown as a RuntimeException.
     *
     * @param paramString A String with query parameters in the following form: paramString = field1=value1&field2=value2&field3=value3...
     * @return A map containing key-value pairs of the query parameters
     */
    //
    public static Map<String, String> paramsToMap(String paramString) {
        Map<String, String> result = new HashMap<>();
        // No query parameters are provided
        if (paramString == null)
            return result;
        // index of the start character of the query param
        int startIndex = 0;
        // index of the end character of the query param
        int endIndex;
        // total length of query string
        int length = paramString.length();
        // while the start of the (last/next) query param does not occur after the actual end of the query String
        while (startIndex < length) {
            // save index of '&' symbol occurrence (end of query param)
            endIndex = paramString.indexOf('&', startIndex);
            // '&' symbol does not occur (just one query param)
            if (endIndex == -1) {
                endIndex = length;
            }
            // index of the start of query param is bigger than the index of the end of the query param
            if (endIndex > startIndex) {
                // save index of the '='-symbol
                int equalIndex = paramString.indexOf('=', startIndex);
                try {
                    // if there is an '='-symbol and it has a higher index than the start of the query param
                    // which means there is a key without a value (missing '='-symbol)
                    if (equalIndex == (-1) || equalIndex > endIndex) {
                        //save the empty string with the provided key
                        result.put(URLDecoder.decode(paramString.substring(startIndex, endIndex), "utf-8"), "");
                    }
                    // else there is a value for that key
                    else {
                        // save the value for that provided key
                        result.put(URLDecoder.decode(paramString.substring(startIndex, equalIndex), "utf-8"), URLDecoder.decode(paramString.substring(equalIndex + 1, endIndex), "utf-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // should not happen, because utf-8 support is mandatory for java
                }
            }
            // the start of the next query param is the next character after the end of the last query param
            startIndex = endIndex + 1;
        }
        return result;
    }

    /**
     * Class implementing the HTTPHandler to handle the login and registration process
     */
    public class LoginHttpHandler implements HttpHandler {
        /**
         * The handle method handles HTTP requests for the server. It checks the request method and URI to determine
         * the appropriate action to take. It handles POST and GET request to the following URIs:
         * /login, /register, /changePassword and /logout
         *
         * @param exchange The HttpExchange object representing the current request/response.
         * @throws IOException If an error occurs while reading or writing to the request/response stream.
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = new HashMap<>();
            // if the request is a POST-Request
            if ("POST".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/login")) {
                // read the Request Body
                StringBuilder buffer = new StringBuilder();
                if (exchange.getRequestBody() != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        buffer.append(line);
                    }
                    String data = buffer.toString();
                    // Convert to Map
                    params = paramsToMap(data);
                }
                // if request contains the User to be logged in
                if (params.containsKey("username") && params.containsKey("password")) {
                    handleResponse(exchange, getLoggedinPage(params));
                } else {
                    handleResponse(exchange, getLoginPage());
                }
                // if the request is a GET-Request
            } else if ("POST".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/register")) {
                // read the Request Body
                StringBuilder buffer = new StringBuilder();
                if (exchange.getRequestBody() != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        buffer.append(line);
                    }
                    String data = buffer.toString();
                    // Convert to Map
                    params = paramsToMap(data);
                }
                // if request contains the User to be logged in
                if (params.containsKey("username") && params.containsKey("password") && params.containsKey("password2")) {
                    handleResponse(exchange, getRegisteredPage(params));
                } else {
                    handleResponse(exchange, getRegisterPage());
                }


                // if the request is a GET-Request
            } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/changePassword")) {
                if (loggedIn) {
                    handleResponse(exchange, getChangePasswordPage());
                }
            } else if ("POST".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/changePassword")) {
                // read the Request Body
                if (loggedIn) {
                    StringBuilder buffer = new StringBuilder();
                    if (exchange.getRequestBody() != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            buffer.append(line);
                        }
                        String data = buffer.toString();
                        // Convert to Map
                        params = paramsToMap(data);
                    }
                    // if request contains the User to be logged in
                    if (params.containsKey("oldPassword") && params.containsKey("newPassword") && params.containsKey("newPassword2")) {
                        handleResponse(exchange, getPasswordChangedPage(params));
                    } else {
                        handleResponse(exchange, getChangePasswordPage());
                    }
                }
            } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/logout")) {
                if (loggedIn) {
                    //set loggedIn to false: verifies that the user could not execute logged-in features
                    loggedIn = false;
                    //delete userdata
                    user.setPassword(null);
                    user.setUsername(null);
                    handleResponse(exchange, getLoggedOutPage());
                }
            }
        }

        /**
         *  The validateUsername Methode checks if the provided username consist of valid characters (A-Z, a-z, 0-9, _) and has a length between 1 and 32 characters
         * @param username the username to be checked
         * @return true, if username is valid; false, else
         */
        protected boolean validateUsername(String username){
            if (username.isEmpty()) {
                return false;
            }
            if (username.length() >= 32) {
                return false;
            }
            if (!username.matches("^[a-zA-Z0-9_]*$")) {
                return false;
            }
            return true;
        }

        /**
         * The getLoginPage method generates an HTML string for a login form
         *
         * @return A string containing the HTML for the login form
         */
        private String getLoginPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "    <h2>Login: </h2>" +
                    "    <div>\n" +
                    "      <label for=\"name\">Username: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"username\" id=\"username\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password\">Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password\" id=\"password\">\n" +
                    "    </div>\n" +
                    "    </div>\n" +
                    "      <input type=\"submit\" value=\"Login\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getLoggedinPage method generates an HTML string for a logged-in page or the login form with an error message.
         * It uses the username and password provided in the params map to check if the user exists in the database
         * using the exists method of the db object.
         *
         * @param params A map containing the key-value pairs of the request parameters
         * @return A string containing the HTML
         */
        private String getLoggedinPage(Map<String, String> params) {
            StringBuilder htmlBuilder = new StringBuilder();
            //login logic
            ResultSet resultSet = db.exists(params.get("username"), params.get("password"));
            try {
                if (resultSet.next()) {
                    user.setUsername(params.get("username").toLowerCase());
                    user.setPassword(params.get("password"));
                    loggedIn = true;
                    htmlBuilder = new StringBuilder();
                    htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                            "  <div><span>").append("You are logged in as: ").append(params.get("username")).append("</span></div>" +
                            "  <div>\n" +
                            "    <form action=\"/index\" method=\"post\">" +
                            "      <input type=\"submit\" value=\"Back\" name=\"back\"/>  " +
                            "    </form>" +
                            "</body></html>");
                    return htmlBuilder.toString();
                }

            } catch (SQLException ex) {

            }
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "    <span>Wrong credentials, try again!</span>\n" +
                    "  </div>\n" +
                    "  <div>\n" +
                    "    <h2>Login: </h2>" +
                    "    <div>\n" +
                    "      <label for=\"name\">Username: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"username\" id=\"username\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password\">Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password\" id=\"password\">\n" +
                    "    </div>\n" +
                    "    </div>\n" +
                    "      <input type=\"submit\" value=\"Login\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getLoggedOutPage method generates an HTML string for the logout screen.
         *
         * @return A string containing the HTML
         */
        private String getLoggedOutPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                    "  <div><span>").append("You are logged out!").append("</span></div>" +
                    "  <div>\n" +
                    "    <form action=\"/index\" method=\"post\">" +
                    "      <input type=\"submit\" value=\"Back\" name=\"back\"/>  " +
                    "    </form>" +
                    "</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getRegisterPage method generates an HTML string for a registration form.
         *
         * @return A string containing the HTML.
         */
        private String getRegisterPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "    <h2>Register: </h2>" +
                    "    <div>\n" +
                    "      <label for=\"name\">Username: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"username\" id=\"username\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password\">Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password\" id=\"password\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password2\">Retype Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password2\" id=\"password2\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Register\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         *  The getRegisteredPage method generates an HTML string after checking the provided parameters, which contain the
         *  username, password and the repeated password as a Map. The user will be created in the database if the passwords
         *  match and the user does not exist yet, otherwise an error message will be displayed.
         * @param params A map containing the key-value pairs of the request parameters
         * @return A string containing the HTML.
         */
        private String getRegisteredPage(Map<String, String> params) {
            StringBuilder htmlBuilder = new StringBuilder();
            String output = "";
            //register logic
            if (validateUsername(params.get("username"))) {
                if (params.get("password").equals(params.get("password2"))) {
                    User newUser = new User();
                    newUser.setUsername(params.get("username"));
                    newUser.setPassword(params.get("password"));
                    db.create(newUser);
                    output = "User " + params.get("username") + " is registered";
                    htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                            "  <div><span>").append(output).append("</span></div>" +
                            "  <div>\n" +
                            "    <form action=\"/login\" method=\"post\">" +
                            "      <input type=\"submit\" value=\"Login\" name=\"login\"/>  " +
                            "    </form>" +
                            "  </div>" +
                            "</body></html>");
                    return htmlBuilder.toString();
                } else {
                    output = "passwords do not match";
                }
            } else {
                output = "Username might be too long (maximum 32 characters) or consists of invalid characters (only letters, numbers and '_' are allowed)";
            }
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "    <h2>Register: </h2>" +
                    "    <div>\n" +
                    "      <label for=\"name\">Username: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"username\" id=\"username\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password\">Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password\" id=\"password\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password2\">Retype Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"password2\" id=\"password2\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Register\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <span style=\"color:red\">"+output+"</span>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();

        }

        /**
         * The getChangePasswordPage method generates an HTML string for a form to change the password.
         *
         * @return A string containing the HTML
         */
        private String getChangePasswordPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "    <h2>Change Password: </h2>" +
                    "    <div>\n" +
                    "      <label for=\"password\">Old Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"oldPassword\" id=\"oldPassword\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password2\">New Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"newPassword\" id=\"newPassword\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"password2\">Retype new Password: </label>\n" +
                    "      <input size=\"100\" type=\"password\" name=\"newPassword2\" id=\"newPassword2\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Change Password\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getRegisteredPage method generates an HTML string for a page confirming the registration of a new user or an error message.
         * It uses the username, password and password2 provided in the params map to check if the passwords match. If the passwords match,
         * it creates a new user object, sets its properties and adds it to the database by calling the create method of the db object.
         * Then it returns an HTML page with a message saying the user is registered and a button to redirect to the login page.
         *
         * @param params A map containing the key-value pairs of the request parameters.
         * @return A string containing the HTML.
         */


        /**
         * The method getPasswordChangedPage handles the password change request of a user.
         * It checks if the provided old password matches the password of the currently logged-in user
         * and if the new password inputs are identical. It also checks if the username meets the requirements (see validateUsername). If both conditions are met, it updates the user's
         * password in the database. Based on the result it returns an HTML page indicating that the password
         * change was successful or failed
         *
         * @param params A map containing the key-value pairs of the request parameters.
         * @return A string containing the HTML.
         */
        private String getPasswordChangedPage(Map<String, String> params) {
            StringBuilder htmlBuilder = new StringBuilder();
            //register logic
            // if user provides the correct old password and two identical new passwords
            if (params.get("oldPassword").equals(user.getPassword()) && params.get("newPassword").equals(params.get("newPassword2"))) {
                User oldUser = user;
                User newUser = new User();
                newUser.setPassword(params.get("newPassword"));
                newUser.setUsername(user.getUsername().toLowerCase());
                // update the user in database
                if (db.update(newUser, oldUser)) {
                    //set the new password to sessionUser
                    user = newUser;
                    htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                            "  <div><span>").append(user.getUsername() + "'s Password is changed").append("</span></div>" +
                            "  <div>\n" +
                            "    <form action=\"/index\" method=\"post\">" +
                            "      <input type=\"submit\" value=\"Back\" name=\"back\"/>  " +
                            "    </form>" +
                            "  </div>" +
                            "</body></html>");
                    //return if everything went well
                    return htmlBuilder.toString();
                }
                // user does not provide the correct old password or two identical new passwords
            }
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                    "  <div><span>").append(params.get("username") + "'s Password could not be changed").append("</span></div>" +
                    "  <div>\n" +
                    "    <form action=\"/changePassword\" method=\"post\">" +
                    "      <input type=\"submit\" value=\"Try again\" name=\"changePassword\"/>  " +
                    "    </form>" +
                    "  </div>" +
                    "</body></html>");
            return htmlBuilder.toString();
        }
    }

    /**
     * Class implementing the HTTPHandler to handle the redirection to the index page
     */
    public class IndexHttpHandler implements HttpHandler {
        @Override
        /**
         * The handle method handles HTTP requests for the server. It checks the request method and URI to determine
         * the appropriate action to take. It handles POST and GET request to the index page:
         */
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleResponse(exchange, getIndexPage());
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                handleResponse(exchange, getIndexPage());
            }
        }

        /**
         * The getChangePasswordPage method generates an HTML string for the index page based on the
         * loggedIn value. If the user is loggedIn, some more features are available
         *
         * @return A String containing the HTML.
         */
        private String getIndexPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            if (loggedIn) {
                htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                        "  <div>\n" +
                        "<h2>Welcome to the vulnerable Application!</h2>" +
                        "  </div>\n" +
                        "  <div>\n" +
                        "    <form action=\"/logout\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Logout\"/>  " +
                        "    </form>" +
                        "    <form action=\"/changePassword\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Change Password\"/>  " +
                        "    </form>" +
                        "    <form action=\"/command\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Command Injection\"/>  " +
                        "    </form>" +
                        "    <form action=\"/resourceConsumption\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Resource Consumption\"/>  " +
                        "    </form>" +
                        "    <form action=\"/nullDereference\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Null Pointer Dereference\"/>  " +
                        "    </form>" +
                        "    </form>" +
                        "    <form action=\"/wraparound\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Integer Wraparound\"/>  " +
                        "    </form>" +
                        "    <form action=\"/pathTraversal\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Path Traversal\"/>  " +
                        "    </form>" +
                        "    <form action=\"/deserialization\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Deserialization of untrusted data\"/>  " +
                        "    </form>" +
                        "    <form action=\"/inputValidation\" method=\"get\">" +
                        "      <input type=\"submit\" value=\"Improper Input Validation\"/>  " +
                        "    </form>" +
                        "  </div>\n").append("</body></html>");
            } else {
                htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                        "  <div>\n" +
                        "<h2>Welcome to the vulnerable Application!</h2>" +
                        "  </div>\n" +
                        "  <div>\n" +
                        "    <form action=\"/login\" method=\"post\">" +
                        "      <input type=\"submit\" value=\"Login\" name=\"login\"/>  " +
                        "    </form>" +
                        "    <form action=\"/register\" method=\"post\">" +
                        "      <input type=\"submit\" value=\"Register\" name=\"register\"/>  " +
                        "    </form>" +
                        "  </div>\n").append("</body></html>");
            }
            return htmlBuilder.toString();
        }
    }


    /**
     * The VulnerabilityHttpHandler class implements the HttpHandler and handles different types of vulnerabilities.
     * It checks if the user is logged in and based on the request method and URI, it calls the appropriate method to handle the request and generate the response.
     */
    public class VulnerabilityHttpHandler implements HttpHandler {
        @Override
        /**
         * The handle method handles HTTP requests for the server. It checks the request method and URI to determine
         * the appropriate action to take. It handles POST and GET request to the following URIs:
         * /command, /resourceConsumption, /changePassword, /nullDereference, /pathTraversal, /deserialization and
         * /inputValidation which represent the security vulnerabilities built into this application.
         *
         * @param exchange The HttpExchange object representing the current request/response.
         * @throws IOException if an I/O error occurs
         */
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = null;
            StringBuilder htmlbuilder = new StringBuilder();
            if (loggedIn) {
                if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/command") && !exchange.getRequestURI().getQuery().contains("domain") && !exchange.getRequestURI().getQuery().contains("parameter")) {
                    handleResponse(exchange, getCommandPage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/command") && ( exchange.getRequestURI().getQuery().contains("domain") || exchange.getRequestURI().getQuery().contains("parameter") )) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    handleResponse(exchange, getCommandResponsePage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/resourceConsumption") && !exchange.getRequestURI().getQuery().contains("days") && !exchange.getRequestURI().getQuery().contains("dailyCost")) {
                    handleResponse(exchange, getResourceConsumptionPage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/resourceConsumption") && exchange.getRequestURI().getQuery().contains("days") && exchange.getRequestURI().getQuery().contains("dailyCost")) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    //verify input is numeric
                    try {
                        Integer.parseInt(params.get("days"));
                        Integer.parseInt(params.get("dailyCost"));
                    } catch (NumberFormatException e) {
                        handleResponse(exchange, getResourceConsumptionPage());
                        return;
                    }
                    handleResponse(exchange, getResourceConsumptionResultPage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/nullDereference") && !exchange.getRequestURI().getQuery().contains("usernames")) {
                    handleResponse(exchange, getNullDereferencePage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/nullDereference") && exchange.getRequestURI().getQuery().contains("usernames")) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    handleResponse(exchange, getNullDereferenceResponsePage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/pathTraversal") && !exchange.getRequestURI().getQuery().contains("content") && !exchange.getRequestURI().getQuery().contains("filename") && !exchange.getRequestURI().getQuery().contains("fileDownload") && !exchange.getRequestURI().getQuery().contains("fileDelete")) {
                    handleResponse(exchange, getPathTraversalPage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/pathTraversal") && ((exchange.getRequestURI().getQuery().contains("content") && exchange.getRequestURI().getQuery().contains("filename")) || exchange.getRequestURI().getQuery().contains("fileDownload") || exchange.getRequestURI().getQuery().contains("fileDelete"))) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    handleResponse(exchange, getPathTraversalResponsePage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/wraparound") && !exchange.getRequestURI().getQuery().contains("initialCapital") && !exchange.getRequestURI().getQuery().contains("monthlySavings") && !exchange.getRequestURI().getQuery().contains("investmentPeriod") && !exchange.getRequestURI().getQuery().contains("annualInterestRate")) {
                    handleResponse(exchange, getIntegerWraparoundPage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/wraparound") && exchange.getRequestURI().getQuery().contains("initialCapital") && exchange.getRequestURI().getQuery().contains("monthlySavings") && exchange.getRequestURI().getQuery().contains("investmentPeriod") && exchange.getRequestURI().getQuery().contains("annualInterestRate")) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    handleResponse(exchange, getIntegerWraparoundResponsePage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/deserialization")) {
                    handleResponse(exchange, getDeserializationPage());
                } else if ("POST".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/deserialization")) {
                    if (exchange.getRequestBody() != null) {
                        StringBuilder buffer = new StringBuilder();
                        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                        String line;
                        while ((line = br.readLine()) != null) {
                            buffer.append(line);
                        }
                        String data = buffer.toString();
                        // Convert to Map
                        params = paramsToMap(data);
                    }
                    handleResponse(exchange, getDeserializationResponsePage(params));
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/inputValidation") && !exchange.getRequestURI().getQuery().contains("rounds")) {
                    handleResponse(exchange, getInputValidationPage());
                } else if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/inputValidation") && exchange.getRequestURI().getQuery().contains("rounds")) {
                    params = paramsToMap(exchange.getRequestURI().getQuery());
                    handleResponse(exchange, getInputValidationResponsePage(params));
                } else {
                    handleResponse(exchange, htmlbuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                            "<div>\n" +
                            "<h1>404</h1>\n" +
                            "<h2>Page Not Found</h2>\n" +
                            "<p>The Page you are looking for doesn't exist or an other error occurred. Click <a href=\"/\">here</a> to go back.</p>\n" +
                            "</div>\n" +
                            "</body></html>").toString());
                }
            } else {
                handleResponse(exchange, htmlbuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                        "<div>\n" +
                        "<h1>404</h1>\n" +
                        "<h2>Page Not Found</h2>\n" +
                        "<p>The Page you are looking for doesn't exist or an other error occurred. Click <a href=\"/\">here</a> to go back.</p>\n" +
                        "</div>\n" +
                        "</body></html>").toString());
            }
        }

        /**
         * The getCommandPage method generates an HTML string for the subpage where the command injection can be executed. It provides a form to
         * enter a domain whose reachability is to be checked afterwards.
         * @return A string containing the HTML.
         */
        private String getCommandPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                    "  <div>\n" +
                    "    <h2>Ping: </h2>\n" +
                    "    <span>You can test the reachability of a website with this tool (the input is resolved as a parameter of the ping command)</span>\n" +
                    "  </div>\n" +
                    "  <form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <label for=\"parameter\">Domain: </label>\n" +
                    "    <input size=\"100\" type=\"text\" name=\"parameter\" id=\"parameter\">\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Check\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "  </form>\n" +
                    "  <div>\n" +
                    "    <span>You can test the reachability of a website with this tool (the input is appended as a string parameter of the bash executing the ping command)</span>\n" +
                    "  </div>\n" +
                    "  <form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <label for=\"domain\">Domain: </label>\n" +
                    "    <input size=\"100\" type=\"text\" name=\"domain\" id=\"domain\">\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Check\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "  </form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getCommandResponsePage handles the command injection by passing the user input to a method of the {@link CommandExecution} class without validating.
         * The user input should be a domain to ping.
         * @param params A map containing the key-value pairs of the request parameters ,in this case the domain.
         * @see CommandExecution
         *
         * @return A string containing the HTML.
         */
        private String getCommandResponsePage(Map<String, String> params) {
            String result="";
            CommandExecution commandExecution = new CommandExecution();
            if (params.containsKey("domain")){
                String domain = params.get("domain");
                result = commandExecution.executePing(domain);
            } else if (params.containsKey("parameter")){
                String parameter = params.get("parameter");
                result = commandExecution.execute("ping -c 4 " +parameter);
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                            "  <div>\n" +
                            "    <h2>Ping: </h2>\n" +
                            "    <span>You can test the reachability of a website with this tool (the input is resolved as a parameter of the ping command)</span>\n" +
                            "  </div>\n" +
                            "  <form action=\"\" method=\"get\">\n" +
                            "  <div>\n" +
                            "    <label for=\"parameter\">Domain: </label>\n" +
                            "    <input size=\"100\" type=\"text\" name=\"parameter\" id=\"parameter\">\n" +
                            "    <div>\n" +
                            "      <input type=\"submit\" value=\"Check\">\n" +
                            "    </div>\n" +
                            "  </div>\n" +
                            "  </form>" +
                            "  <div>\n" +
                            "    <span>You can test the reachability of a website with this tool (the input is appended as a string parameter of the bash executing the ping command)</span>\n" +
                            "  </div>\n" +
                            "  <form action=\"\" method=\"get\">\n" +
                            "  <div>\n" +
                            "    <label for=\"domain\">Domain: </label>\n" +
                            "    <input size=\"100\" type=\"text\" name=\"domain\" id=\"domain\">\n" +
                            "    <div>\n" +
                            "      <input type=\"submit\" value=\"Check\">\n" +
                            "    </div>\n" +
                            "  </div>\n" +
                            "  </form>\n" +
                    "    </div>\n" +
                    "    <pre><code>" + result + "</code></pre>\n" +
                    "  </div>\n").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getResourceConsumptionPage method generates an HTML string for the subpage where the resource consumption can be triggered. It provides a form to
         * enter the number of days and the daily cost to calculate the total cost recursive.
         *
         * @return A string containing the HTML.
         */
        private String getResourceConsumptionPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Uncontrolled Resource Consumption: </h2>" +
                    "    <span>A recursive method is called, which calculates the total cost from the inputs daily cost and days. </span>" +
                    "    <div>\n" +
                    "      <label for=\"dailyCost\">Daily Cost: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"dailyCost\" id=\"dailyCost\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"days\">Days: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"days\" id=\"days\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Calculate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getResourceConsumptionResultPage handles the resource consumption vulnerability by passing the user input to a method of the {@link ResourceConsumption} class without validating.
         * The user input should be the days and the daily cost, from which the total cost are recursively calculated (may cause a StackOverflow).
         *
         * @see ResourceConsumption
         * @param params A map containing the key-value pairs of the request parameters, in this case the days and the dailyCost.
         * @return A string containing the HTML.
         */
        private String getResourceConsumptionResultPage(Map<String, String> params) {
            StringBuilder htmlBuilder = new StringBuilder();
            //String result;
            int days, dailyCost, result;
            ResourceConsumption resourceConsumption = new ResourceConsumption();
            days = Integer.parseInt(params.get("days"));
            dailyCost = Integer.parseInt(params.get("dailyCost"));
            result = resourceConsumption.calculateCostRekursive(days, dailyCost);
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Uncontrolled Resource Consumption: </h2>" +
                    "    <span>A recursive method is called, which calculates the total cost from the inputs daily cost and days. </span>" +
                    "    <div>\n" +
                    "      <label for=\"dailyCost\">Daily Cost: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"dailyCost\" id=\"dailyCost\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"days\">Days: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"days\" id=\"days\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Calculate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>" +
                    "    </div>\n" +
                    "    <pre><code>" + result + "</code></pre>\n" +
                    "  </div>\n").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getNullDereferencePage method generates an HTML string for the subpage where the null pointer dereference can be triggered. It provides a form to
         * enter a comma-separated list of usernames, which validity is to be checked afterwards.
         *
         * @return A string containing the HTML.
         */
        private String getNullDereferencePage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Validate Usernames: </h2>" +
                    "    <span>Validate if the desired username is permitted. Multiple usernames can be entered in a comma-separated format.</span>" +
                    "    <div>\n" +
                    "      <label for=\"usernames\">Username(s): </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"usernames\" id=\"usernames\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Validate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getNullDereferenceResponsePage handles the null pointer dereference vulnerability by passing the user input to a method of the {@link NullPointerDereference} class without validating.
         * The user input should a comma-separated list of usernames whose validity is to be checked (may cause a NullPointerException).
         *
         * @see NullPointerDereference
         * @param params
         * @return
         */
        private String getNullDereferenceResponsePage(Map<String, String> params) {
            String usernames = params.get("usernames");
            NullPointerDereference nullPointerDereference = new NullPointerDereference();
            String result = nullPointerDereference.validateNames(usernames);
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Validate Usernames: </h2>\n" +
                    "    <span>Validate if the desired username is permitted. Multiple usernames can be entered in a comma-separated format.</span>\n" +
                    "    <div>\n" +
                    "      <label for=\"usernames\">Username(s): </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"usernames\" id=\"usernames\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Validate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>"+
                    "    </div>\n" +
                    "    <pre><code>" + result + "</code></pre>\n" +
                    "  </div>\n").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getPathTraversalPage method generates an HTML string for the subpage where the path traversal could be exploited. It provides a form to
         * enter a file and its content to safe in a safe directory starting with the users name. It exists, it lists the files of the user and then provides the option to delete
         * files or to query the content.
         *
         * @return A string containing the HTML.
         */
        private String getPathTraversalPage() {
            FileOperations fileOperations = new FileOperations();
            String filesString = "";
            String appendGetFile = "";
            String[] files = fileOperations.getFiles(user.getUsername());
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    filesString = files[i] + "\n";
                }
                appendGetFile = "<form action=\"\" method=\"get\">\n" +
                        "  <div>\n" +
                        "    <h2>Get File: </h2>\n" +
                        "    <span>By entering the file name, you can read the content of the previously uploaded file</span>\n" +
                        "    <div>\n" +
                        "      <label for=\"fileDownload\">Filename: </label>\n" +
                        "      <input size=\"100\" type=\"text\" name=\"fileDownload\" id=\"fileDownload\">\n" +
                        "    </div>\n" +
                        "    <div>\n" +
                        "      <input type=\"submit\" value=\"Download\">\n" +
                        "    </div>\n" +
                        "  </div>\n" +
                        "</form>\n"+
                        "<form action=\"\" method=\"get\">\n" +
                        "  <div>\n" +
                        "    <h2>Delete File: </h2>\n" +
                        "    <span>By entering the file name, you can delete the previously uploaded file</span>\n" +
                        "    <div>\n" +
                        "      <label for=\"fileDelete\">Filename: </label>\n" +
                        "      <input size=\"100\" type=\"text\" name=\"fileDelete\" id=\"fileDelete\">\n" +
                        "    </div>\n" +
                        "    <div>\n" +
                        "      <input type=\"submit\" value=\"Delete\">\n" +
                        "    </div>\n" +
                        "  </div>\n" +
                        "</form>\n";
            }
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Upload File: </h2>\n" +
                    "    <span>By entering a file name and the contents of this file, the file is created in the <code>username/filename.txt</code> directory.</span>\n" +
                    "    <div>\n" +
                    "      <label for=\"filename\">Filename: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"filename\" id=\"filename\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"content\">Content of File: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"content\" id=\"content\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Upload\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>\n" +
                    "  <div>\n" +
                    "    <h2>List my Files: </h2>\n" +
                    "    <pre><code>" + filesString + "</code></pre>\n" +
                    "  </div>\n").append(appendGetFile).append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getPathTraversalResponsePage handles the path traversal vulnerability by passing the user input to a method of the {@link FileOperations} class without validating.
         * The HTML page contains forms for each operation such as upload, download and delete a file, where the user can enter the required information. In addition, the created files are listed.
         *
         * @see FileOperations
         * @param params A map containing the key-value pairs of the request parameters, in this case filename, content, fileDownload, fileUpload.
         * @return A string containing the HTML.
         */
        private String getPathTraversalResponsePage(Map<String, String> params) {
            String result, path, fileDelete;
            String filesString = "";
            String appendGetFile = "";
            String appendDownload = "";
            String appendUpload = "";


            FileOperations fileOperations = new FileOperations();
            // if a user wants to upload a file
            if (params.containsKey("filename") && params.containsKey("content")) {
                String content = params.get("content");
                String filename = params.get("filename");
                path = fileOperations.createFile(user.getUsername(), filename);
                //verify the file could be created
                if (!path.equals("")) {
                    //normalize the path
                    Path normalized = Paths.get(path).normalize();
                    path = normalized.toString();
                    //write to the file
                    result = fileOperations.writeToFile(path, content);
                } else {
                    result = "File could not be created.";
                }
                appendUpload =
                        "    <div>\n" +
                                "      <span>\n" + result + ", Path: \n" +
                                "        <code>" + path + "</code>\n" +
                                "      </span>\n" +
                                "  </div>\n";
                // if the user wants to get the content of a file previously created
            } else if (params.containsKey("fileDownload")) {
                String fileDownload = params.get("fileDownload");
                String content = fileOperations.readFile(user.getUsername(), fileDownload);
                appendDownload =
                        "    <div>\n" +
                                "      <span>\n Content: \n" +
                                "        <code>" + content + "</code>\n" +
                                "      </span>\n" +
                                "  </div>\n";
            } else if (params.containsKey("fileDelete")){
                fileDelete = params.get("fileDelete");
                fileOperations.deleteFile(user.getUsername(), fileDelete);
            }

            // List the files of the user
            String[] files = fileOperations.getFiles(user.getUsername());
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    filesString = files[i] + "\n";
                }
                appendGetFile = "<form action=\"\" method=\"get\">\n" +
                        "  <div>\n" +
                        "    <h2>Get File: </h2>\n" +
                        "    <span>By entering the file name, you can read the content of the previously uploaded file</span>\n" +
                        "    <div>\n" +
                        "      <label for=\"fileDownload\">Filename: </label>\n" +
                        "      <input size=\"100\" type=\"text\" name=\"fileDownload\" id=\"fileDownload\">\n" +
                        "    </div>\n" +
                        "    <div>\n" +
                        "      <input type=\"submit\" value=\"Download\">\n" +
                        "    </div>\n" +
                        "  </div>\n" +
                        "</form>\n"+
                        "<form action=\"\" method=\"get\">\n" +
                        "  <div>\n" +
                        "    <h2>Delete File: </h2>\n" +
                        "    <span>By entering the file name, you can delete the previously uploaded file</span>\n" +
                        "    <div>\n" +
                        "      <label for=\"fileDelete\">Filename: </label>\n" +
                        "      <input size=\"100\" type=\"text\" name=\"fileDelete\" id=\"fileDelete\">\n" +
                        "    </div>\n" +
                        "    <div>\n" +
                        "      <input type=\"submit\" value=\"Delete\">\n" +
                        "    </div>\n" +
                        "  </div>\n" +
                        "</form>\n";
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Upload File: </h2>\n" +
                    "    <span>By entering a file name and the contents of this file, the file is created in the <code>username/filename.txt</code> directory.</span>\n" +
                    "    <div>\n" +
                    "      <label for=\"filename\">Filename: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"filename\" id=\"filename\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"content\">Content of File: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"content\" id=\"content\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Upload\">\n" +
                    "    </div>\n" +
                    "  </div>\n").append(appendUpload).append(
                    "</form>\n" +
                            "  <div>\n" +
                            "    <h2>List my Files: </h2>\n" +
                            "    <pre><code>" + filesString + "</code></pre>\n" +
                            "  </div>\n").append(appendGetFile).append(appendDownload).append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getIntegerWraparoundPage method generates an HTML string for the subpage where the integer wraparound vulnerability could be exploited. It provides a form to
         * enter the initial Capital, monthly savings, annual investment period and the annual interest rate to calculate the final capital.
         * @return A string containing the HTML.
         */
        private String getIntegerWraparoundPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Compound interest calculator: </h2>\n" +
                    "    <span>This compound interest calculator can be used to calculate how much compound interest will accrue over a given investment period. </span>\n" +
                    "    <div>\n" +
                    "      <label for=\"initialCapital\">Initial capital (rounded to whole numbers): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"initialCapital\" id=\"initialCapital\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"monthlySavings\">Monthly savings (rounded to whole numbers): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"monthlySavings\" id=\"monthlySavings\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"investmentPeriod\">Investment period (annual, in whole years): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"investmentPeriod\" id=\"investmentPeriod\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"annualInterestRate\">Annual interest rate: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"annualInterestRate\" id=\"annualInterestRate\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Calculate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getIntegerWraparoundResponsePage handles the integer wraparound vulnerability by passing the user input to a method of the {@link IntegerWraparound} class without validating.
         * The user input is located in the map passed as a parameter. These are parsed to integers and then used for the calculation. The result is written to the HTML string.
         * Moreover, the HTML page contains forms for the initial Capital, monthly savings, annual investment period and the annual interest rate to calculate the final capital.
         * @param params A map containing the key-value pairs of the request parameters, in this case the initial Capital, monthly savings, annual investment period and the annual interest rate
         * @return A string containing the HTML.
         */
        private String getIntegerWraparoundResponsePage(Map<String, String> params) {
            String resultString;
            IntegerWraparound integerWraparound = new IntegerWraparound();
            int initialCapital = Integer.parseInt(params.get("initialCapital"));
            int monthlySavings = Integer.parseInt(params.get("monthlySavings"));
            int investmentPeriod = Integer.parseInt(params.get("investmentPeriod"));
            int annualInterestRate = Integer.parseInt(params.get("annualInterestRate"));
            int[] result = integerWraparound.calculateFinalCapital(initialCapital, monthlySavings, investmentPeriod, annualInterestRate);
            if (result[0] == 0 && result[1] == 0 && result[2] == 0){
                resultString = "There was an error in the calculation, please fill in each input field with a number greater than 0.";
            } else {
                resultString = "Interest amount: "+result[0]+"\n Deposit amount: "+result[1]+"\n final Capital: "+result[2];
            }
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Compound interest calculator: </h2>\n" +
                    "    <span>This compound interest calculator can be used to calculate how much compound interest will accrue over a given investment period. </span>\n" +
                    "    <div>\n" +
                    "      <label for=\"initialCapital\">Initial capital (rounded to whole numbers): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"initialCapital\" id=\"initialCapital\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"monthlySavings\">Monthly savings (rounded to whole numbers): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"monthlySavings\" id=\"monthlySavings\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"investmentPeriod\">Investment period (annual, in whole years): </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"investmentPeriod\" id=\"investmentPeriod\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"annualInterestRate\">Annual interest rate: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"annualInterestRate\" id=\"annualInterestRate\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Calculate\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>" +
                    "    <div>\n" +
                    "<span style=\"color:blue\">"+ resultString + "</span>" +
                    "  </div>\n").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getDeserializationPage method generates an HTML string for the subpage where the deserialization of untrusted data vulnerability could be exploited.
         * It provides a form to enter a serialized and base64 encoded Object.
         * @return A string containing the HTML.
         */
        private String getDeserializationPage() {
            byte[] serializedObjectStream;
            String serializedObject = null;
            SerializationHelper serializationHelper = new SerializationHelper();
            try {
                serializedObjectStream = serializationHelper.serialize(new VulnerableObject("list", "ls -l"));
                serializedObject = Base64.getEncoder().encodeToString(serializedObjectStream);
            } catch (IOException e) {
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"post\">\n" +
                    "  <div>\n" +
                    "  <h2>Deserialization: </h2> \n" +
                    "  <span> The following text field receives a serialized object and deserializes it. </span> \n" +
                    "  <div>\n" +
                    "  <span> The serialized object with Base64 encoding looks like: </span> \n" +
                    "    <pre><code>" + serializedObject + "</code></pre>\n" +
                    "  </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"objectString\">Object String: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"objectString\" id=\"objectString\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Submit\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getDeserializationResponsePage handles the deserialization of untrusted data vulnerability by passing the serialized object to a method of the {@link SerializationHelper} class without validating.
         * The object gets deserialized and the command, as an attribute of the object, executed at the same time, is added to the HTML String.
         * The HTML page also contains a form to enter another serialized and base64 encoded Object.
         * @param params A map containing the key-value pairs of the request parameters, in this case the base64 encoded serialized object
         * @return A string containing the HTML.
         */
        private String getDeserializationResponsePage(Map<String, String> params) {
            String name = null, command = null;
            byte[] serializedObject = Base64.getDecoder().decode(params.get("objectString"));
            SerializationHelper serializationHelper = new SerializationHelper();
            Object o = serializationHelper.deserialize(serializedObject);
            if (o instanceof VulnerableObject) {
                //reflection to get the object
                VulnerableObject vulnerableObject = VulnerableObject.class.cast(o);
                name = vulnerableObject.getName();
                command = vulnerableObject.getCommand();
            } else {
                name = "wrong object";
                command = "no command was executed";
            }
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "  <h2>Deserialization: </h2> \n" +
                    "  <span> The following text field receives a serialized object and deserializes it. </span> \n" +
                    "  <div>\n" +
                    "  <span> The deserialized object <code>" + name + "</code> executed the command: </span> \n" +
                    "    <pre><code>" + command + "</code></pre>\n" +
                    "  </div>\n" +
                    "    <div>\n" +
                    "      <label for=\"objectString\">Object String: </label>\n" +
                    "      <input size=\"100\" type=\"text\" name=\"objectString\" id=\"objectString\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Submit\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The getInputValidationPage method generates an HTML string for the subpage where the improper input validation vulnerability could be exploited.
         * It provides a form  to enter the number of rounds to be rolled.
         * @return A string containing the HTML
         */
        private String getInputValidationPage() {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><form action=\"\" method=\"get\">\n" +
                    "  <div>\n" +
                    "    <h2>Roll the Dice: </h2>" +
                    "    <span>Indicate the number of rounds to be rolled. A dice result is output for each round.</span>" +
                    "    <div>\n" +
                    "      <label for=\"rounds\">Rounds: </label>\n" +
                    "      <input size=\"100\" type=\"number\" name=\"rounds\" id=\"rounds\">\n" +
                    "    </div>\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Dice!\">\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</form>").append("</body></html>");
            return htmlBuilder.toString();
        }

        /**
         * The method getInputValidationResponsePage handles the improper input validation vulnerability by passing the serialized object to a method of the {@link ImproperInputValidation} class without validating.
         * It passes the number of rounds from the params Map to this method and adds the result to the HTML string.
         * @param params A map containing the key-value pairs of the request parameters, in this case the rounds to be rolled.
         * @return A string containing the HTML.
         */
        private String getInputValidationResponsePage(Map<String, String> params) {
            String result = "";
            byte[] diceResults;
            ImproperInputValidation improperInputValidation = new ImproperInputValidation();
            diceResults = improperInputValidation.rollDice(Integer.parseInt(params.get("rounds")));
            for (int i = 0; i < diceResults.length; i++){
                result = result+(i+1)+". round = "+diceResults[i]+"\n";
            }
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                    "  <div>\n" +
                    "    <h2>Roll the Dice: </h2>\n" +
                    "    <span>Indicate the number of rounds to be rolled. A dice result is output for each round.</span>\n" +
                    "  </div>\n" +
                    "  <div>\n" +
                    "  <form action=\"\" method=\"get\">\n" +
                    "    <label for=\"rounds\">Rounds: </label>\n" +
                    "    <input size=\"100\" type=\"number\" name=\"rounds\" id=\"rounds\">\n" +
                    "    <div>\n" +
                    "      <input type=\"submit\" value=\"Dice!\">\n" +
                    "    </div>" +
                    "  </form>" +
                    "  </div>\n" +
                    "  <div>" +
                    "    <pre><code>" + result + "</code></pre>" +
                    "  </div>\n").append("</body></html>");
            return htmlBuilder.toString();
        }
    }
}