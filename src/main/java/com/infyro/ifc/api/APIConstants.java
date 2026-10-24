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
    //public static final String CROP = BASE_URL + "/crop";
    public static final String CROP_TYPE = BASE_URL + "/crop-types";

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
        public static final String VIEW_FARM = "/{id}";
        public static final String DELETE = "/{id}";
        public static final String OWNED_FARMS = "/owned-farms";
        public static final String UPDATE = "/{id}";
    }

    //Field Endpoints
    public static final class Field{
        public static final String CREATE = "/{farmId}/fields";
        public static final String GET_FIELDS_BY_FARM = "/farm/{farmId}";
        public static final String GET_FIELD_BY_ID = "/{fieldId}";
        public static final String UPDATE = "/{fieldId}";
        public static final String DELETE = "/{fieldId}";
    }

    //CropType Endpoints
    public static final class CropType{
        public static final String CREATE = "/{farmId}";
        public static final String UPDATE = "/{farmId}/{id}";
        public static final String DELETE = "/{farmId}/{id}";
        public static final String GET_CROP = "/{farmId}/{id}";
        public static final String GET_ALL_CROPS = "/{farmId}";
    }
}
