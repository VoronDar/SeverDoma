package com.mribi.severdoma.pojo;

public class Bisiness {
    private String name;
    private String phoneNumber;
    private String mail;
    private String description;
    private int Type;
    private double latitude;
    private double longitude;
    private String id;
    private String vk;
    private String insta;
    private String fb;
    private String address;

    public Bisiness() {
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public String getVk() {
        return vk;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public String getInsta() {
        return insta;
    }

    public void setInsta(String insta) {
        this.insta = insta;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bisiness bisiness = (Bisiness) o;

        if (Type != bisiness.Type) return false;
        if (Double.compare(bisiness.latitude, latitude) != 0) return false;
        if (Double.compare(bisiness.longitude, longitude) != 0) return false;
        if (name != null ? !name.equals(bisiness.name) : bisiness.name != null) return false;
        if (phoneNumber != null ? !phoneNumber.equals(bisiness.phoneNumber) : bisiness.phoneNumber != null)
            return false;
        if (mail != null ? !mail.equals(bisiness.mail) : bisiness.mail != null) return false;
        if (description != null ? !description.equals(bisiness.description) : bisiness.description != null)
            return false;
        return id != null ? id.equals(bisiness.id) : bisiness.id == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (mail != null ? mail.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + Type;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
