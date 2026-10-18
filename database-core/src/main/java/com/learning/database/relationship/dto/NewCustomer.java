package com.learning.database.relationship.dto;

import java.util.List;

public record NewCustomer(String name, String email, List<NewOrder> orders) {}
