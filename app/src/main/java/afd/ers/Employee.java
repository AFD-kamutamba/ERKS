package afd.ers;


public class Employee {

    private String employee_name;
    private String employee_password;

    public Employee(String employee_name, String employee_password) {
        this.employee_name = employee_name;
        this.employee_password = employee_password;
    }

    public String getEmployeeName() {
        return employee_name;
    }

    public String getEmployeePassword() {
        return employee_password;
    }
}
