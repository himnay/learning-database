package com.learning.graph.controller;

import com.learning.graph.service.ShopGraphService;
import com.learning.graph.service.SocialGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final SocialGraphService social;
    private final ShopGraphService shop;

    // ---- social graph (homogeneous: person + knows) ----

    @GetMapping("/social/persons")
    public List<String> persons() {
        return social.listPersons();
    }

    @GetMapping("/social/knows")
    public List<Map<String, Object>> knows() {
        return social.whoKnowsWhom();
    }

    @GetMapping("/social/friends-of-friends")
    public List<Map<String, Object>> friendsOfFriends(@RequestParam(defaultValue = "Alice") String name) {
        return social.friendsOfFriends(name);
    }

    @GetMapping("/social/known-by")
    public List<String> knownBy(@RequestParam(defaultValue = "Alice") String name) {
        return social.whoIsKnownBy(name);
    }

    @GetMapping("/social/connections")
    public List<String> connections(@RequestParam(defaultValue = "Alice") String name) {
        return social.connections(name);
    }

    @GetMapping("/social/reachable")
    public List<Map<String, Object>> reachable(@RequestParam(defaultValue = "Alice") String name,
                                               @RequestParam(defaultValue = "4") int maxDepth) {
        return social.reachable(name, maxDepth);
    }

    @GetMapping("/social/explain")
    public List<String> explain() {
        return social.explainFriendsOfFriends();
    }

    // ---- shop graph (heterogeneous: customer / "order" / product / employee) ----

    @GetMapping("/shop/customer-orders")
    public List<Map<String, Object>> customerOrders() {
        return shop.customerOrders();
    }

    @GetMapping("/shop/order-contents")
    public List<Map<String, Object>> orderContents() {
        return shop.orderContents();
    }

    @GetMapping("/shop/persons")
    public List<String> shopPersons() {
        return shop.allPersons();
    }

    @GetMapping("/shop/recommendations")
    public List<Map<String, Object>> recommendations(@RequestParam(defaultValue = "Wireless Headphones") String product,
                                                     @RequestParam(defaultValue = "0.5") double minWeight) {
        return shop.recommendations(product, minWeight);
    }

    @GetMapping("/shop/top-customers")
    public List<Map<String, Object>> topCustomers() {
        return shop.topCustomersBySpend();
    }
}
