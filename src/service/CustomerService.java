package service;

import data.DatabaseHelper;
import model.Customer;

import java.sql.ResultSet;

public class CustomerService {
    public static boolean addCustomer(Customer customer) {
        return DatabaseHelper.addCustomer(
                customer.fullName,
                customer.dob,
                customer.address,
                customer.phone,
                customer.identityCard
        );
    }

    public static boolean updateCustomer(Customer customer) {
        return DatabaseHelper.updateCustomer(
                customer.id,
                customer.fullName,
                customer.dob,
                customer.address,
                customer.phone,
                customer.identityCard
        );
    }

    public static boolean deleteCustomer(int customerId) {
        return DatabaseHelper.deleteCustomer(customerId);
    }

    public static String validateCustomer(Customer customer) {
        if (customer.fullName == null || customer.fullName.trim().isEmpty()) {
            return "Tên khách hàng không được để trống.";
        }
        if (customer.phone == null || customer.phone.trim().isEmpty()) {
            return "Số điện thoại không được để trống.";
        }
        if (customer.identityCard == null || customer.identityCard.trim().isEmpty()) {
            return "Số CCCD/CMND không được để trống.";
        }
        return DatabaseHelper.checkDuplicateCustomer(customer.phone, customer.identityCard);
    }

    public static ResultSet searchCustomers(String keyword) {
        return DatabaseHelper.searchCustomers(keyword);
    }

    public static ResultSet getAllCustomers() {
        return DatabaseHelper.getAllCustomers();
    }
}
