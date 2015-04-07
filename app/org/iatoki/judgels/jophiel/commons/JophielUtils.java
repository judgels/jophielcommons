package org.iatoki.judgels.jophiel.commons;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import org.apache.commons.codec.binary.Base64;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.User;
import play.Play;
import play.libs.Json;
import play.mvc.Http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class JophielUtils {

    private static Lock activityLock = new ReentrantLock();

    private JophielUtils() {
        // prevent instantiation
    }

    public static ClientID getClientJid() {
        String clientId = Play.application().configuration().getString("jophiel.clientJid");
        if (clientId == null) {
            throw new IllegalStateException("jophiel.clientJid not found in configuration");
        }
        return new ClientID(clientId);
    }

    public static Secret getClientSecret() {
        String clientSecret = Play.application().configuration().getString("jophiel.clientSecret");
        if (clientSecret == null) {
            throw new IllegalStateException("jophiel.clientSecret not found in configuration");
        }
        return new Secret(clientSecret);
    }

    public static String getAccessToken() {
        return Http.Context.current().session().get("accessToken");
    }

    public static String getEncodedAccessToken() {
        return Base64.encodeBase64String(getAccessToken().getBytes());
    }

    public static String getBase64Encoded(String s) {
        return Base64.encodeBase64String(s.getBytes());
    }

    public static String verifyUsername(String username) {
        try {
            URL url = getEndpoint("verifyUsername").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + getClientJid() + ":" + getClientSecret());
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("username=" + URLEncoder.encode(username, "UTF-8"));
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            InputStream is = connection.getInputStream();
            JsonNode jsonNode = Json.parse(is);
            is.close();

            if (jsonNode.get("success").asBoolean()) {
                return jsonNode.get("jid").asText();
            } else {
                throw null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean sendUserActivities(String accessToken, List<UserActivity> activityLogList) {
        try {
            URL url = getEndpoint("userActivities").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + getBase64Encoded(accessToken));
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("userActivities=" + URLEncoder.encode(Json.toJson(activityLogList).toString(), "UTF-8"));
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            InputStream is = connection.getInputStream();
            JsonNode jsonNode = Json.parse(is);
            is.close();

            return jsonNode.get("success").asBoolean();
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    public static User getUserByUserJid(String userJid) {
        try {
            URL url = getEndpoint("userInfoByJid").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + getClientJid() + ":" + getClientSecret());
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("userJid=" + URLEncoder.encode(userJid, "UTF-8"));
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            InputStream is = connection.getInputStream();
            JsonNode jsonNode = Json.parse(is);
            is.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                User user = new User(jsonNode.get("id").asInt(), jsonNode.get("jid").asText(), jsonNode.get("username").asText(), jsonNode.get("name").asText(), jsonNode.get("email").asText(), new URL(jsonNode.get("profilePictureUrl").asText()));

                return user;
            } else {
                throw new RuntimeException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAutoCompleteEndPoint() {
        try {
            return getEndpoint("userAutoComplete").toURL().toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static URI getEndpoint(String service) {
        String baseUrl = Play.application().configuration().getString("jophiel.baseUrl");
        if (baseUrl == null) {
            throw new IllegalStateException("jophiel.baseUrl not found in configuration");
        }

        try {
            return new URI(baseUrl + "/" + service);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("jophiel.baseUrl malformed in configuration");
        }
    }

    public static URL getDefaultAvatarUrl() throws MalformedURLException {
        return getEndpoint("assets/images/avatar/avatar-default.png").toURL();
    }

    public static void updateUserAvatarCache(AbstractAvatarCacheService<?> avatarCacheService) {
        if (IdentityUtils.getUserJid() != null) {
            try {
                avatarCacheService.putImageUrl(IdentityUtils.getUserJid(), new URL(Http.Context.current().session().get("avatar")), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Lock getActivityLock() {
        return activityLock;
    }
}
