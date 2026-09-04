package com.infyro.ifc.api;

public final class APIConstants {
    private APIConstants() {}

    //Base URLS
    private static final String API="/ifc";
    private static final String VERSION="/v1";
    private static final String BASE_URL= API+VERSION;

    //Resources
    public static final String AUTH = BASE_URL + "/auth";
    public static final String USER = BASE_URL + "/user";

    //Auth Endpoints
    public static final class Auth{
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String VALIDATE = "/validate";
    }

    //User Endpoints
    public static final class Member{
        public static final String MEMBER_DASHBOARD = APIConstants.USER + "/dashboard";
    }
}
