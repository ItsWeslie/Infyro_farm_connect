package com.infyro.ifc.api;

public final class APIConstants {
    private APIConstants() {}

    private static final String API="/ifc";
    private static final String VERSION="/v1";
    private static final String BASE_URL=API+VERSION;


    public static final String AUTH=BASE_URL+"/auth";
    private static final String USER=BASE_URL+"/user";
    private static final String FARM=BASE_URL+"/farm";
    private static final String FIELD=BASE_URL+"/field";
    private static final String CROP_PLANTING=BASE_URL+"/crop-plantings";
    private static final String CROP_TYPE=BASE_URL+"/crop-types";
    private static final String CROP_ACTIVITY=BASE_URL+"/crop-activities";

    public static final class Auth {
        public static final String LOGIN="/login";
        private static final String LOGOUT="/logout";
        private static final String VALIDATE="/validate";
        private static final String SIGNUP="/signup";
        private static final String REFRESH="/refresh";
        private static final String LOGOUT_ALL="/logout-all";
    }
    public static final class Farm {
        public static final String CREATE="/create";
        private static final String VIEW_FARM="/{id}";
        private static final String DELETE="/{id}";
        private static final String OWNED_FARMS="/owned-farms";
        private static final String UPDATE="/{id}";
    }
    public static final class Field {
        public static final String CREATE="/{farmId}/fields";
        private static final String GET_FIELDS_BY_FARM="/farm/{farmId}";
        private static final String GET_FIELD_BY_ID="/{fieldId}";
        private static final String UPDATE="/{fieldId}";
        private static final String DELETE="/{fieldId}";
    }
    public static final class CropType {
        public static final String CREATE="/{farmId}";
        private static final String UPDATE="/{farmId}/{id}";
        private static final String DELETE="/{farmId}/{id}";
        private static final String GET_CROP="/{farmId}/{id}";
        private static final String GET_ALL_CROPS="/{farmId}";
    }
    public static final class CropActivity {
        public static final String BY_PLANTING="/planting/{plantingId}";
        private static final String BY_ID="/{id}";
        private static final String STATUS="/{id}/status";
    }
    public static final class CropPlanting {
        public static final String CREATE="/fields/{fieldId}";
        private static final String GET_BY_FIELD="/fields/{fieldId}";
        private static final String GET_BY_ID="/{id}";
        private static final String UPDATE="/{id}";
        private static final String UPDATE_STATUS="/{id}/status";
        private static final String DELETE="/{id}";
    }
}