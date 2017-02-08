package org.freeopenls.servlet.utils;

import java.io.OutputStream;
import javax.servlet.ServletResponse;
 
public class ServletUtility
{
    /**
     * Writes the bytes to the {@link OutputStream}
     *
     * @param response
     * @param bytes
     */
    public static void write(ServletResponse response, byte[] bytes)
    {
        int contentLength = -1;
        OutputStream outputStream = null;
 
        if ((null != response) &&
            (null != bytes) &&
            (bytes.length > 0))
        {
            contentLength = bytes.length;
 
            try
            {
                response.setContentLength(contentLength);
                outputStream = response.getOutputStream();
                outputStream.write(bytes);
            }
            catch (Exception exception)
            {
                outputStream = null;
            }
            finally
            {
                try
                {
                    if (null != outputStream) outputStream.close();
                }
                catch (Exception e){}
            }
        }
    }
}