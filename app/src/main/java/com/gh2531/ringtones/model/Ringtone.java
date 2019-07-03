package com.gh2531.ringtones.model;

public class Ringtone {
    private String file_name;
    private String file_url;

    public Ringtone(){

    }

    public Ringtone(String file_name, String file_url) {
        this.file_name = file_name;
        this.file_url = file_url;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
}
