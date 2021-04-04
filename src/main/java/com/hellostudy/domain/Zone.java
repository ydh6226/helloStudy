package com.hellostudy.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@Getter
public class Zone {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    private String province;

    public Zone(String city, String localNameOfCity, String province) {
        this.city = city;
        this.localNameOfCity = localNameOfCity;
        this.province = province;
    }

    /**
     * @return ex) Seoul(서울특별시)/none
     */
    public String getFullName() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}
