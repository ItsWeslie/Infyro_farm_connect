package com.infyro.ifc.api;

public final class APIConstants {
    private APIConstants() {}
    private static final String API="/ifc", VERSION="/v1", BASE_URL=API+VERSION;
    public static final String AUTH=BASE_URL+"/auth", USER=BASE_URL+"/user", FARM=BASE_URL+"/farm",
            FIELD=BASE_URL+"/field", CROP_PLANTING=BASE_URL+"/crop-plantings",
            CROP_TYPE=BASE_URL+"/crop-types", CROP_ACTIVITY=BASE_URL+"/crop-activities";
    public static final class Auth {
        public static final String LOGIN="/login", LOGOUT="/logout", VALIDATE="/validate",
                SIGNUP="/signup", REFRESH="/refresh", LOGOUT_ALL="/logout-all";
    }
    public static final class Farm {
        public static final String CREATE="/create", VIEW_FARM="/{id}", DELETE="/{id}",
                OWNED_FARMS="/owned-farms", UPDATE="/{id}";
    }
    public static final class Field {
        public static final String CREATE="/{farmId}/fields", GET_FIELDS_BY_FARM="/farm/{farmId}",
                GET_FIELD_BY_ID="/{fieldId}", UPDATE="/{fieldId}", DELETE="/{fieldId}";
    }
    public static final class CropType {
        public static final String CREATE="/{farmId}", UPDATE="/{farmId}/{id}", DELETE="/{farmId}/{id}",
                GET_CROP="/{farmId}/{id}", GET_ALL_CROPS="/{farmId}";
    }
    public static final class CropActivity {
        public static final String BY_PLANTING="/planting/{plantingId}", BY_ID="/{id}", STATUS="/{id}/status";
    }
    public static final class CropPlanting {
        public static final String CREATE="/fields/{fieldId}", GET_BY_FIELD="/fields/{fieldId}",
                GET_BY_ID="/{id}", UPDATE="/{id}", UPDATE_STATUS="/{id}/status", DELETE="/{id}";
    }
}