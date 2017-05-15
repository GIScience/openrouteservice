package heigit.ors.servlet.requests;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

// suppress calls to sendError() and just setStatus() instead
// do NOT use sendError() otherwise per servlet spec the container will send an html error page
@SuppressWarnings({ "unused", "unchecked", "deprecation" })
public class StatusCodeCaptureWrapper extends HttpServletResponseWrapper 
{
    private Integer statusCode;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public StatusCodeCaptureWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
        this.response = response;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public void sendError(int sc) throws IOException {
        // do NOT use sendError() otherwise per servlet spec the container will send an html error page
        this.setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        // do NOT use sendError() otherwise per servlet spec the container will send an html error page
        this.setStatus(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        this.statusCode = sc;
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.statusCode = sc;
        super.setStatus(sc);
    }
}