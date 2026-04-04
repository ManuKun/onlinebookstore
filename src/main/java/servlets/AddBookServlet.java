package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.db.BooksDBConstants;
import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

public class AddBookServlet extends HttpServlet {

    BookService bookService = new BookServiceImpl();

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

        PrintWriter pw = res.getWriter();
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        if (!StoreUtil.isLoggedIn(UserRole.SELLER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("SellerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\"><tr><td>Please Login First to Continue!!</td></tr></table>");
            return;
        }

        // CSRF TOKEN VALIDATION (only for POST submission)
        String bName = req.getParameter(BooksDBConstants.COLUMN_NAME);
        if (bName != null) {
            String sessionToken = (String) req.getSession().getAttribute("csrfToken");
            String requestToken = req.getParameter("csrfToken");

            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF detected");
                return;
            }
        }

        RequestDispatcher rd = req.getRequestDispatcher("SellerHome.html");
        rd.include(req, res);
        StoreUtil.setActiveTab(pw, "addbook");

        pw.println("<div class='container my-2'>");

        // Generate CSRF token when loading form
        if (bName == null || bName.isBlank()) {
            String csrfToken = UUID.randomUUID().toString();
            req.getSession().setAttribute("csrfToken", csrfToken);
            showAddBookForm(pw, csrfToken);
            return;
        }

        try {

            String uniqueID = UUID.randomUUID().toString();
            String bCode = uniqueID;

            // XSS FIX
            bName = StringEscapeUtils.escapeHtml4(bName);
            String bAuthor = StringEscapeUtils.escapeHtml4(
                    req.getParameter(BooksDBConstants.COLUMN_AUTHOR));

            double bPrice = Integer.parseInt(req.getParameter(BooksDBConstants.COLUMN_PRICE));
            int bQty = Integer.parseInt(req.getParameter(BooksDBConstants.COLUMN_QUANTITY));

            Book book = new Book(bCode, bName, bAuthor, bPrice, bQty);

            String message = bookService.addBook(book);

            if ("SUCCESS".equalsIgnoreCase(message)) {
                pw.println("<table class=\"tab\"><tr><td>Book Detail Updated Successfully!<br/>Add More Books</td></tr></table>");
            } else {
                pw.println("<table class=\"tab\"><tr><td>Failed to Add Books! Fill up CareFully</td></tr></table>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            pw.println("<table class=\"tab\"><tr><td>Failed to Add Books! Fill up CareFully</td></tr></table>");
        }
    }

    // Updated form method (includes CSRF token)
    private static void showAddBookForm(PrintWriter pw, String csrfToken) {

        String form = "<table class=\"tab my-5\" style=\"width:40%;\">\r\n"
                + "        <tr>\r\n"
                + "            <td>\r\n"
                + "                <form action=\"addbook\" method=\"post\">\r\n"
                + "                    <input type='hidden' name='csrfToken' value='" + csrfToken + "'/>\r\n"
                + "                    <label for=\"bookName\">Book Name : </label> <input type=\"text\" name=\"name\" id=\"bookName\" placeholder=\"Enter Book's name\" required><br/>\r\n"
                + "                    <label for=\"bookAuthor\">Book Author : </label><input type=\"text\" name=\"author\" id=\"bookAuthor\" placeholder=\"Enter Author's Name\" required><br/>\r\n"
                + "                    <label for=\"bookPrice\">Book Price : </label><input type=\"number\" name=\"price\" placeholder=\"Enter the Price\" required><br/>\r\n"
                + "                    <label for=\"bookQuantity\">Book Qnty : </label><input type=\"number\" name=\"quantity\" id=\"bookQuantity\" placeholder=\"Enter the quantity\" required><br/>\r\n"
                + "                    <input class=\"btn btn-success my-2\" type=\"submit\" value=\" Add Book \">\r\n"
                + "                </form>\r\n"
                + "            </td>\r\n"
                + "        </tr>\r\n"
                + "    </table>";

        pw.println(form);
    }
}