package com.shelfflow.services.common.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorefrontCatalogCacheKeysTest {

    @Test
    void categoriesKeyShouldUseDefaultPrefixWhenBlank() {
        assertEquals("shelfflow:storefront:catalog:categories", StorefrontCatalogCacheKeys.categoriesKey(" "));
    }

    @Test
    void productsKeyShouldBeStableAndSensitiveToQuery() {
        String first = StorefrontCatalogCacheKeys.productsKey(
                "shelfflow:storefront:catalog:",
                "Milk",
                11L,
                "p.update_time",
                "DESC",
                1,
                20
        );
        String second = StorefrontCatalogCacheKeys.productsKey(
                "shelfflow:storefront:catalog",
                " milk ",
                11L,
                "p.update_time",
                "desc",
                1,
                20
        );
        String differentPage = StorefrontCatalogCacheKeys.productsKey(
                "shelfflow:storefront:catalog",
                "milk",
                11L,
                "p.update_time",
                "desc",
                2,
                20
        );

        assertEquals(first, second);
        assertNotEquals(first, differentPage);
        assertTrue(first.startsWith("shelfflow:storefront:catalog:products:"));
    }

    @Test
    void allKeysPatternShouldMatchCatalogPrefix() {
        assertEquals("shelfflow:storefront:catalog:*", StorefrontCatalogCacheKeys.allKeysPattern(null));
    }
}
