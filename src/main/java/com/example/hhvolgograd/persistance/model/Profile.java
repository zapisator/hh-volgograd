package com.example.hhvolgograd.persistance.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Min(0)
    private BigDecimal cash;

    @OneToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Profile(BigDecimal cash) {
        this.cash = cash;
    }

    @Override
    public String toString() {
        return "Profile {" +
                "\n\tid=" + id +
                ",\n\tcash=" + cash +
                "\n}";
    }
}
