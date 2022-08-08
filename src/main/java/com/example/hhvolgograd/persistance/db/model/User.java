package com.example.hhvolgograd.persistance.db.model;

import com.example.hhvolgograd.exception.IncorrectJsonException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Schema(description = "A person with its identifiers")
public class User {

    public static final int MIN_AGE = 0;
    public static final int MAX_AGE = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Min(value = MIN_AGE, message = "Human age could not be less than " + MIN_AGE)
    @Max(value = MAX_AGE, message = "Human age could not be more than " + MAX_AGE)
    private int age;
    @Column(unique = true)
    @NotBlank(message = "Name may not be blank")
    @Pattern(
            regexp = "^[-a-z0-9!#$%&'*+/=?^_`{|}~]+"
                    + "(\\.[-a-z0-9!#$%&'*+/=?^_`{|}~]+)*@([a-z0-9]([-a-z0-9]{0,61}[a-z0-9])?\\.)"
                    + "*(aero|arpa|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil"
                    + "|mobi|museum|name|net|org|pro|tel|travel|[a-z][a-z])$"
    )
    private String email;

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    @JsonManagedReference
    private Profile profile;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "user"
    )
    @JsonManagedReference
    private Set<Phone> phones;

    public static User fromJson(String userJson) {
        try {
            return new ObjectMapper().readValue(userJson, User.class);
        } catch (JsonProcessingException e) {
            throw new IncorrectJsonException(format("The string '%s' is incorrect", userJson), e);
        }
    }

    public User(String name, int age, String email) {
        if (MAX_AGE < 0 || MIN_AGE >= MAX_AGE) {
            throw new RuntimeException(
                    format(
                            "Invalid class constants. Expected: min age is positive and '%d' < max age '%d'",
                            MIN_AGE, MAX_AGE)
            );
        }

        this.name = name;
        this.age = age;
        this.email = email;
        this.phones = new HashSet<>();
    }

    public User userWithProfile(Profile profile) {
        this.profile = profile;
        profile.setUser(this);
        return this;
    }

    public User userWithPhone(Phone phone) {
        this.phones.add(phone);
        phone.setUser(this);
        return this;
    }

    public User userWithPhones(Collection<Phone> phones) {
        this.phones.addAll(phones);
        phones.forEach(phone -> phone.setUser(this));
        return this;
    }

    @Override
    public String toString() {
        return "User {" +
                "\n\tid=" + id +
                ",\n\tname='" + name + '\'' +
                ",\n\tage=" + age +
                ",\n\temail='" + email + '\'' +
                (profile == null ? "" : ",\n\t" + profile) +
                (phones == null || phones.isEmpty() ? "" : ",\n\tphones: [\n" + phones + "\n\t]") +
                "\n}";
    }

    public String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}

