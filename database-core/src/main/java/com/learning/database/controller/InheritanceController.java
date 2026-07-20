package com.learning.database.controller;

import com.learning.database.entity.inheritance.AnimalEntityInheritanceType;
import com.learning.database.entity.inheritance.PaymentEntityInheritanceType;
import com.learning.database.entity.inheritance.VehicleEntityInheritanceType;
import com.learning.database.entity.inheritance.entites.CarEntity;
import com.learning.database.entity.inheritance.entites.ComputerEntity;
import com.learning.database.entity.inheritance.entites.DogEntity;
import com.learning.database.repository.AnimalRepository;
import com.learning.database.repository.ComputerRepository;
import com.learning.database.repository.PaymentRepository;
import com.learning.database.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * All four inheritance mapping strategies, one resource each:
 *
 *   /vehicles  SINGLE_TABLE     — one table + dtype discriminator; polymorphic query = plain SELECT
 *   /payments  JOINED           — parent + child tables; polymorphic query = JOINs
 *   /animals   TABLE_PER_CLASS  — table per subclass; polymorphic query = UNION ALL (slow)
 *   /computers MappedSuperclass — no polymorphic query possible; each subclass has its own repo
 *
 * Watch the SQL log (show-sql=true) while hitting these to see the difference.
 */
@RestController
@RequestMapping("/api/inheritance")
@RequiredArgsConstructor
public class InheritanceController {

    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final AnimalRepository animalRepository;
    private final ComputerRepository computerRepository;

    // ── SINGLE_TABLE ─────────────────────────────────────────────────────────

    /** Polymorphic: returns Cars and Motorcycles from ONE table (no join, no union). */
    @GetMapping("/vehicles")
    public List<VehicleEntityInheritanceType> vehicles() {
        return vehicleRepository.findAll();
    }

    @GetMapping("/vehicles/by-brand/{brand}")
    public List<VehicleEntityInheritanceType> vehiclesByBrand(@PathVariable String brand) {
        return vehicleRepository.findByBrand(brand);
    }

    /** Subclass-narrowed derived query — Spring Data filters on the discriminator. */
    @GetMapping("/vehicles/cars")
    public List<CarEntity> cars(@RequestParam String brand, @RequestParam Integer doors) {
        return vehicleRepository.findByBrandAndNumDoors(brand, doors);
    }

    // ── JOINED ───────────────────────────────────────────────────────────────

    /** Polymorphic: parent row joined to the matching child table per subtype. */
    @GetMapping("/payments")
    public List<PaymentEntityInheritanceType> payments() {
        return paymentRepository.findAll();
    }

    @GetMapping("/payments/above")
    public List<PaymentEntityInheritanceType> paymentsAbove(@RequestParam BigDecimal amount) {
        return paymentRepository.findByAmountGreaterThan(amount);
    }

    // ── TABLE_PER_CLASS ──────────────────────────────────────────────────────

    /** Polymorphic — but Hibernate must UNION ALL every subclass table. */
    @GetMapping("/animals")
    public List<AnimalEntityInheritanceType> animals() {
        return animalRepository.findAll();
    }

    @GetMapping("/animals/by-name/{name}")
    public List<AnimalEntityInheritanceType> animalsByName(@PathVariable String name) {
        return animalRepository.findByName(name);
    }

    /** Subclass query hits only the dog table — no union. */
    @GetMapping("/animals/dogs")
    public List<DogEntity> dogsByBreed(@RequestParam String breed) {
        return animalRepository.findByBreed(breed);
    }

    // ── MappedSuperclass ─────────────────────────────────────────────────────

    /** No polymorphic query exists — this repo only ever sees the computer table. */
    @GetMapping("/computers")
    public List<ComputerEntity> computers() {
        return computerRepository.findAll();
    }

    @GetMapping("/computers/by-os/{os}")
    public List<ComputerEntity> computersByOs(@PathVariable String os) {
        return computerRepository.findByOs(os);
    }
}
