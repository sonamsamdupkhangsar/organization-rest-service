package me.sonam.organization;

import me.sonam.organization.config.OrganizationUserLimitProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationUserLimitPropertiesTest {

    @Test
    void returnsHostSpecificLimitForIssuerUrl() {
        OrganizationUserLimitProperties properties = new OrganizationUserLimitProperties();
        properties.setDefaultMaxAddedUsers(5);
        properties.getHosts().put("demo.openissuer.com", 2);
        properties.getHosts().put("free.openissuer.com", 2);

        assertThat(properties.maxAddedUsersForIssuer("https://demo.openissuer.com")).isEqualTo(2);
        assertThat(properties.maxAddedUsersForIssuer("https://free.openissuer.com/issuer")).isEqualTo(2);
    }

    @Test
    void returnsDefaultLimitWhenIssuerHasNoOverride() {
        OrganizationUserLimitProperties properties = new OrganizationUserLimitProperties();
        properties.setDefaultMaxAddedUsers(5);
        properties.getHosts().put("demo.openissuer.com", 2);

        assertThat(properties.maxAddedUsersForIssuer("https://business1.openissuer.com")).isEqualTo(5);
        assertThat(properties.maxAddedUsersForIssuer("")).isEqualTo(5);
    }
}
