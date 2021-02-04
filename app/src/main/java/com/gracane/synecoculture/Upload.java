package com.gracane.synecoculture;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mUploaderName;
    private String mUploaderSurname;
    private String mUploaderEmail;
    private String mImageUrl;
    private String mKey;

    public Upload(){
//empty constructor needed
    }

    public Upload(String name,String uploadername,String uploadersurname,String uploaderemail, String imageUrl){
        if(name.trim().equals("")){
            name = "No Name";
        }
        mName = name;
        mUploaderName = uploadername;
        mUploaderSurname = uploadersurname;
        mUploaderEmail = uploaderemail;
        mImageUrl = imageUrl;
    }

    public String getName(){ return mName; }
    public void setName(String name){ mName = name; }

    public String getUploaderName(){ return mUploaderName; }
    public void setUploaderName(String uploader){ mUploaderName = uploader; }

    public String getUploaderEmail(){ return mUploaderEmail; }
    public void setUploaderEmail(String uploaded){ mUploaderEmail = uploaded; }

    public String getUploaderSurname(){ return mUploaderSurname; }
    public void setUploaderSurname(String uploader){ mUploaderSurname = uploader; }


    public String getImageUrl(){
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl){
        mImageUrl = imageUrl;
    }
    @Exclude
    public String getKey(){
        return mKey;
    }
    @Exclude
    public void setKey(String key){
        mKey = key;
    }
}
