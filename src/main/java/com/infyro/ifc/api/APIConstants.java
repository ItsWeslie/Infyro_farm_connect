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
    public static final String FARM = BASE_URL + "/farm";
    public static final String FIELD = BASE_URL + "/field";

    //Auth Endpoints
    public static final class Auth{
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String VALIDATE = "/validate";
        public static final String SIGNUP = "/signup";
    }

    //User Endpoints
    public static final class Member{
        public static final String MEMBER_DASHBOARD = APIConstants.USER + "/dashboard";
    }

    //Farm Endpoints
    public static final class Farm{
        public static final String CREATE = "/create";
        public static final String VIEW_FARM = "/view/{id}";
        public static final String REMOVE = "/remove/{id}";
        public static final String OWNED_FARMS = "/owned-farms";
        public static final String UPDATE = "/update/{id}";
    }

    //Field Endpoints
    public static final class Field{
        public static final String UNALLOCATED_FIELDS = "/unallocated-fields/{id}";
        public static final String MAP_CROP = "/map-crop/{id}";
    }
}
