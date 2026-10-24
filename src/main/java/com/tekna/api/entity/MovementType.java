package com.tekna.api.entity;

public enum MovementType {
    ENTRY {
        @Override
        public void applyToStock(Product product, int quantity) {
            product.increaseStock(quantity);
        }
    },
    EXIT {
        @Override
        public void applyToStock(Product product, int quantity) {
            product.decreaseStock(quantity);
        }
    };

    /**
     * Applies the movement quantity to the product stock polymorphically.
     *
     * @param product  the product whose stock is being updated
     * @param quantity the amount of stock to apply
     */
    public abstract void applyToStock(Product product, int quantity);
}
