package com.vmware;

import com.vmware.http.RequestParams;
import com.vmware.http.cookie.ApiAuthentication;
import com.vmware.http.exception.ForbiddenException;
import com.vmware.http.exception.NotAuthorizedException;
import com.vmware.util.IOUtils;
import com.vmware.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Base Class for Rest and Non Rest services.
 */
public abstract class AbstractService {

    private final static int MAX_LOGIN_RETRIES = 3;

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public String baseUrl;
    protected String apiUrl;
    protected ApiAuthentication credentialsType;
    protected String username;

    protected Boolean connectionIsAuthenticated = null;

    protected AbstractService(String baseUrl, String apiPath, ApiAuthentication credentialsType, String username) {
        this.baseUrl = UrlUtils.addTrailingSlash(baseUrl);
        this.apiUrl = this.baseUrl + apiPath;
        this.credentialsType = credentialsType;
        this.username = username;
    }

    public boolean isConnectionAuthenticated() {
        if (connectionIsAuthenticated != null) {
            return connectionIsAuthenticated;
        }
        try {
            checkAuthenticationAgainstServer();
            connectionIsAuthenticated = true;
        } catch (NotAuthorizedException | ForbiddenException e) {
            connectionIsAuthenticated = false;
        }
        return connectionIsAuthenticated;
    }

    public void setupAuthenticatedConnection() {
        int retryCount = 0;
        while (!isConnectionAuthenticated()) {
            if (retryCount > MAX_LOGIN_RETRIES) {
                System.exit(1);
            }
            connectionIsAuthenticated = null;
            displayInputMessage(retryCount);
            try {
                loginManually();
            } catch (NotAuthorizedException e) {
                connectionIsAuthenticated = false;
            }
            retryCount++;
        }
    }

    public abstract boolean isBaseUriTrusted();

    private void displayInputMessage(int retryCount) {
        if (retryCount == 0) {
            String homeFolder = System.getProperty("user.home");
            String filePath = homeFolder + "/" + credentialsType.getFileName();
            if (credentialsType.getCookieName() != null) {
                log.info("Valid {} cookie ({}) not found in file {}", credentialsType.name(),
                        credentialsType.getCookieName(), filePath);
            } else {
                log.info("Valid {} token not found in file {}", credentialsType.name(), filePath);
            }
        } else  {
            log.info("");
            log.warn("Login failure");
            log.info("Retrying login, attempt {} of {}", retryCount, MAX_LOGIN_RETRIES);
        }
    }

    /**
     * Api tokens are stored in the user's home directly.
     * @see ApiAuthentication for file system locations
     */
    protected String readExistingApiToken(ApiAuthentication credentialsType) {
        String homeFolder = System.getProperty("user.home");
        File apiTokenFile = new File(homeFolder + "/" + credentialsType.getFileName());
        if (!apiTokenFile.exists()) {
            return null;
        }
        log.debug("Reading {} api token from file {}", credentialsType.name(), apiTokenFile.getPath());
        return IOUtils.read(apiTokenFile);
    }

    protected void saveApiToken(String apiToken, ApiAuthentication credentialsType) {
        String existingToken = readExistingApiToken(credentialsType);
        if (apiToken == null || apiToken.equals(existingToken)) {
            return;
        }
        String homeFolder = System.getProperty("user.home");
        File apiTokenFile = new File(homeFolder + "/" + credentialsType.getFileName());

        log.info("Saving {} api token to {}", credentialsType.name(), apiTokenFile.getPath());
        IOUtils.write(apiTokenFile, apiToken);
    }

    /**
     * Subclasses should implement this method as an authentication check.
     * An UnauthorizedException should be thrown if authentication fails
     */
    protected abstract void checkAuthenticationAgainstServer();

    /**
     * Ask the user for credentials and retrieve a token / cookie for future authentication. This should be persisted.
     */
    protected abstract void loginManually();
}
