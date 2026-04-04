package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.db.UsersDBConstants;
import com.bittercode.model.User;
import com.bittercode.model.UserRole;
import com.bittercode.service.UserService;
import com.bittercode.service.impl.UserServiceImpl;

public class CustomerLoginServlet extends HttpServlet {

    UserService authService = new UserServiceImpl();

    // 🔹 Handle login (POST)
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);
        PrintWriter pw = res.getWriter();

        HttpSession session = req.getSession();

        // STEP 1: CSRF Validation
        String sessionToken = (String) session.getAttribute("csrfToken");
        String requestToken = req.getParameter("csrfToken");

        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            throw new ServletException("CSRF validation failed!");
        }

        // 🔹 Remove token after validation (one-time use)
        session.removeAttribute("csrfToken");

        String uName = req.getParameter(UsersDBConstants.COLUMN_USERNAME);
        String pWord = req.getParameter(UsersDBConstants.COLUMN_PASSWORD);

        User user = authService.login(UserRole.CUSTOMER, uName, pWord, req);

        try {

            if (user != null) {

                res.sendRedirect("CustomerHome.html");

            } else {

                RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
                rd.include(req, res);

                pw.println("<table class=\"tab\"><tr><td>Incorrect UserName or PassWord</td></tr></table>");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Handle page load (GET)
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        HttpSession session = req.getSession();

        // STEP 2: Generate CSRF token
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);

        // Forward to login page with token
        req.setAttribute("csrfToken", csrfToken);

        RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
        rd.forward(req, res);
    }
}