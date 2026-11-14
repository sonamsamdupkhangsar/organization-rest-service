package me.sonam.organization.handler;

public class OrganizationMemberLimitException extends OrgException {
    public static final String CODE = "ORGANIZATION_MEMBER_LIMIT_REACHED";

    public OrganizationMemberLimitException() {
        super("Max number of organization users reached");
    }
}
