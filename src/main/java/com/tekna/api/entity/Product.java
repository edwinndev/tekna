package com.tekna.api.entity;

import com.tekna.api.common.entity.BaseEntity;
import com.tekna.api.common.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at is null")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Category category;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 5;

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.stock = (this.stock == null ? 0 : this.stock) + quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        int currentStock = (this.stock == null ? 0 : this.stock);
        if (currentStock < quantity) {
            throw new InsufficientStockException("Insufficient stock for product '" + this.name + "'. Current stock: "
                    + currentStock + ", requested: " + quantity);
        }
        this.stock = currentStock - quantity;
    }

    public boolean isLowStock() {
        return this.stock != null && this.minStock != null && this.stock <= this.minStock;
    }
}
