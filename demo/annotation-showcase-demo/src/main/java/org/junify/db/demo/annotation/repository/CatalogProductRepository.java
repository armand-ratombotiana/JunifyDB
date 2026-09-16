package org.junify.db.demo.annotation.repository;

import org.junify.db.adapter.jnosql.DocumentTemplate;
import org.junify.db.adapter.jnosql.JunifyRepository;
import org.junify.db.demo.annotation.model.CatalogProduct;

import java.util.List;

/**
 * Standard repository implementation for CatalogProduct demonstrating Eclipse JNoSQL / Spring Data-like
 * repository patterns over JunifyDB.
 */
public class CatalogProductRepository extends JunifyRepository<CatalogProduct, String> {

    public CatalogProductRepository(org.junify.db.JunifyDB db) {
        super(CatalogProduct.class, db);
    }

    public List<CatalogProduct> findByCategory(String category) {
        return findBy("category", category);
    }

    public List<CatalogProduct> findInStock(int minStock) {
        return query("SELECT * FROM catalog_products WHERE stock_qty >= ? ORDER BY unit_price ASC", minStock);
    }

    public List<CatalogProduct> findByPriceBetween(double minPrice, double maxPrice) {
        return query("SELECT * FROM catalog_products WHERE unit_price BETWEEN ? AND ? ORDER BY unit_price ASC", minPrice, maxPrice);
    }

    public List<CatalogProduct> findByFluentCategory(String category, double maxPrice) {
        return db.from(CatalogProduct.class)
                .where("category = ? AND unit_price <= ?", category, maxPrice)
                .orderBy("unit_price ASC")
                .list();
    }
}
