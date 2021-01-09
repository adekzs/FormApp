package com.adeks.formapp.model;

import java.io.File;
import java.util.Objects;

public class User {
    private String first_name;
    private String last_name;
    private String phone;
    private String password;
    private String password_confirmation;
    private String address;
    private String service;
    private String state;
    private File  image;
    private String lga;
    private String email;
    private String description;
    private String price;
    private String availability;
    private String location;
    private String duration;

    public User(String first_name, String last_Name, String phone, String password, String password_confirmation, String address, String service,
                String state, File file, String lga, String email, String description, String price, String availability, String location, String duration) {
        this.first_name = first_name;
        this.image = file;
        this.email = email;
        this.last_name = last_Name;
        this.phone = phone;
        this.password = password;
        this.password_confirmation = password_confirmation;
        this.address = address;
        this.service = service;
        this.state = state;
        this.lga = lga;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.location = location;
        this.duration = duration;
    }

    public File getFile() {
        return image;
    }

    public void setFile(File file) {
        this.image = file;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLga() {
        return lga;
    }

    public void setLga(String lga) {
        this.lga = lga;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_Name() {
        return last_name;
    }

    public void setLast_Name(String last_Name) {
        this.last_name = last_Name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getFirst_name().equals(user.getFirst_name()) &&
                getLast_Name().equals(user.getLast_Name()) &&
                getPhone().equals(user.getPhone()) &&
                getPassword().equals(user.getPassword()) &&
                getPassword_confirmation().equals(user.getPassword_confirmation()) &&
                getAddress().equals(user.getAddress()) &&
                getService().equals(user.getService()) &&
                getState().equals(user.getState()) &&
                getDescription().equals(user.getDescription()) &&
                getPrice().equals(user.getPrice()) &&
                getAvailability().equals(user.getAvailability()) &&
                getLocation().equals(user.getLocation()) &&
                getDuration().equals(user.getDuration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirst_name(), getLast_Name(), getPhone());
    }

    @Override
    public String toString() {
        return "User{" +
                "first_name='" + first_name + '\'' +
                ", last_Name='" + last_name + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", password_confirmation='" + password_confirmation + '\'' +
                ", address='" + address + '\'' +
                ", service='" + service + '\'' +
                ", state='" + state + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", availability='" + availability + '\'' +
                ", location='" + location + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
