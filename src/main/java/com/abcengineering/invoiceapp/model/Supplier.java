package com.abcengineering.invoiceapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "supplier")
@Getter
@Setter
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @NotBlank
    @Column(name = "company_address", nullable = false)
    private String companyAddress;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_email", length = 254)
    private String contactEmail;

    @Column(name = "contact_telephone", length = 30)
    private String contactTelephone;

    @Column(name = "active", nullable = false)
    private boolean active;

    // Required by JPA
    protected Supplier() {}

    public Supplier(String companyName, String companyAddress) {
        this(companyName, companyAddress, null, null, null);
    }

    public Supplier(String companyName, String companyAddress,
                    String contactName, String contactEmail,
                    String contactTelephone) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactTelephone = contactTelephone;
        this.active = true;
    }
}
