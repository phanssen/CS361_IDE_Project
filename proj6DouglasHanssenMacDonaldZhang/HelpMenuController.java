/*
File: HelpMenuController.java
CS361 Project 6
Names:  Kyle Douglas, Paige Hanssen, Wyett MacDonald, and Tia Zhang
Date: 11/1/18
*/

package proj6DouglasHanssenMacDonaldZhang;
/**
 * This is a controller file for help menu.
 * There is a method that opens URL with browser and two methods deal with two menuitems in help menu.
 * @author Danqing Zhao
 * @author Micheal Coyne
 * @author Wyett MacDonald
 */

import java.lang.reflect.Method;

public class HelpMenuController {

    public void handleHelpMenuItemAction(){
        openURL("https://docs.oracle.com/javase/tutorial/");
    }

    public void handleUrlMenuItemAction(){
        openURL("https://github.com/phoenixding/idrem/tree/master/sourcecode/edu/cmu/cs/sb/chromviewer");
    }
    private static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) { }
    }

    private static void browse(String url) throws Exception {
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL",new Class[] { String.class });
            openURL.invoke(null, new Object[] { url });
        } else if (osName.startsWith("Windows")) {
            Runtime.getRuntime().exec(
                    "rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            String[] browsers = { "firefox", "opera", "mozilla"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                if (Runtime.getRuntime()
                        .exec(new String[] { "which", browsers[count] })
                        .waitFor() == 0)
                    browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[] { browser, url });
            }
        }
    }
}
