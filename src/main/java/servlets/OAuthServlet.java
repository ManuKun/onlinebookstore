package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        res.getWriter().println("<h2>OAuth Servlet Hit</h2>");

        System.out.println("OAuthServlet hit");

        String code = req.getParameter("code");
        System.out.println("Authorization Code: " + code);

        if (code == null) {
            res.getWriter().println("Error: No authorization code received");
            return;
        }

        try {

            // STEP 1: Exchange code for access token
            URL url = new URL("https://oauth2.googleapis.com/token");

            String params = "code=" + code +
                    "&client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&redirect_uri=http://localhost:8083/onlinebookstore/oauth" +
                    "&grant_type=authorization_code";

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(params.getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();

            System.out.println("Token Response: " + response);

            JSONObject json = new JSONObject(response);
            String accessToken = json.getString("access_token");

            // STEP 2: Fetch user info
            URL userInfoUrl = new URL(
                    "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken);

            HttpURLConnection userConn = (HttpURLConnection) userInfoUrl.openConnection();

            BufferedReader userBr = new BufferedReader(new InputStreamReader(userConn.getInputStream()));
            String userInfo = userBr.readLine();

            System.out.println("User Info: " + userInfo);

            JSONObject userJson = new JSONObject(userInfo);

            String email = userJson.getString("email");

            // STEP 3: Create session
            HttpSession session = req.getSession();
            session.setAttribute("CUSTOMER", email);

            System.out.println("✅ Login success: " + email);

            // STEP 4: Redirect properly
            res.sendRedirect("CustomerHome.html");

        } catch (Exception e) {
            e.printStackTrace();

            res.setContentType("text/html");
            res.getWriter().println("<h2>OAuth Error</h2>");
            res.getWriter().println("<pre>" + e.getMessage() + "</pre>");
        }
    }
}