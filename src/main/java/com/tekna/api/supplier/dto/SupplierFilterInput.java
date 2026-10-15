package com.tekna.api.supplier.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierFilterInput {

    private String name;
    private String contactName;
    private String email;
}
