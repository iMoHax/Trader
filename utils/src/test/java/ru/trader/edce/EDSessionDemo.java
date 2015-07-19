package ru.trader.edce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EDSessionDemo {
    private final static Logger LOG = LoggerFactory.getLogger(EDSessionDemo.class);

    private static String readLine(String format, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        return reader.readLine();
    }

    public static void main(String args[]) throws Exception {
        LOG.info("Test ED Companion connect");
        EDSession edSession = new EDSession();
        if (edSession.getLastStatus() == ED_SESSION_STATUS.OK){
            LOG.info("Check get profile");
            edSession.readProfile(s ->{});
        }
        if (edSession.getLastStatus() == ED_SESSION_STATUS.LOGIN_REQUIRED) {
            String login = readLine("Login:");
            String pass = readLine("Password:");
            edSession.login(login, pass);
            if (edSession.getLastStatus() == ED_SESSION_STATUS.VERIFICATION_REQUIRED) {
                LOG.info("Check verification");
                String code = readLine("Verify code:");
                edSession.submitVerifyCode(code);
                edSession.login(login, pass);
            }
            if (edSession.getLastStatus() == ED_SESSION_STATUS.OK) {
                LOG.info("Check get profile");
                edSession.readProfile(s -> {});
            }
        }
        edSession.close();
    }
}
