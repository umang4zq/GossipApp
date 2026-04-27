    package com.example.gossipapp;

    import android.os.Parcel;
    import android.os.Parcelable;

    import com.google.firebase.Timestamp;

    public class UserModel implements Parcelable {
        private String email;
        private String username;
        private String password;
        private String userId;
        private Timestamp timestamp;
        private boolean isOnline;
        private String usernameLower;
        private String profilepic;
        private String avatarResName;
        private String fcmToken;
        public UserModel() {}

        public UserModel(String email, String username, String password, String userId, Timestamp timestamp, boolean isOnline,String fcmToken) {
            this.email = email;
            this.username = username;
            this.password = password;
            this.userId = userId;
            this.timestamp = timestamp;
            this.isOnline = isOnline;
            this.fcmToken = fcmToken;


        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Timestamp getTimestamp() { return timestamp; }
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean online) { isOnline = online; }
        public String   getProfilepic() { return profilepic; }
        public String getAvatarResName() { return avatarResName; }
        public String getFcmToken() {
            return fcmToken;
        }

        public void setFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
        }


        // Parcelable implementation
        protected UserModel(Parcel in) {
            email = in.readString();
            username = in.readString();
            password = in.readString();
            userId = in.readString();
            long timeMillis = in.readLong();
            timestamp = new Timestamp(timeMillis / 1000, (int) ((timeMillis % 1000) * 1000000)); // convert ms back to seconds+nanos
            isOnline = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(email);
            dest.writeString(username);
            dest.writeString(password);
            dest.writeString(userId);
            dest.writeLong(timestamp != null ? timestamp.toDate().getTime() : 0); // write as milliseconds
            dest.writeByte((byte) (isOnline ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
            @Override
            public UserModel createFromParcel(Parcel in) {
                return new UserModel(in);
            }

            @Override
            public UserModel[] newArray(int size) {
                return new UserModel[size];
            }
        };

    }
