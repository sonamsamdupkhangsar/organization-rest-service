package me.sonam.organization.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "organization-user-limits")
public class OrganizationUserLimitProperties {
    private int defaultMaxAddedUsers = 5;
    private final Map<String, Integer> hosts = new HashMap<>();

    public int getDefaultMaxAddedUsers() {
        return defaultMaxAddedUsers;
    }

    public void setDefaultMaxAddedUsers(int defaultMaxAddedUsers) {
        this.defaultMaxAddedUsers = defaultMaxAddedUsers;
    }

    public Map<String, Integer> getHosts() {
        return hosts;
    }

    public int maxAddedUsersForIssuer(String issuer) {
        String host = normalizeHost(issuer);
        if (StringUtils.hasText(host) && hosts.containsKey(host)) {
            return hosts.get(host);
        }
        return defaultMaxAddedUsers;
    }

    private String normalizeHost(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            return null;
        }

        String trimmedIssuer = issuer.trim();
        try {
            URI uri = URI.create(trimmedIssuer);
            if (StringUtils.hasText(uri.getHost())) {
                return uri.getHost();
            }
        }
        catch (IllegalArgumentException ignored) {
            // Treat non-URI values as host names below.
        }

        int schemeIndex = trimmedIssuer.indexOf("://");
        String host = schemeIndex >= 0 ? trimmedIssuer.substring(schemeIndex + 3) : trimmedIssuer;
        int slashIndex = host.indexOf('/');
        if (slashIndex >= 0) {
            host = host.substring(0, slashIndex);
        }
        int colonIndex = host.indexOf(':');
        if (colonIndex >= 0) {
            host = host.substring(0, colonIndex);
        }
        return StringUtils.hasText(host) ? host : null;
    }
}
