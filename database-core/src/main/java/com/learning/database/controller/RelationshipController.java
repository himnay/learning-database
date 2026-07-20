package com.learning.database.controller;

import com.learning.database.entity.embeddable.OrderItemEntity;
import com.learning.database.entity.embeddable.OrderItemId;
import com.learning.database.entity.relationship.*;
import com.learning.database.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * JPA relationship demos:
 *   @OneToOne    User ↔ Address        (JOIN FETCH to load both in one query)
 *   @OneToMany   Customer ↔ Order      (cascade persist, orphanRemoval, JSON managed/back refs)
 *   @ManyToMany  Student ↔ Course      (join table, @JsonIgnoreProperties to break cycles)
 *   @EmbeddedId  OrderItem             (composite key lookups)
 *
 * List endpoints return summary DTOs (lazy collections not fetched);
 * detail endpoints use the JOIN FETCH repository methods and return full entities.
 */
@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final OrderItemRepository orderItemRepository;

    public record PersonSummary(Long id, String name, String email) {}
    public record CourseSummary(Long id, String title, String description) {}
    public record EnrollmentCount(String title, Integer students) {}
    public record NewOrder(String product, BigDecimal amount) {}
    public record NewCustomer(String name, String email, List<NewOrder> orders) {}

    // ── @OneToOne: User ↔ Address ────────────────────────────────────────────

    /** JOIN FETCH — user + address in one SQL join. */
    @GetMapping("/users")
    public List<UserEntity> usersWithAddress() {
        return userRepository.findAllWithAddress();
    }

    @GetMapping("/users/by-email")
    public PersonSummary userByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(u -> new PersonSummary(u.getId(), u.getName(), u.getEmail()))
                .orElse(null);
    }

    // ── @OneToMany: Customer ↔ Order ─────────────────────────────────────────

    @GetMapping("/customers")
    public List<PersonSummary> customers() {
        return customerRepository.findAll().stream()
                .map(c -> new PersonSummary(c.getId(), c.getName(), c.getEmail()))
                .toList();
    }

    /** JOIN FETCH — customer + orders in one query; @JsonManagedReference serializes orders. */
    @GetMapping("/customers/{id}")
    public CustomerEntity customerWithOrders(@PathVariable Long id) {
        return customerRepository.findByIdWithOrders(id).orElse(null);
    }

    /**
     * CascadeType.ALL demo: one save() persists the customer AND its orders.
     * Both sides of the bidirectional link must be set in code (JPA does not sync them).
     */
    @PostMapping("/customers")
    public CustomerEntity createCustomer(@RequestBody NewCustomer request) {
        CustomerEntity customer = new CustomerEntity();
        customer.setName(request.name());
        customer.setEmail(request.email());
        if (request.orders() != null) {
            for (NewOrder newOrder : request.orders()) {
                OrderEntity order = new OrderEntity();
                order.setProduct(newOrder.product());
                order.setAmount(newOrder.amount());
                order.setCustomer(customer);          // owning side (FK)
                customer.getOrders().add(order);      // inverse side
            }
        }
        return customerRepository.save(customer);     // cascades PERSIST to orders
    }

    /** orphanRemoval demo: removing an order from the collection deletes its row. */
    @DeleteMapping("/customers/{customerId}/orders/{orderId}")
    @Transactional
    public CustomerEntity removeOrder(@PathVariable Long customerId, @PathVariable Long orderId) {
        CustomerEntity customer = customerRepository.findByIdWithOrders(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        customer.getOrders().removeIf(o -> o.getId().equals(orderId));  // orphanRemoval deletes the row
        return customerRepository.save(customer);
    }

    // ── @ManyToMany: Student ↔ Course ────────────────────────────────────────

    @GetMapping("/students")
    public List<PersonSummary> students() {
        return studentRepository.findAll().stream()
                .map(s -> new PersonSummary(s.getId(), s.getName(), s.getEmail()))
                .toList();
    }

    /** JOIN FETCH — student + courses; @JsonIgnoreProperties("students") breaks the cycle. */
    @GetMapping("/students/{id}")
    public StudentEntity studentWithCourses(@PathVariable Long id) {
        return studentRepository.findByIdWithCourses(id).orElse(null);
    }

    /** Enroll: modifies the owning side (student.courses) — inserts into the join table. */
    @PostMapping("/students/{studentId}/courses/{courseId}")
    @Transactional
    public StudentEntity enroll(@PathVariable Long studentId, @PathVariable Long courseId) {
        StudentEntity student = studentRepository.findByIdWithCourses(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (student.getCourses().stream().noneMatch(c -> c.getId().equals(courseId))) {
            student.getCourses().add(course);
        }
        return studentRepository.save(student);
    }

    @GetMapping("/courses")
    public List<CourseSummary> courses() {
        return courseRepository.findAll().stream()
                .map(c -> new CourseSummary(c.getId(), c.getTitle(), c.getDescription()))
                .toList();
    }

    /** Inverse side JOIN FETCH — course + enrolled students. */
    @GetMapping("/courses/{id}")
    public CourseEntity courseWithStudents(@PathVariable Long id) {
        return courseRepository.findByIdWithStudents(id).orElse(null);
    }

    /** Aggregate over the join table: SIZE(c.students) per course. */
    @GetMapping("/courses/enrollments")
    public List<EnrollmentCount> enrollments() {
        return courseRepository.findCourseEnrollmentCounts().stream()
                .map(row -> new EnrollmentCount((String) row[0], ((Number) row[1]).intValue()))
                .toList();
    }

    // ── @EmbeddedId: OrderItem composite key ─────────────────────────────────

    @GetMapping("/order-items/by-order/{orderId}")
    public List<OrderItemEntity> orderItemsByOrder(@PathVariable Long orderId) {
        return orderItemRepository.findById_OrderId(orderId);
    }

    /** findById with the composite OrderItemId(orderId, productCode). */
    @GetMapping("/order-items/{orderId}/{productCode}")
    public OrderItemEntity orderItem(@PathVariable Long orderId, @PathVariable String productCode) {
        return orderItemRepository.findById(new OrderItemId(orderId, productCode)).orElse(null);
    }
}
