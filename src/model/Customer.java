package model;
public class Customer {
    public int id; // Sửa thành int (số tự tăng)
    public String fullName;
    public String dob;
    public String address;
    public String phone;
    public String identityCard;

    public Customer() {}

    public Customer(int id, String fullName, String dob, String address, String phone, String identityCard) {
        this.id = id;
        this.fullName = fullName;
        this.dob = dob;
        this.address = address;
        this.phone = phone;
        this.identityCard = identityCard;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}