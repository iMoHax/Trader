package ru.trader.edce;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

/* Connect to Elite Dangerous Companion
*  Thanks Andargor(https://github.com/Andargor) for source on Python
*/
public class EDSession {
    private final static Logger LOG = LoggerFactory.getLogger(EDSession.class);
    private final static String COMPANION_DOMAIN = "companion.orerve.net";
    private final static String LOGIN_URL = "https://" + COMPANION_DOMAIN + "/user/login";
    private final static String CONFIRM_URL = "https://" + COMPANION_DOMAIN + "/user/confirm";
    private final static String PROFILE_URL = "https://" + COMPANION_DOMAIN + "/profile";
    private final static String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D257";
    private final static String COOKIE_FILE = "edce.tmp";

    private CookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private ED_SESSION_STATUS lastStatus;

    public EDSession() throws IOException, ClassNotFoundException {
        this.lastStatus = ED_SESSION_STATUS.LOGIN_REQUIRED;
        initClient();
    }

    private void initClient() throws IOException, ClassNotFoundException {
        cookieStore = readCookieStore();
        checkCookie();
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(USER_AGENT)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    public void login(String login, String password){
        LOG.info("Login to {}, email {}", COMPANION_DOMAIN, login);
        HttpUriRequest loginRequest = RequestBuilder.post(LOGIN_URL)
                .addParameter("email", login)
                .addParameter("password", password)
                .build();
        EDResponseHandler handler = new EDResponseHandler(content -> {
            if (content.contains("verification code")){
                LOG.info("Verification code required");
                lastStatus = ED_SESSION_STATUS.VERIFICATION_REQUIRED;
            } else {
                if (content.contains("Password")){
                    LOG.info("Wrong password or email");
                    lastStatus = ED_SESSION_STATUS.LOGIN_FAILED;
                } else {
                    lastStatus = ED_SESSION_STATUS.OK;
                }
            }
        });
        try {
            if (!httpClient.execute(loginRequest, handler)){
                lastStatus = ED_SESSION_STATUS.ERROR;
            }
        } catch (IOException e) {
            LOG.error("Error on connect to {}", LOGIN_URL);
            LOG.error("", e);
            lastStatus = ED_SESSION_STATUS.ERROR;
        }
    }

    public void submitVerifyCode(String code){
        LOG.info("Submit verify code to {}", COMPANION_DOMAIN);
        HttpUriRequest submitRequest = RequestBuilder.post(CONFIRM_URL)
                .addParameter("code", code)
                .build();
        EDResponseHandler handler = new EDResponseHandler(content -> {
            if (content.contains("verification code")){
                LOG.info("Wrong verify code");
                lastStatus = ED_SESSION_STATUS.VERIFICATION_REQUIRED;
            } else {
                lastStatus = ED_SESSION_STATUS.OK;
            }
        });
        try {
            if (!httpClient.execute(submitRequest, handler)){
                lastStatus = ED_SESSION_STATUS.ERROR;
            }
        } catch (IOException e) {
            LOG.error("Error on connect to {}", CONFIRM_URL);
            LOG.error("", e);
            lastStatus = ED_SESSION_STATUS.ERROR;
        }
    }

    public void readProfile(Consumer<String> contentConsumer){
        LOG.info("Submit profile request to {}", COMPANION_DOMAIN);
        HttpUriRequest submitRequest = RequestBuilder.get(PROFILE_URL).build();
        EDResponseHandler handler = new EDResponseHandler(content -> {
            if (lastStatus != ED_SESSION_STATUS.OK){
                if (content.contains("verification code")){
                    LOG.info("Verification code required");
                    lastStatus = ED_SESSION_STATUS.VERIFICATION_REQUIRED;
                } else if (content.contains("Password")) {
                    LOG.info("Login required");
                    lastStatus = ED_SESSION_STATUS.LOGIN_REQUIRED;
                } else {
                    contentConsumer.accept(content);
                    lastStatus = ED_SESSION_STATUS.OK;
                }
            } else {
                contentConsumer.accept(content);
            }
        });
        try {
            if (!httpClient.execute(submitRequest, handler)){
                lastStatus = ED_SESSION_STATUS.ERROR;
            }
        } catch (IOException e) {
            LOG.error("Error on connect to {}", CONFIRM_URL);
            LOG.error("", e);
            lastStatus = ED_SESSION_STATUS.ERROR;
        }
    }

    public ED_SESSION_STATUS getLastStatus() {
        return lastStatus;
    }

    public void close() throws IOException {
        writeCookieStore(cookieStore);
        httpClient.close();
    }

    private void checkCookie(){
        boolean appFound = false, mtkFound = false, midFound = false;
        for (Cookie cookie : cookieStore.getCookies()) {
            if (COMPANION_DOMAIN.equals(cookie.getDomain())) {
                if ("CompanionApp".equals(cookie.getName())) {
                    appFound = true;
                } else if ("mid".equals(cookie.getName())){
                    midFound = true;
                } else if ("mtk".equals(cookie.getName())){
                    mtkFound = true;
                }
                if (appFound && mtkFound && midFound){
                    LOG.debug("Old cookie found, login not required");
                    lastStatus = ED_SESSION_STATUS.OK;
                    break;
                }
            }
        }
    }

    public void clearCookies(){
        cookieStore.clear();
    }

    public void clearExpiredCookies(){
        cookieStore.clearExpired(new Date());
    }

    private static CookieStore readCookieStore() {
        LOG.debug("Read cookie store from {}", COOKIE_FILE);
        File file = new File(COOKIE_FILE);
        BasicCookieStore cookieStore = null;
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                cookieStore = (BasicCookieStore) ois.readObject();
                cookieStore.clearExpired(new Date());
            } catch (ClassNotFoundException | IOException e) {
                LOG.warn("Error on read cookie from {}", COOKIE_FILE);
                LOG.warn("", e);
            }
        }
        if (cookieStore == null) {
            LOG.debug("Not found cookie store, create new");
            cookieStore = new BasicCookieStore();
        }
        return cookieStore;
    }

    private static void writeCookieStore(CookieStore cookieStore) {
        LOG.debug("Write cookie store to {}", COOKIE_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(COOKIE_FILE))){
            oos.writeObject(cookieStore);
            oos.flush();
        } catch (IOException e) {
            LOG.warn("Error on write cookie to {}", COOKIE_FILE);
            LOG.warn("", e);
        }
    }

    private class EDResponseHandler implements ResponseHandler<Boolean> {

        private final Consumer<String> contentReader;

        public EDResponseHandler() {
            this(null);
        }

        public EDResponseHandler(Consumer<String> contentReader) {
            this.contentReader = contentReader;
        }

        private void readContent(HttpEntity entity) throws IOException {
            if (contentReader != null) {
                String content = EntityUtils.toString(entity);
                LOG.debug("Content: {}", content);
                contentReader.accept(content);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Content: {}", EntityUtils.toString(entity));
                } else {
                    EntityUtils.consume(entity);
                }
            }
        }

        @Override
        public Boolean handleResponse(HttpResponse response) throws IOException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response: {}", response.getStatusLine());
                LOG.debug("Headers: {}", Arrays.toString(response.getAllHeaders()));
            }
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300){
                HttpEntity entity = response.getEntity();
                if (entity != null){
                    if ("application/json".equals(entity.getContentType().getValue())){
                        lastStatus = ED_SESSION_STATUS.OK;
                    }
                    readContent(entity);
                }
                return true;
            } else {
                LOG.warn("Don't connect, status: {}", response.getStatusLine());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Content: {}", EntityUtils.toString(response.getEntity()));
                }
                return false;
            }
        }
    }
}
