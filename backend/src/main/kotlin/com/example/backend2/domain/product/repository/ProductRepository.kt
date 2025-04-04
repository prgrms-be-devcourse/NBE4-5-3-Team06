package com.example.backend2.domain.product.repository

import com.example.backend2.domain.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long>
