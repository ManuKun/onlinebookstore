package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

public class OAuthServlet extends HttpServlet {

    private static final String CLIENT_ID = "906619891428-rvfp34h2c8r5bud3cteu6kb25k2lhev1.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        res.setContentType("text/html");
        System.out.println("OAuthServlet hit");

        String code = req.getParameter("code");
        System.out.println("Authorization Code: " + code);

        if (code == null) {
            res.getWriter().println("Error: No authorization code received");
            return;
        }

        try {

            //STEP 1: Prepare request
            URL url = new URL("https://oauth2.googleapis.com/token");

            String params =
                    "code=" + URLEncoder.encode(code, "UTF-8") +
                    "&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&redirect_uri=" + URLEncoder.encode("http://localhost:8083/onlinebookstore/oauth", "UTF-8") +
                    "&grant_type=authorization_code";

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(params.getBytes("UTF-8"));

            //DEBUG
            int status = conn.getResponseCode();
            System.out.println("HTTP CODE: " + status);

            BufferedReader br;
            if (status == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            String response = responseBuilder.toString();
            System.out.println("FULL TOKEN RESPONSE: " + response);

            //If error response, show it
            if (status != 200) {
                res.getWriter().println("<h3>OAuth Failed</h3>");
                res.getWriter().println("<pre>" + response + "</pre>");
                return;
            }

            JSONObject json = new JSONObject(response);
            String accessToken = json.getString("access_token");

            //STEP 2: Fetch user info
            URL userInfoUrl = new URL(
                    "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken);

            HttpURLConnection userConn = (HttpURLConnection) userInfoUrl.openConnection();

            BufferedReader userBr = new BufferedReader(new InputStreamReader(userConn.getInputStream()));

            StringBuilder userBuilder = new StringBuilder();
            String uline;

            while ((uline = userBr.readLine()) != null) {
                userBuilder.append(uline);
            }

            String userInfo = userBuilder.toString();
            System.out.println("FULL USER INFO: " + userInfo);

            JSONObject userJson = new JSONObject(userInfo);
            String email = userJson.getString("email");

            //STEP 3: Create session
            HttpSession session = req.getSession();
            session.setAttribute("CUSTOMER", email);

            System.out.println("Login success: " + email);

            // STEP 4: Redirect
            res.sendRedirect("CustomerHome.html");

        } catch (Exception e) {
            e.printStackTrace();

            res.getWriter().println("<h2>OAuth Error</h2>");
            res.getWriter().println("<pre>" + e.getMessage() + "</pre>");
        }
    }
}