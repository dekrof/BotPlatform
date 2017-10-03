package ua.ukrposhta.models.facebook;

import com.google.gson.annotations.SerializedName;

public class FacebookUserProfile {
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("profile_pic")
    private String profilePic;
    @SerializedName("timezone")
    private String timeZone;
    private String gender;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getGender() {
        return gender;
    }
}
