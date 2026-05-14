package gov.state.hhs.model;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a citizen client who has applied for or is receiving HHS services.
 */
@Entity
@Table(name = "client")
@NamedQueries({
    @NamedQuery(
        name  = "Client.findAll",
        query = "SELECT c FROM Client c ORDER BY c.lastName, c.firstName"
    ),
    @NamedQuery(
        name  = "Client.searchByName",
        query = "SELECT c FROM Client c WHERE LOWER(c.lastName) LIKE :term OR LOWER(c.firstName) LIKE :term ORDER BY c.lastName"
    ),
    @NamedQuery(
        name  = "Client.findByEmail",
        query = "SELECT c FROM Client c WHERE c.email = :email"
    )
})
public class Client implements Serializable {

    @Id
    @SequenceGenerator(name = "client_seq", sequenceName = "client_seq", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_seq")
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @NotBlank
    @Size(max = 60)
    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // Store only last four digits for privacy
    @Size(min = 4, max = 4)
    @Column(name = "ssn_last_four", length = 4)
    private String ssnLastFour;

    @Pattern(regexp = "^[0-9\\-() +]{7,20}$", message = "Please enter a valid phone number.")
    @Column(length = 20)
    private String phone;

    @Email
    @Column(length = 150)
    private String email;

    @NotBlank
    @Column(name = "address_line1", nullable = false, length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 100)
    private String addressLine2;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String city;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @NotBlank
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Please enter a valid ZIP code.")
    @Column(nullable = false, length = 10)
    private String zip;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ServiceApplication> applications = new ArrayList<>();

    @PrePersist
    protected void onPersist() {
        this.createdDate = LocalDateTime.now();
    }

    // --- Convenience methods ---

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSsnLastFour() { return ssnLastFour; }
    public void setSsnLastFour(String ssnLastFour) { this.ssnLastFour = ssnLastFour; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public LocalDateTime getCreatedDate() { return createdDate; }

    public List<ServiceApplication> getApplications() { return applications; }
}
